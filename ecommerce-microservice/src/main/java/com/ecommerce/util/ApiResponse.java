package com.ecommerce.util;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ApiResponse<T> {
    private String message;
    private T data;
    private boolean success;
    private LocalDateTime timestamp;
    
    public ApiResponse(String message, T data) {
        this.message = message;
        this.data = data;
        this.success = true;
        this.timestamp = LocalDateTime.now();
    }
    
    public ApiResponse(String message, T data, boolean success) {
        this.message = message;
        this.data = data;
        this.success = success;
        this.timestamp = LocalDateTime.now();
    }
}
