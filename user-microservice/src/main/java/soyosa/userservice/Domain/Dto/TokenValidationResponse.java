package soyosa.userservice.Domain.Dto;

import com.fasterxml.jackson.annotation.JsonInclude;

/**
 * Response DTO for token validation endpoint
 */
@JsonInclude(JsonInclude.Include.NON_NULL)
public class TokenValidationResponse {

    private boolean valid;
    private String userId;
    private String role;
    private Long expiresAt;

    public TokenValidationResponse() {
    }

    public TokenValidationResponse(boolean valid, String userId, String role, Long expiresAt) {
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
