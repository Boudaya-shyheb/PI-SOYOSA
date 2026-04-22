package com.ecommerce.tracking.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8070}")
public interface UserServiceClient {

    @GetMapping("/api/internal/token/validate")
    TokenValidationDto validateToken(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/api/internal/token/role")
    String getUserRole(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/api/internal/token/user-id")
    String getUserId(@RequestHeader("Authorization") String authHeader);

    class TokenValidationDto {
        private boolean valid;
        private String userId;
        private String role;
        private Long expiresAt;

        public boolean isValid() {
            return valid;
        }

        public void setValid(boolean valid) {
            this.valid = valid;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getRole() {
            return role;
        }

        public void setRole(String role) {
            this.role = role;
        }

        public Long getExpiresAt() {
            return expiresAt;
        }

        public void setExpiresAt(Long expiresAt) {
            this.expiresAt = expiresAt;
        }
    }
}
