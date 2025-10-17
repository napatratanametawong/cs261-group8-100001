// src/main/java/com/example/lc2_booking_room/service/JwtService.java
package com.example.lc2_booking_room.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.SignatureAlgorithm;
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
        // หมายเหตุ: HS256 ต้องการ secret อย่างน้อย ~32 bytes
        this.key = Keys.hmacShaKeyFor(secret.getBytes());
        this.expiryMillis = expirySeconds * 1000L;
    }

    /** ออกโทเคน พร้อมฝัง role และ username ลงใน claims */
    public String issueToken(String email, String role, String username) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setSubject(email)
                .addClaims(Map.of(
                        "role", role == null ? "" : role,
                        "username", username == null ? "" : username
                ))
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + expiryMillis))
                .signWith(key, SignatureAlgorithm.HS256)
                .compact();
    }

    /** ตรวจความถูกต้องของโทเคน (ลายเซ็น/วันหมดอายุ) */
    public boolean validate(String token) {
        try {
            Jwts.parserBuilder().setSigningKey(key).build().parseClaimsJws(token);
            return true;
        } catch (JwtException e) {
            return false;
        }
    }

    /** ดึงอีเมล (เก็บไว้ที่ subject) */
    public String getEmail(String token) {
        return parseClaims(token).getSubject();
    }

    /** ดึง role จาก claims (ถ้าไม่มี ให้คืนค่าว่าง แล้วไป default ฝั่ง caller) */
    public String getRole(String token) {
        Object v = parseClaims(token).get("role");
        return v == null ? "" : String.valueOf(v);
    }

    /** ดึง username จาก claims */
    public String getUsername(String token) {
        Object v = parseClaims(token).get("username");
        return v == null ? "" : String.valueOf(v);
    }

    // ===== helper =====
    private Claims parseClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
}
