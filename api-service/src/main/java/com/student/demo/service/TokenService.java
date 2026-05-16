package com.student.demo.service;

import com.student.demo.entity.RefreshToken;
import com.student.demo.entity.User;
import com.student.demo.repository.RefreshTokenRepository;
import com.student.demo.repository.UserRepository;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.UUID;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;
    private final UserRepository userRepository;

    public TokenService(JwtEncoder jwtEncoder, RefreshTokenRepository refreshTokenRepository, UserRepository userRepository) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
        this.userRepository = userRepository;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        String role = user.getRole() != null ? user.getRole() : "USER";
        
        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("self")
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES)) // 15 min expiry for security
                .subject(user.getEmail())
                .claim("scope", role)
                .build();

        return this.jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Transactional
    public String generateRefreshToken(User user) {
        String tokenHash = UUID.randomUUID().toString() + "-" + UUID.randomUUID().toString();
        
        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7)); // 7 days expiry
        
        refreshTokenRepository.save(refreshToken);
        
        return tokenHash;
    }
    
    @Transactional
    public void revokeTokensForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }
}
