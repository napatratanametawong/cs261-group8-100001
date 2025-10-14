package com.example.lc2_booking_room.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;
    private final long expiryMillis;

    public JwtService(
            @Value("${app.jwt.secret}") String secret,
            @Value("${app.jwt.expiry-seconds:86400}") long expirySeconds
    ) {
        if (secret == null || secret.isBlank()) {
            throw new IllegalStateException("app.jwt.secret is missing (ENV APP_JWT_SECRET).");
        }

        byte[] keyBytes = null;

        try {
            keyBytes = io.jsonwebtoken.io.Decoders.BASE64URL.decode(secret);
        } catch (RuntimeException ignore) { /* try next */ }
        if (keyBytes == null) {
            try {
                keyBytes = io.jsonwebtoken.io.Decoders.BASE64.decode(secret);
            } catch (RuntimeException ignore) { /* try next */ }
        }
        
        if (keyBytes == null) {
            keyBytes = secret.getBytes(StandardCharsets.UTF_8);
        }

        if (keyBytes.length < 32) { // 256 บิตขั้นต่ำสำหรับ HS256
            throw new IllegalStateException(
                "JWT key too short: " + keyBytes.length + " bytes. Need >= 32 bytes (256 bits). " +
                "Generate a new secret (Base64 48 bytes is good)."
            );
        }

        this.key = Keys.hmacShaKeyFor(keyBytes);
        this.expiryMillis = Math.multiplyExact(expirySeconds, 1000L);
    }

    public String issueToken(String email, String role, String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of(
                        "role", role,
                        "username", username == null ? "" : username
                ))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiryMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    public String getSubject(String token) {
        return getAllClaims(token).getSubject();
    }

    public String getRole(String token) {
        Object r = getAllClaims(token).get("role");
        return r == null ? "USER" : String.valueOf(r);
    }

    private Claims getAllClaims(String token) {
        return Jwts.parserBuilder().setSigningKey(key).build()
                .parseClaimsJws(token).getBody();
    }
}
