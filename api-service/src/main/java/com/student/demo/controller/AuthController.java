package com.student.demo.controller;

import com.student.demo.dto.UserDTO;
import com.student.demo.entity.RefreshToken;
import com.student.demo.entity.User;
import com.student.demo.repository.RefreshTokenRepository;
import com.student.demo.repository.UserRepository;
import com.student.demo.security.SecurityUtil;
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
    private final SecurityUtil securityUtil;

    public AuthController(UserService userService,
                          UserRepository userRepository,
                          TokenService tokenService,
                          RefreshTokenRepository refreshTokenRepository,
                          SecurityUtil securityUtil) {
        this.userService = userService;
        this.userRepository = userRepository;
        this.tokenService = tokenService;
        this.refreshTokenRepository = refreshTokenRepository;
        this.securityUtil = securityUtil;
    }

    @PostMapping("/signup")
    public UserDTO register(@RequestBody User user) {
        return UserDTO.from(userService.register(user));
    }

    @PostMapping("/login")
    public Map<String, Object> login(@RequestBody User user, HttpServletResponse response) {
        if (user.getEmail() == null || user.getEmail().isBlank()) {
            throw new IllegalArgumentException("Email is required");
        }
        if (user.getPassword() == null || user.getPassword().isBlank()) {
            throw new IllegalArgumentException("Password is required");
        }

        User existingUser = userRepository.findByEmail(user.getEmail())
                .orElseThrow(() -> new IllegalArgumentException("Invalid email or password"));

        if (!userService.matchesPassword(existingUser, user.getPassword())) {
            throw new IllegalArgumentException("Invalid email or password");
        }

        if (existingUser.getPassword() != null && !existingUser.getPassword().startsWith("$2")) {
            userService.upgradeLegacyPassword(existingUser, user.getPassword());
        }

        setAuthCookies(response, existingUser);

        Map<String, Object> responseBody = new HashMap<>();
        responseBody.put("user", UserDTO.from(existingUser));
        return responseBody;
    }

    @PostMapping("/refresh")
    public Map<String, Object> refresh(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token");
        if (refreshToken == null) {
            throw new IllegalArgumentException("Refresh token missing");
        }

        String tokenHash = tokenService.hashRefreshToken(refreshToken);
        RefreshToken tokenEntity = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new IllegalArgumentException("Invalid refresh token"));

        if (tokenEntity.isRevoked() || tokenEntity.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new IllegalArgumentException("Refresh token expired or revoked");
        }

        tokenEntity.setRevoked(true);
        refreshTokenRepository.save(tokenEntity);

        setAuthCookies(response, tokenEntity.getUser());

        return Map.of("message", "Token refreshed successfully");
    }

    @PostMapping("/logout")
    public Map<String, String> logout(HttpServletRequest request, HttpServletResponse response) {
        String refreshToken = extractCookie(request, "refresh_token");
        if (refreshToken != null) {
            String tokenHash = tokenService.hashRefreshToken(refreshToken);
            refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token ->
                    tokenService.revokeTokensForUser(token.getUser()));
        }

        clearCookie(response, "access_token", "/");
        clearCookie(response, "refresh_token", "/auth/refresh");

        return Map.of("message", "Logged out successfully");
    }

    @GetMapping("/me")
    public UserDTO getCurrentUser() {
        return UserDTO.from(securityUtil.requireCurrentUser());
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
        accessCookie.setMaxAge(15 * 60);

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/auth/refresh");
        refreshCookie.setMaxAge(7 * 24 * 60 * 60);

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);
    }

    private String extractCookie(HttpServletRequest request, String name) {
        if (request.getCookies() == null) {
            return null;
        }
        for (Cookie cookie : request.getCookies()) {
            if (name.equals(cookie.getName())) {
                return cookie.getValue();
            }
        }
        return null;
    }

    private void clearCookie(HttpServletResponse response, String name, String path) {
        Cookie cookie = new Cookie(name, "");
        cookie.setPath(path);
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}
