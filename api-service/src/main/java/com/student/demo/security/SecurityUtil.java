package com.student.demo.security;

import com.student.demo.entity.User;
import com.student.demo.exception.UnauthorizedException;
import com.student.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;

@Component
public class SecurityUtil {

    private final UserRepository userRepository;

    public SecurityUtil(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    public User requireCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("Authentication required");
        }

        Long userId = extractUserId(authentication);
        if (userId != null) {
            return userRepository.findById(userId)
                    .orElseThrow(() -> new UnauthorizedException("User not found"));
        }

        String email = authentication.getName();
        return userRepository.findByEmail(email)
                .orElseThrow(() -> new UnauthorizedException("User not found"));
    }

    public Long requireCurrentUserId() {
        return requireCurrentUser().getId();
    }

    private Long extractUserId(Authentication authentication) {
        if (authentication instanceof JwtAuthenticationToken jwtAuth) {
            Jwt jwt = jwtAuth.getToken();
            Object claim = jwt.getClaim("userId");
            if (claim instanceof Number number) {
                return number.longValue();
            }
            if (claim instanceof String s) {
                try {
                    return Long.parseLong(s);
                } catch (NumberFormatException ignored) {
                    return null;
                }
            }
        }
        return null;
    }
}
