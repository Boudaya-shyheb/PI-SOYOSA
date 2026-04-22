package com.ecommerce.service.impl;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderCourierAssignmentDTO;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.CreateOrderRequest.OrderItemRequest;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.entity.OrderCourierAssignment;
import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderItem;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.BusinessException;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.mapper.EntityMapper;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.OrderItemRepository;
import com.ecommerce.repository.ProductRepository;
import com.ecommerce.repository.CouponRepository;
import com.ecommerce.repository.OrderCourierAssignmentRepository;
import com.ecommerce.service.OrderService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
@Transactional
@Slf4j
public class OrderServiceImpl implements OrderService {

    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;
    private final ProductRepository productRepository;
    private final CouponRepository couponRepository;
    private final OrderCourierAssignmentRepository orderCourierAssignmentRepository;
    private final EntityMapper entityMapper;

    @Override
    @Transactional(readOnly = true)
    public OrderDTO getOrderById(Long id) {
        log.debug("Fetching order with id: {}", id);
        return orderRepository.findById(id)
                .map(entityMapper::toOrderDTO)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", id));
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getOrdersByCustomer(Long customerId) {
        log.debug("Fetching orders for customer: {}", customerId);
        return orderRepository.findByCustomerId(customerId).stream()
                .map(entityMapper::toOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDTO> getOrdersByCustomerPaginated(Long customerId, Pageable pageable) {
        log.debug("Fetching orders for customer with pagination: customerId={}, page={}, size={}",
                customerId, pageable.getPageNumber(), pageable.getPageSize());
        Page<Order> page = orderRepository.findByCustomerId(customerId, pageable);
        return buildPageResponse(page);
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderDTO> getAllOrders() {
        log.debug("Fetching all orders");
        return orderRepository.findAll().stream()
                .map(entityMapper::toOrderDTO)
                .collect(Collectors.toList());
    }

    @Override
    @Transactional(readOnly = true)
    public PageResponse<OrderDTO> getAllOrdersPaginated(Pageable pageable) {
        log.debug("Fetching all orders with pagination: page={}, size={}", pageable.getPageNumber(),
                pageable.getPageSize());
        Page<Order> page = orderRepository.findAll(pageable);
        return buildPageResponse(page);
    }

    @Override
    public OrderDTO createOrder(CreateOrderRequest request) {
        log.debug("Creating order for customer: {}", request.getCustomerId());

        if (request.getItems() == null || request.getItems().isEmpty()) {
            throw new BusinessException("Order must contain at least one item");
        }

        // Create order
        Order order = Order.builder()
                .customerId(request.getCustomerId())
                .status(Order.OrderStatus.PENDING)
                .totalPrice(BigDecimal.ZERO)
            .deliveryStreet(normalizeAddressField(request.getDeliveryStreet()))
            .deliveryCity(normalizeAddressField(request.getDeliveryCity()))
            .deliveryPostalCode(normalizeAddressField(request.getDeliveryPostalCode()))
            .deliveryCountry(normalizeAddressField(request.getDeliveryCountry()))
            .deliveryLat(request.getDeliveryLat())
            .deliveryLng(request.getDeliveryLng())
                .build();

        BigDecimal totalPrice = BigDecimal.ZERO;

        // Add items to order
        for (OrderItemRequest itemRequest : request.getItems()) {
            Product product = productRepository.findById(itemRequest.getProductId())
                    .orElseThrow(() -> new ResourceNotFoundException("Product", "id", itemRequest.getProductId()));
            normalizeDiscount(product);

            // Check stock
            if (product.getQuantityAvailable() < itemRequest.getQuantity()) {
                throw new BusinessException("Insufficient stock for product: " + product.getName());
            }

            OrderItem orderItem = OrderItem.builder()
                    .order(order)
                    .product(product)
                    .quantity(itemRequest.getQuantity())
                    .price(product.getPrice())
                    .build();

            order.addOrderItem(orderItem);
            totalPrice = totalPrice.add(orderItem.getSubtotal());

            // Decrease product stock
            product.setQuantityAvailable(product.getQuantityAvailable() - itemRequest.getQuantity());
            productRepository.save(product);
        }

        order.setTotalPrice(totalPrice);

        // Apply coupon discount if provided
        if (request.getCouponCode() != null && !request.getCouponCode().isBlank()) {
            BigDecimal discountAmount = applyCouponToOrder(request.getCouponCode(), totalPrice);
            BigDecimal discounted = totalPrice.subtract(discountAmount);
            order.setTotalPrice(discounted.max(BigDecimal.ZERO));
            log.info("Coupon {} applied to order. Total: {} -> {}",
                request.getCouponCode(), totalPrice, order.getTotalPrice());
        } else if (request.getDiscountAmount() != null
            && request.getDiscountAmount().compareTo(BigDecimal.ZERO) > 0) {
            throw new BusinessException("Coupon code required when applying a discount");
        }

        Order savedOrder = orderRepository.save(order);
        log.info("Order created successfully with id: {}", savedOrder.getId());

        return entityMapper.toOrderDTO(savedOrder);
    }

    private String normalizeAddressField(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Override
    public OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status) {
        log.debug("Updating order {} status to {}", orderId, status);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Validate status transitions
        if (order.getStatus() == Order.OrderStatus.DELIVERED ||
                order.getStatus() == Order.OrderStatus.CANCELLED) {
            throw new BusinessException("Cannot update status of delivered or cancelled orders");
        }

        order.setStatus(status);
        Order updated = orderRepository.save(order);
        log.info("Order status updated to: {}", status);

        return entityMapper.toOrderDTO(updated);
    }

    @Override
    public void cancelOrder(Long orderId) {
        log.debug("Cancelling order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        if (order.getStatus() == Order.OrderStatus.SHIPPED ||
                order.getStatus() == Order.OrderStatus.DELIVERED) {
            throw new BusinessException("Cannot cancel order that has already been shipped");
        }

        // Restore stock for all items
        for (OrderItem item : order.getOrderItems()) {
            Product product = item.getProduct();
            product.setQuantityAvailable(product.getQuantityAvailable() + item.getQuantity());
            productRepository.save(product);
        }

        order.setStatus(Order.OrderStatus.CANCELLED);
        orderRepository.save(order);
        log.info("Order cancelled: {}", orderId);
    }

    @Override
    public void deleteOrder(Long orderId) {
        log.debug("Deleting order: {}", orderId);

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        // Delete all order items first
        orderItemRepository.deleteByOrderId(orderId);
        
        // Delete the order
        orderRepository.deleteById(orderId);
        log.info("Order deleted: {}", orderId);
    }

    @Override
    public OrderCourierAssignmentDTO assignCourier(Long orderId, Long courierId, String courierName, Long assignedBy) {
        Order order = orderRepository.findById(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("Order", "id", orderId));

        OrderCourierAssignment assignment = orderCourierAssignmentRepository
            .findByOrderId(order.getId())
            .orElseGet(() -> OrderCourierAssignment.builder().orderId(order.getId()).build());

        assignment.setCourierId(courierId);
        assignment.setCourierName(courierName);
        assignment.setAssignedBy(assignedBy);
        OrderCourierAssignment saved = orderCourierAssignmentRepository.save(assignment);

        if (order.getStatus() != Order.OrderStatus.CANCELLED && order.getStatus() != Order.OrderStatus.DELIVERED) {
            order.setStatus(Order.OrderStatus.SHIPPED);
            orderRepository.save(order);
        }

        return new OrderCourierAssignmentDTO(saved.getOrderId(), saved.getCourierId(),
            saved.getCourierName(), saved.getAssignedBy(), saved.getCreatedAt());
    }

    @Override
    public OrderCourierAssignmentDTO unassignCourier(Long orderId, Long assignedBy) {
        OrderCourierAssignment assignment = orderCourierAssignmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("OrderCourierAssignment", "orderId", orderId));

        orderCourierAssignmentRepository.deleteByOrderId(orderId);

        return new OrderCourierAssignmentDTO(assignment.getOrderId(), assignment.getCourierId(),
            assignment.getCourierName(), assignedBy, assignment.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public OrderCourierAssignmentDTO getCourierAssignment(Long orderId) {
        OrderCourierAssignment assignment = orderCourierAssignmentRepository.findByOrderId(orderId)
            .orElseThrow(() -> new ResourceNotFoundException("OrderCourierAssignment", "orderId", orderId));

        return new OrderCourierAssignmentDTO(assignment.getOrderId(), assignment.getCourierId(),
            assignment.getCourierName(), assignment.getAssignedBy(), assignment.getCreatedAt());
    }

    @Override
    @Transactional(readOnly = true)
    public List<OrderCourierAssignmentDTO> getAllCourierAssignments() {
        return orderCourierAssignmentRepository.findAll().stream()
            .map(assignment -> new OrderCourierAssignmentDTO(
                assignment.getOrderId(),
                assignment.getCourierId(),
                assignment.getCourierName(),
                assignment.getAssignedBy(),
                assignment.getCreatedAt()
            ))
            .collect(Collectors.toList());
    }

    // ==================== HELPER METHODS ====================

    private PageResponse<OrderDTO> buildPageResponse(Page<Order> page) {
        return PageResponse.<OrderDTO>builder()
                .content(page.getContent().stream()
                        .map(entityMapper::toOrderDTO)
                        .collect(Collectors.toList()))
                .currentPage(page.getNumber())
                .pageSize(page.getSize())
                .totalElements(page.getTotalElements())
                .totalPages(page.getTotalPages())
                .isFirst(page.isFirst())
                .isLast(page.isLast())
                .hasNext(page.hasNext())
                .hasPrevious(page.hasPrevious())
                .build();
    }

    private void normalizeDiscount(Product product) {
        if (product == null || product.getDiscountEndsAt() == null) {
            return;
        }
        if (product.getDiscountEndsAt().isAfter(LocalDateTime.now())) {
            return;
        }
        if (product.getOriginalPrice() != null) {
            product.setPrice(product.getOriginalPrice());
        }
        product.setOriginalPrice(null);
        product.setDiscountPercent(null);
        product.setDiscountEndsAt(null);
        productRepository.save(product);
    }

    private BigDecimal applyCouponToOrder(String code, BigDecimal orderTotal) {
        String normalized = code.trim().toUpperCase();
        com.ecommerce.entity.Coupon coupon = couponRepository.findByCodeIgnoreCase(normalized)
                .orElseThrow(() -> new BusinessException("Coupon '" + normalized + "' not found"));

        if (!Boolean.TRUE.equals(coupon.getActive())) {
            throw new BusinessException("Coupon '" + normalized + "' is no longer active");
        }

        java.time.LocalDateTime now = java.time.LocalDateTime.now();
        if (coupon.getStartsAt() != null && now.isBefore(coupon.getStartsAt())) {
            throw new BusinessException("Coupon '" + normalized + "' is not yet valid");
        }
        if (coupon.getEndsAt() != null && now.isAfter(coupon.getEndsAt())) {
            throw new BusinessException("Coupon '" + normalized + "' has expired");
        }

        if (coupon.getUsageLimit() != null && coupon.getUsedCount() >= coupon.getUsageLimit()) {
            throw new BusinessException("Coupon '" + normalized + "' usage limit has been reached");
        }

        if (coupon.getMinOrderTotal() != null
                && orderTotal.compareTo(coupon.getMinOrderTotal()) < 0) {
            throw new BusinessException(
                    "Minimum order total of " + coupon.getMinOrderTotal() + " TND required for this coupon");
        }

        BigDecimal discountAmount = BigDecimal.ZERO;
        switch (coupon.getType()) {
            case PERCENT -> {
                discountAmount = orderTotal
                        .multiply(coupon.getValue())
                        .divide(new BigDecimal("100"), 2, java.math.RoundingMode.HALF_UP);
                if (coupon.getMaxDiscount() != null
                        && discountAmount.compareTo(coupon.getMaxDiscount()) > 0) {
                    discountAmount = coupon.getMaxDiscount();
                }
            }
            case FIXED -> discountAmount = coupon.getValue().min(orderTotal);
            case FREE_SHIPPING -> discountAmount = BigDecimal.ZERO;
        }

        coupon.setUsedCount(coupon.getUsedCount() + 1);
        couponRepository.save(coupon);

        return discountAmount;
    }
}
