package com.student.demo.config;

import com.student.demo.security.OAuth2SuccessHandler;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.beans.factory.annotation.Value;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Value("${app.frontend.url:http://localhost:5173}")
    private String frontendUrl;

    private final OAuth2SuccessHandler successHandler;
    private final org.springframework.security.oauth2.server.resource.web.BearerTokenResolver bearerTokenResolver;

    public SecurityConfig(OAuth2SuccessHandler successHandler, org.springframework.security.oauth2.server.resource.web.BearerTokenResolver bearerTokenResolver) {
        this.successHandler = successHandler;
        this.bearerTokenResolver = bearerTokenResolver;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))
            .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/auth/**", "/actuator/health", "/actuator/info", "/swagger-ui/**", "/v3/api-docs/**", "/error", "/").permitAll()
                .requestMatchers("/debug/**").denyAll()
                .anyRequest().authenticated()
            )
            .oauth2Login(oauth2 -> oauth2.successHandler(successHandler))
            .oauth2ResourceServer(oauth2 -> oauth2
                .bearerTokenResolver(bearerTokenResolver)
                .jwt(Customizer.withDefaults()));

        return http.build();
    }

    @Bean
    public UrlBasedCorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        if (frontendUrl != null && frontendUrl.contains(",")) {
            config.setAllowedOrigins(java.util.Arrays.asList(frontendUrl.split(",")));
        } else {
            config.setAllowedOrigins(List.of(frontendUrl));
        }
        config.setAllowedHeaders(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
