package com.student.demo.security;

import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.oauth2.server.resource.web.BearerTokenResolver;
import org.springframework.stereotype.Component;

@Component
public class CookieBearerTokenResolver implements BearerTokenResolver {

    private static final org.slf4j.Logger logger = org.slf4j.LoggerFactory.getLogger(CookieBearerTokenResolver.class);

    @Override
    public String resolve(HttpServletRequest request) {
        logger.info("Resolving token for request to URI: {} with query: {}", request.getRequestURI(), request.getQueryString());
        if (request.getCookies() != null) {
            for (Cookie cookie : request.getCookies()) {
                if ("access_token".equals(cookie.getName())) {
                    logger.info("Found access_token cookie!");
                    return cookie.getValue();
                }
            }
        }
        
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            logger.info("Found Bearer token in Header!");
            return bearerToken.substring(7);
        }
        
        logger.warn("No token found in either Cookie or Header for request to: {}", request.getRequestURI());
        return null;
    }

}
