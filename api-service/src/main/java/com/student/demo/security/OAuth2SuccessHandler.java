package com.student.demo.security;

import com.student.demo.entity.User;
import com.student.demo.repository.UserRepository;
import com.student.demo.service.TokenService;
import org.springframework.security.crypto.password.PasswordEncoder;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.SimpleUrlAuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class OAuth2SuccessHandler extends SimpleUrlAuthenticationSuccessHandler {

    private final TokenService tokenService;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @org.springframework.beans.factory.annotation.Value("${app.frontend.url}")
    private String frontendUrl;

    public OAuth2SuccessHandler(TokenService tokenService, UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.tokenService = tokenService;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response, Authentication authentication) throws IOException, ServletException {
        OAuth2User oAuth2User = (OAuth2User) authentication.getPrincipal();
        String email = oAuth2User.getAttribute("email");
        
        if (email == null) {
            email = oAuth2User.getAttribute("login") + "@github.com";
        }

        final String finalEmail = email;
        User user = userRepository.findByEmail(email).orElseGet(() -> {
            User newUser = new User();
            newUser.setEmail(finalEmail);
            newUser.setName(oAuth2User.getAttribute("name"));
            newUser.setRole("USER");
            newUser.setPassword(passwordEncoder.encode("OAUTH-" + java.util.UUID.randomUUID()));
            return userRepository.save(newUser);
        });

        String accessToken = tokenService.generateAccessToken(user);
        String refreshToken = tokenService.generateRefreshToken(user);

        Cookie accessCookie = new Cookie("access_token", accessToken);
        accessCookie.setHttpOnly(true);
        accessCookie.setPath("/");
        accessCookie.setMaxAge(15 * 60); // 15 mins

        Cookie refreshCookie = new Cookie("refresh_token", refreshToken);
        refreshCookie.setHttpOnly(true);
        refreshCookie.setPath("/auth/refresh"); // Restrict to refresh endpoint
        refreshCookie.setMaxAge(7 * 24 * 60 * 60); // 7 days

        response.addCookie(accessCookie);
        response.addCookie(refreshCookie);

        getRedirectStrategy().sendRedirect(request, response, frontendUrl + "/dashboard");
    }
}
