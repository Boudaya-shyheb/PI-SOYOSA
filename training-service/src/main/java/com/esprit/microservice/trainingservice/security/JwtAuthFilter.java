package com.esprit.microservice.trainingservice.security;

import com.esprit.microservice.trainingservice.entities.Role;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(
        @NonNull HttpServletRequest request,
        @NonNull HttpServletResponse response,
        @NonNull FilterChain filterChain
    ) throws ServletException, IOException {

        final String authHeader = request.getHeader("Authorization");

        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        try {
            final String jwt = authHeader.substring(7);
            final String email = jwtUtil.extractUsername(jwt);

            if (email != null && SecurityContextHolder.getContext().getAuthentication() == null) {
                // Extract role and userId directly from JWT (Stateless)
                Object rawRoleObj = jwtUtil.extractClaim(jwt, claims -> claims.get("role"));
                Object userIdObj = jwtUtil.extractClaim(jwt, claims -> claims.get("userId"));

                // Normalize role
                Role role = Role.STUDENT; // default
                if (rawRoleObj != null) {
                    String normalizedRole = rawRoleObj.toString().toUpperCase();
                    if (normalizedRole.startsWith("ROLE_")) {
                        normalizedRole = normalizedRole.substring(5);
                    }
                    if ("TEACHER".equals(normalizedRole)) normalizedRole = "TUTOR";
                    try {
                        role = Role.valueOf(normalizedRole);
                    } catch (IllegalArgumentException ignored) {}
                }

                Long userId = null;
                if (userIdObj != null) {
                    try {
                        userId = Long.parseLong(userIdObj.toString());
                    } catch (NumberFormatException ignored) {}
                }

                SecurityUser securityUser = SecurityUser.builder()
                        .id(userId)
                        .email(email)
                        .role(role)
                        .build();

                UsernamePasswordAuthenticationToken authToken =
                    new UsernamePasswordAuthenticationToken(
                        securityUser, null, securityUser.getAuthorities()
                    );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        } catch (Exception e) {
            // Log minimally or ignore if token is merely invalid/expired for unauthenticated routes
        }

        filterChain.doFilter(request, response);
    }
}
