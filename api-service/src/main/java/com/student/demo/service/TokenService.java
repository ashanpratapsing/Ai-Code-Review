package com.student.demo.service;

import com.student.demo.entity.RefreshToken;
import com.student.demo.entity.User;
import com.student.demo.repository.RefreshTokenRepository;
import org.springframework.security.oauth2.jwt.JwtClaimsSet;
import org.springframework.security.oauth2.jwt.JwtEncoder;
import org.springframework.security.oauth2.jwt.JwtEncoderParameters;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.HexFormat;
import java.util.UUID;

@Service
public class TokenService {

    private final JwtEncoder jwtEncoder;
    private final RefreshTokenRepository refreshTokenRepository;

    public TokenService(JwtEncoder jwtEncoder, RefreshTokenRepository refreshTokenRepository) {
        this.jwtEncoder = jwtEncoder;
        this.refreshTokenRepository = refreshTokenRepository;
    }

    public String generateAccessToken(User user) {
        Instant now = Instant.now();
        String role = user.getRole() != null ? user.getRole() : "USER";

        JwtClaimsSet claims = JwtClaimsSet.builder()
                .issuer("ai-code-review")
                .issuedAt(now)
                .expiresAt(now.plus(15, ChronoUnit.MINUTES))
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("scope", role)
                .build();

        return jwtEncoder.encode(JwtEncoderParameters.from(claims)).getTokenValue();
    }

    @Transactional
    public String generateRefreshToken(User user) {
        String rawToken = UUID.randomUUID() + "." + UUID.randomUUID();
        String tokenHash = sha256(rawToken);

        RefreshToken refreshToken = new RefreshToken();
        refreshToken.setUser(user);
        refreshToken.setTokenHash(tokenHash);
        refreshToken.setExpiresAt(LocalDateTime.now().plusDays(7));
        refreshTokenRepository.save(refreshToken);

        return rawToken;
    }

    public String hashRefreshToken(String rawToken) {
        return sha256(rawToken);
    }

    @Transactional
    public void revokeTokensForUser(User user) {
        refreshTokenRepository.deleteByUser(user);
    }

    private String sha256(String value) {
        try {
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(value.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(hash);
        } catch (NoSuchAlgorithmException e) {
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }
}
