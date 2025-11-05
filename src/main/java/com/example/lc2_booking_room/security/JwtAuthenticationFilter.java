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
    protected boolean shouldNotFilter(HttpServletRequest req) {
        String p = req.getServletPath();
        if ("OPTIONS".equalsIgnoreCase(req.getMethod())) return true;
        return p.startsWith("/auth/")
            || p.equals("/error")
            || p.startsWith("/actuator");
    }
    // log.debug("JWT shouldNotFilter? path={}, result={}", p, result);


    @Override
    protected void doFilterInternal(HttpServletRequest req, HttpServletResponse res, FilterChain chain)
            throws ServletException, IOException {

        try {
            // 1) Read token from Authorization: Bearer ... or cookie AUTH
            String token = resolveToken(req);
            if (token == null || token.isBlank()) {
                chain.doFilter(req, res);
                return;
            }

            // 2) Ask JwtService for the username (JwtService will validate/parse internally)
            String username = null;
            try {
                username = jwtService.getUsername(token); // throws if invalid/expired
            } catch (Exception e) {
                // Token invalid/expired → leave anonymous and continue
                chain.doFilter(req, res);
                return;
            }

            // 3) If username present, create an Authentication and put it in the context
            if (username != null && !username.isBlank()
                    && SecurityContextHolder.getContext().getAuthentication() == null) {

                // If you don’t enforce roles on this endpoint, empty authorities are fine.
                var authorities = List.<SimpleGrantedAuthority>of();

                var authentication =
                        new UsernamePasswordAuthenticationToken(username, null, authorities);

                SecurityContextHolder.getContext().setAuthentication(authentication);
            }

        } finally {
            // 4) Always continue down the chain
            chain.doFilter(req, res);
        }
    }


    // Helper: Accept "Authorization: Bearer ..." or cookie "AUTH"
    private String resolveToken(HttpServletRequest req) {
        String h = req.getHeader("Authorization");
        if (h != null && h.regionMatches(true, 0, "Bearer ", 0, 7)) {
            return h.substring(7).trim();
        }
        if (req.getCookies() != null) {
            for (Cookie c : req.getCookies()) {
                if ("AUTH".equals(c.getName())) return c.getValue();
            }
        }
        return null;
    }

}
