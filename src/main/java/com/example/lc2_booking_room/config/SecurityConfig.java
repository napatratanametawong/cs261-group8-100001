package com.example.lc2_booking_room.config;

import com.example.lc2_booking_room.security.JwtAuthenticationFilter;
import com.example.lc2_booking_room.security.SmartAuthEntryPoint;
import com.example.lc2_booking_room.service.login.JwtService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter(JwtService jwtService) {
        return new JwtAuthenticationFilter(jwtService);
    }

    @Bean
    public SmartAuthEntryPoint smartAuthEntryPoint() {
        // เปลี่ยน path ให้ตรงกับไฟล์จริง
        return new SmartAuthEntryPoint("/login/pages/loginPage.html");
    }

    @Bean
    SecurityFilterChain api(HttpSecurity http,
            JwtAuthenticationFilter jwtFilter,
            SmartAuthEntryPoint smartEntryPoint) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .cors(Customizer.withDefaults())
                .sessionManagement(sm -> sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // ✅ หน้า public
                        .requestMatchers(HttpMethod.GET,
                                "/",
                                "/login/**",
                                "/styles/**", "/scripts/**", "/webjars/**")
                        .permitAll()

                        // ✅ auth endpoints และ health
                        .requestMatchers(HttpMethod.POST, "/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/actuator/health").permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()

                        // ✅ Protected
                        .requestMatchers(HttpMethod.GET, "/api/rooms/**").hasAnyRole("USER", "BUILDING_ADMIN")
                        .requestMatchers("/bookingRoom/**").hasRole("USER")
                        .requestMatchers("/admin/**").hasRole("BUILDING_ADMIN")

                        // resorce
                        .requestMatchers("/resource/**", "/global-head.js").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(smartEntryPoint)
                        .accessDeniedHandler((req, res, e) -> {
                            res.setStatus(403);
                            res.setContentType("application/json;charset=UTF-8");
                            res.getWriter().write("{\"error\":\"Forbidden\"}");
                        }))
                .addFilterBefore(jwtFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration c = new CorsConfiguration();
        c.setAllowedOriginPatterns(List.of("*"));
        c.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"));
        c.setAllowedHeaders(List.of("*"));
        c.setAllowCredentials(true);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", c);
        return source;
    }
}
