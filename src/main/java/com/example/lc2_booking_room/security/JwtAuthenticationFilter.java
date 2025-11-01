package com.example.lc2_booking_room.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.Cookie;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import com.example.lc2_booking_room.service.login.JwtService;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    /** ข้ามเฉพาะคำขอที่ไม่ต้องตรวจ JWT จริง ๆ */
    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        String method = request.getMethod();

        // ข้าม preflight
        if ("OPTIONS".equalsIgnoreCase(method)) return true;

        // ปล่อยให้ POST ใต้ /auth/** (เช่น /auth/tucheck, /auth/request-otp, /auth/verify-otp) ไม่ต้องตรวจ JWT
        if ("POST".equalsIgnoreCase(method) && path.startsWith("/auth/")) return true;

        // ปล่อย health
        if ("GET".equalsIgnoreCase(method) && "/actuator/health".equals(path)) return true;

        // นอกนั้นให้ฟิลเตอร์ทำงาน (รวมถึง GET /auth/me)
        return false;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        String token = resolveToken(req);

        if (token != null && jwtService.validate(token)) {
            String email = jwtService.getEmail(token);        // subject = email
            String role  = jwtService.getRole(token);
            if (role == null || role.isBlank()) role = "USER";

            UsernamePasswordAuthenticationToken auth =
                    new UsernamePasswordAuthenticationToken(
                            email,
                            null,
                            List.of(new SimpleGrantedAuthority("ROLE_" + role)));

            SecurityContextHolder.getContext().setAuthentication(auth);
        }

        chain.doFilter(req, res);
    }

    /** ดึง token จาก Header Bearer หรือคุกกี้ AUTH */
    private String resolveToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return authHeader.substring(7).trim();
        }
        Cookie[] cookies = req.getCookies();
        if (cookies != null) {
            for (Cookie c : cookies) {
                if ("AUTH".equals(c.getName())) {
                    return c.getValue();
                }
            }
        }
        return null;
    }
}
