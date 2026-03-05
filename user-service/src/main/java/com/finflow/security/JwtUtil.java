package com.finflow.security;

import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import java.util.UUID;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * JwtUtil — utility class untuk generate dan validasi JWT token
 *
 * Generate token → saat user login berhasil
 * Validasi token → saat user hit endpoint protected
 * Extract claims → ambil data dari dalam token (userId, email, dll)
 */
@Slf4j
@Component
public class JwtUtil {

    @Value("${jwt.secret}")
    private String secret;

    @Value("${jwt.expiration}")
    private long expiration;

    // -------------------------------------------------------
    // Generate token dari data user
    // -------------------------------------------------------
    public String generateToken(UUID userId, String email) {
        Date now = new Date();
        Date expiredAt = new Date(now.getTime() + expiration);
        return Jwts.builder()
                .subject(userId.toString())
                .claim("email", email)
                .issuedAt(now)
                .expiration(expiredAt)
                .signWith(getSecretKey())
                .compact();
    }

    // -------------------------------------------------------
    // Validasi token — return true jika valid, false jika tidak
    // -------------------------------------------------------
    public boolean isTokenValid(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            log.warn("Invalid JWT Token: {}", e.getMessage());
            return false;
        }
    }

    // -------------------------------------------------------
    // Extract userId dari token
    // -------------------------------------------------------
    public UUID extractUserId(String token) {
        String subject = getClaims(token).getSubject();
        return UUID.fromString(subject);
    }

    // -------------------------------------------------------
    // Extract email dari token
    // -------------------------------------------------------
    public String extractEmail(String token) {
        return getClaims(token).get("email", String.class);
    }

    // -------------------------------------------------------
    // Ambil waktu expired token untuk ditampilkan di response
    // -------------------------------------------------------
    public LocalDateTime extractExpiredAt(String token) {
        Date expiredAt = getClaims(token).getExpiration();
        return expiredAt.toInstant()
                .atZone(ZoneId.systemDefault())
                .toLocalDateTime();
    }

    // -------------------------------------------------------
    // Helper: parse dan ambil semua claims dari token
    // -------------------------------------------------------
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSecretKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    // -------------------------------------------------------
    // Helper: buat SecretKey dari string secret
    // -------------------------------------------------------
    private SecretKey getSecretKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
