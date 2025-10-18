package com.example.lc2_booking_room.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpHeaders;
import org.springframework.security.web.AuthenticationEntryPoint;
import java.io.IOException;

public class SmartAuthEntryPoint implements AuthenticationEntryPoint {
    private final String loginPath;

    public SmartAuthEntryPoint(String loginPath) {
        this.loginPath = loginPath; 
    }

    @Override
    public void commence(HttpServletRequest req, HttpServletResponse res,
                         org.springframework.security.core.AuthenticationException e) throws IOException {
        String accept = req.getHeader(HttpHeaders.ACCEPT);
        String xhr = req.getHeader("X-Requested-With");

        boolean wantsHtml =
            (accept != null && accept.contains("text/html")) ||
            (xhr == null && !req.getRequestURI().startsWith("/api/"));

        if (wantsHtml) {
            res.setStatus(302);
            res.setHeader("Location", loginPath);
        } else {
            res.setStatus(401);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"Unauthorized\"}");
        }
    }
}
