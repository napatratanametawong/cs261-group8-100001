package com.example.lc2_booking_room.controller;

import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/user")
public class UserController {

    @GetMapping("/profile")
    public Map<String, Object> profile(Authentication auth) {
        if (auth == null || !auth.isAuthenticated()) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED);
        }
        String email = String.valueOf(auth.getPrincipal());
        String role = auth.getAuthorities().stream()
                .map(GrantedAuthority::getAuthority)
                .findFirst().orElse("");
        if (role.startsWith("ROLE_")) {
            role = role.substring("ROLE_".length());
        }
        Map<String, Object> resp = new HashMap<>();
        resp.put("email", email);
        resp.put("role", role);
        resp.put("username", email); // fallback if needed
        resp.put("displayName", null); // UI falls back to email
        return resp;
    }
}

