package com.student.demo.controller;

import com.student.demo.entity.RefreshToken;
import com.student.demo.entity.User;
import com.student.demo.repository.RefreshTokenRepository;
import com.student.demo.repository.UserRepository;
import com.student.demo.service.TokenService;
import com.student.demo.service.UserService;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {

    private final UserService userService;
    private final UserRepository userRepository;
    private final TokenService tokenService;
    private final RefreshTokenRepository refreshTokenRepository;

    public AuthController(UserService userService, UserRepository userRepository, TokenService tokenService, RefreshTokenRepository refreshTokenRepository) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    @PostMapping("/signup")
    public User register(@RequestBody User user) {
        return userService.register(user);
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user, HttpServletResponse response) {

        if (user.getEmail() == null || user.getEmail().isEmpty()) {
            throw new RuntimeException("Email is required");
        }

        User existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new RuntimeException("User not found with email: " + user.getEmail()));

        if (!existingUser.getPassword().equals(user.getPassword())) {
            throw new RuntimeException("Invalid password");
        }

        setAuthCookies(response, existingUser);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user", existingUser);
        return responseBody;
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken == null) {
            throw new RuntimeException("Refresh token missing");
        }

        RefreshToken tokenEntity = refreshTokenRepository.findByTokenHash(refreshToken)
                .orElseThrow(() -> new RuntimeException("Invalid refresh token"));

        if (tokenEntity.isRevoked() || tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new RuntimeException("Refresh token expired or revoked");
        }

        // Rotate token
        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);

        setAuthCookies(response, tokenEntity.getUser());

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("message", "Token refreshed successfully");
        return responseBody;
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = null;
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("refresh_token".equals(cookie.getName())) {
                    refreshToken = cookie.getValue();
                }
            }
        }

        if (refreshToken != null) {
            refreshTokenRepository.findByTokenHash(refreshToken).ifPresent(token -> {
                tokenService.revokeTokensForUser(token.getUser());
            });
        }

        Cookie accessCookie = new Cookie("access_token", "");
        accessCookie.setPath("/");
        accessCookie.setMaxAge(0);
        response.addCookie(accessCookie);

        Cookie refreshCookie = new Cookie("refresh_token", "");
        refreshCookie.setPath("/auth/refresh");
        refreshCookie.setMaxAge(0);
        response.addCookie(refreshCookie);

        return Map.of("message", "Logged out successfully");
    }
    
    @GetMapping("/me")
    public User getCurrentUser(org.springframework.security.core.Authentication authentication) {
        if (authentication == null || authentication.getName() == null) {
             throw new RuntimeException("Not authenticated");
        }
        return userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new RuntimeException("User not found"));
    }

    @GetMapping("/verify")
    public Map<String, Object> verify(org.springframework.security.core.Authentication authentication) {
        Map<String, Object> details = new HashMap<>();
        if (authentication == null) {
            details.put("status", "NOT_AUTHENTICATED");
        } else {
            details.put("status", "AUTHENTICATED");
            details.put("name", authentication.getName());
            details.put("authorities", authentication.getAuthorities());
        }
        return details;
    }

    private void setAuthCookies(HttpServletResponse response, User user) {
        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60); // 15 mins

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/auth/refresh");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }
}
