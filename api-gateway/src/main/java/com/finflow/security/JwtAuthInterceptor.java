package com.finflow.security;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.util.List;

/**
 * JwtAuthInterceptor — intercept setiap request di API Gateway
 * Pakai HandlerInterceptor karena gateway-server-webmvc berbasis MVC
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtAuthInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    // Endpoint yang tidak butuh token
    private static final List<String> PUBLIC_PATHS = List.of(
            "/api/v1/users/register",
            "/api/v1/auth/login");

    @Override
    public boolean preHandle(HttpServletRequest request,
            HttpServletResponse response,
            Object handler) throws Exception {

        String path = request.getRequestURI();
        log.debug("Incoming request: {}", path);

        // 1. Public path → langsung lanjut
        if (isPublicPath(path)) {
            log.debug("Public path, skipping JWT: {}", path);
            return true;
        }

        // 2. Ambil Authorization header
        String authHeader = request.getHeader("Authorization");

        // 3. Cek format Bearer token
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            log.warn("Missing Authorization header: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"Unauthorized\"}");
            return false; // ← stop, tidak diteruskan ke service
        }

        // 4. Ambil token
        String token = authHeader.substring(7);

        // 5. Validasi token
        if (!jwtUtil.isTokenValid(token)) {
            log.warn("Invalid token for path: {}", path);
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.getWriter().write("{\"success\":false,\"message\":\"Invalid or expired token\"}");
            return false; // ← stop
        }

        // 6. Token valid → teruskan ke service
        log.debug("JWT valid, forwarding to service: {}", path);
        return true; // ← lanjut
    }

    private boolean isPublicPath(String path) {
        return PUBLIC_PATHS.stream().anyMatch(path::equals);
    }
}
