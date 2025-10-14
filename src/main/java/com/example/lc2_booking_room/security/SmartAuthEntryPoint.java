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

        String ctx = req.getContextPath() == null ? "" : req.getContextPath();
        String uri = req.getRequestURI();
        String target = ctx + loginPath;

        // --- กันลูป: ถ้าอยู่หน้า login หรือ /error/* อยู่แล้ว อย่า redirect ---
        if (uri.equals(target) || uri.startsWith(ctx + "/error")) {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"Unauthorized\"}");
            return;
        }

        boolean isApi = uri.startsWith(ctx + "/api/");
        String accept = req.getHeader(HttpHeaders.ACCEPT);
        String xhr = req.getHeader("X-Requested-With");
        boolean wantsHtml =
                !isApi && (
                    (accept != null && accept.contains("text/html")) ||
                    (xhr == null)
                );

        if (wantsHtml) {
            res.setStatus(HttpServletResponse.SC_FOUND); // 302
            res.setHeader("Location", target);
        } else {
            res.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            res.setContentType("application/json;charset=UTF-8");
            res.getWriter().write("{\"error\":\"Unauthorized\"}");
        }
    }
}
