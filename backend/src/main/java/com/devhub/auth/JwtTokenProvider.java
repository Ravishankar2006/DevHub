package com.devhub.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtTokenProvider {

    private final SecretKey key;
    private final long accessExpirationMs;
    private final long refreshExpirationMs;

    public JwtTokenProvider(
            @Value("${devhub.jwt.secret}") String secret,
            @Value("${devhub.jwt.access-expiration-ms}") long accessExpirationMs,
            @Value("${devhub.jwt.refresh-expiration-ms}") long refreshExpirationMs) {
        
        byte[] keyBytes;
        try {
            keyBytes = Decoders.BASE64.decode(secret);
        } catch (IllegalArgumentException e) {
            // Fall back to raw UTF-8 bytes if not valid Base64
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        // Fail fast on a weak secret rather than silently padding it -- a misconfigured
        // deployment should error loudly at startup, not run with a degraded key.
        if (keyBytes.length < 32) {
            throw new IllegalStateException(
                    "devhub.jwt.secret is too short: must decode to at least 32 bytes (256 bits) for HMAC-SHA256.");
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.accessExpirationMs = accessExpirationMs;
        this.refreshExpirationMs = refreshExpirationMs;
    }

    public String generateAccessToken(String email) {
        return generateToken(email, accessExpirationMs);
    }

    public String generateRefreshToken(String email) {
        return generateToken(email, refreshExpirationMs);
    }

    private String generateToken(String email, long expirationMs) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(email)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(key)
                .compact();
    }

    public String getEmailFromToken(String token) {
        Claims claims = Jwts.parser()
                .verifyWith(key)
                .build()
                .parseSignedClaims(token)
                .getPayload();

        return claims.getSubject();
    }

    public boolean validateToken(String token) {
        try {
            Jwts.parser().verifyWith(key).build().parseSignedClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }
}
