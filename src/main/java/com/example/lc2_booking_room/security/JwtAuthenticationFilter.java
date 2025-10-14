package com.example.lc2_booking_room.security;

import com.example.lc2_booking_room.service.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtService jwtService;

    public JwtAuthenticationFilter(JwtService jwtService) {
        this.jwtService = jwtService;
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String ctx = req.getContextPath() == null ? "" : req.getContextPath();
        String p = req.getRequestURI().substring(ctx.length());


        return "OPTIONS".equalsIgnoreCase(req.getMethod())
                || p.equals("/") || p.startsWith("/error")
                || p.startsWith("/login/") || p.startsWith("/auth/")
                || p.startsWith("/css/") || p.startsWith("/js/")
                || p.startsWith("/images/") || p.startsWith("/webjars/")
                || p.endsWith(".html") || p.endsWith(".css") || p.endsWith(".js")
                || p.equals("/favicon.ico");
    }

    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {
        // ถ้ามี Authentication อยู่แล้ว (จาก filter ก่อนหน้า) ก็ข้าม
        if (SecurityContextHolder.getContext().getAuthentication() != null) {
            chain.doFilter(req, res);
            return;
        }

        String token = resolveToken(req);

        if (token != null && jwtService.validate(token)) {
            var username = jwtService.getSubject(token);
            var role = jwtService.getRole(token); // ควรคืนเช่น USER / BUILDING_ADMIN

            var auth = new UsernamePasswordAuthenticationToken(
                    username, null, List.of(new SimpleGrantedAuthority("ROLE_" + role)));
            SecurityContextHolder.getContext().setAuthentication(auth);
        } else {
            SecurityContextHolder.clearContext();
        }

        chain.doFilter(req, res);
    }

    private String resolveToken(HttpServletRequest req) {
        String authHeader = req.getHeader("Authorization");
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            return authHeader.substring(7);
        }
        if (req.getCookies() != null) {
            for (var cookie : req.getCookies()) {
                if ("AUTH".equals(cookie.getName())) {
                    return cookie.getValue();
                }
            }
        }
        return null;
    }
}

