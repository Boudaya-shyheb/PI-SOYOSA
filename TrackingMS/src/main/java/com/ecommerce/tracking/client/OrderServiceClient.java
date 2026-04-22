package com.ecommerce.tracking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "ecommerce-service", url = "${ecommerce-service.url:http://localhost:8085}")
public interface OrderServiceClient {

    @GetMapping("/api/internal/orders/{id}/owner")
    ApiResponse<OrderOwnerDto> getOrderOwner(@RequestHeader("Authorization") String authHeader,
                                             @PathVariable("id") Long id);

    @GetMapping("/api/internal/orders/{id}/courier")
    ApiResponse<OrderCourierDto> getOrderCourier(@RequestHeader("Authorization") String authHeader,
                                                 @PathVariable("id") Long id);

    class ApiResponse<T> {
        private String message;
        private T data;

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public T getData() {
            return data;
        }

        public void setData(T data) {
            this.data = data;
        }
    }

    class OrderOwnerDto {
        private Long orderId;
        private Long customerId;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Long getCustomerId() {
            return customerId;
        }

        public void setCustomerId(Long customerId) {
            this.customerId = customerId;
        }
    }

    class OrderCourierDto {
        private Long orderId;
        private Long courierId;

        public Long getOrderId() {
            return orderId;
        }

        public void setOrderId(Long orderId) {
            this.orderId = orderId;
        }

        public Long getCourierId() {
            return courierId;
        }

        public void setCourierId(Long courierId) {
            this.courierId = courierId;
        }
    }
}
