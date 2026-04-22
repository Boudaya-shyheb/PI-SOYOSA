package soyosa.userservice.Controller;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureException;
import io.jsonwebtoken.io.Decoders;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import soyosa.userservice.Domain.Dto.TokenValidationResponse;

/**
 * Internal API for other services to validate JWT tokens and extract user information.
 * This is used for inter-service communication (e.g., courses service validating user tokens).
 */
@Tag(name = "Token Validation")
@RestController
@RequestMapping("/api/internal/token")
public class TokenValidationController {

    @Value("${jwt.secret}")
    private String jwtSecret;

    /**
     * Validate a JWT token and return user information
     * @param authHeader Authorization header (Bearer <token>)
     * @return Token validation response with userId and role
     */
    @GetMapping("/validate")
    public ResponseEntity<TokenValidationResponse> validateToken(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(jwtSecret))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String userId = claims.getSubject();
            String role = (String) claims.get("role");
            if (role == null) {
                role = (String) claims.get("authorities");
            }

            TokenValidationResponse response = new TokenValidationResponse(
                    true,
                    userId,
                    role,
                    claims.getExpiration().getTime()
            );
            return ResponseEntity.ok(response);
        } catch (SignatureException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, null, null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TokenValidationResponse(false, null, null, null));
        }
    }

    /**
     * Get user role from a JWT token
     * @param authHeader Authorization header (Bearer <token>)
     * @return Role name
     */
    @GetMapping("/role")
    public ResponseEntity<String> getRole(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(jwtSecret))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            String role = (String) claims.get("role");
            if (role == null) {
                role = (String) claims.get("authorities");
            }

            return ResponseEntity.ok(role);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }

    /**
     * Get userId from a JWT token
     * @param authHeader Authorization header (Bearer <token>)
     * @return User ID
     */
    @GetMapping("/user-id")
    public ResponseEntity<String> getUserId(@RequestHeader("Authorization") String authHeader) {
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }

        String token = authHeader.substring(7);
        try {
                Claims claims = Jwts.parserBuilder()
                    .setSigningKey(Decoders.BASE64.decode(jwtSecret))
                    .build()
                    .parseClaimsJws(token)
                    .getBody();

            return ResponseEntity.ok(claims.getSubject());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
    }
}
