// com/example/lc2_booking_room/service/JwtService.java
package com.example.lc2_booking_room.service;

import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.security.Key;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService {

    private final Key key;
    private final long expiryMillis;

    public JwtService(@Value("${app.jwt.secret}") String secret,
                      @Value("${app.jwt.expiry-seconds}") long expirySeconds) {
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiryMillis = expirySeconds * 1000L;
    }

    public String issueToken(String email, String role, String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of("role", role, "username", username == null ? "" : username))
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
