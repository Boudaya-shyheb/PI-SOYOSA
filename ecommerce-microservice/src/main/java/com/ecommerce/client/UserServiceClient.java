package com.ecommerce.client;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(name = "user-service", url = "${user-service.url:http://localhost:8070}")
public interface UserServiceClient {

    @GetMapping("/api/internal/token/validate")
    TokenValidationDto validateToken(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/api/internal/token/role")
    String getUserRole(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/api/internal/token/user-id")
    String getUserId(@RequestHeader("Authorization") String authHeader);

    @GetMapping("/api/internal/user/{userId}")
    UserInfoDto getUserInfo(@PathVariable("userId") String userId);

    class UserInfoDto {
        private String userId;
        private String username;
        private String email;
        private String firstName;
        private String lastName;

        public UserInfoDto() {
        }

        public UserInfoDto(String userId, String username, String email, String firstName, String lastName) {
            this.userId = userId;
            this.username = username;
            this.email = email;
            this.firstName = firstName;
            this.lastName = lastName;
        }

        public String getUserId() {
            return userId;
        }

        public void setUserId(String userId) {
            this.userId = userId;
        }

        public String getUsername() {
            return username;
        }

        public void setUsername(String username) {
            this.username = username;
        }

        public String getEmail() {
            return email;
        }

        public void setEmail(String email) {
            this.email = email;
        }

        public String getFirstName() {
            return firstName;
        }

        public void setFirstName(String firstName) {
            this.firstName = firstName;
        }

        public String getLastName() {
            return lastName;
        }

        public void setLastName(String lastName) {
            this.lastName = lastName;
        }

        public String getDisplayName() {
            if (firstName != null && lastName != null) {
                return firstName + " " + lastName;
            }
            return username;
        }
    }

    class TokenValidationDto {
        private boolean valid;
        private String userId;
        private String role;
        private Long expiresAt;

        public TokenValidationDto() {
        }

        public TokenValidationDto(boolean valid, String userId, String role, Long expiresAt) {
            this.valid = valid;
            this.userId = userId;
            this.role = role;
            this.expiresAt = expiresAt;
        }

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
