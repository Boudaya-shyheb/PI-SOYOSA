package com.ecommerce.service;

import com.ecommerce.dto.OrderDTO;
import com.ecommerce.dto.OrderCourierAssignmentDTO;
import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.PageResponse;
import com.ecommerce.entity.Order;
import org.springframework.data.domain.Pageable;

import java.util.List;

public interface OrderService {
    OrderDTO getOrderById(Long id);
    List<OrderDTO> getOrdersByCustomer(Long customerId);
    PageResponse<OrderDTO> getOrdersByCustomerPaginated(Long customerId, Pageable pageable);
    List<OrderDTO> getAllOrders();
    PageResponse<OrderDTO> getAllOrdersPaginated(Pageable pageable);
    OrderDTO createOrder(CreateOrderRequest request);
    OrderDTO updateOrderStatus(Long orderId, Order.OrderStatus status);
    void cancelOrder(Long orderId);
    void deleteOrder(Long orderId);
    OrderCourierAssignmentDTO assignCourier(Long orderId, Long courierId, String courierName, Long assignedBy);
    OrderCourierAssignmentDTO unassignCourier(Long orderId, Long assignedBy);
    OrderCourierAssignmentDTO getCourierAssignment(Long orderId);
    List<OrderCourierAssignmentDTO> getAllCourierAssignments();
}
