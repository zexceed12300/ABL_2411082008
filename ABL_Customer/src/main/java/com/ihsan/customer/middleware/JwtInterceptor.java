package com.ihsan.customer.middleware;

import com.ihsan.customer.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String method = request.getMethod();
        if ("OPTIONS".equalsIgnoreCase(method)) {
            return true;
        }

        // Operasi baca (GET) diizinkan publik agar OrderService dapat mengambil data customer via DiscoveryClient
        if ("GET".equalsIgnoreCase(method)) {
            return true;
        }

        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Missing or invalid Authorization header\"}");
            return false;
        }

        String token = authHeader.substring(7).trim();
        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Token is invalid or expired\"}");
            return false;
        }

        String role = jwtUtil.extractRole(token);

        // Hapus customer hanya boleh oleh ADMIN
        if ("DELETE".equalsIgnoreCase(method) && !"ADMIN".equalsIgnoreCase(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Admin role required to delete customer\"}");
            return false;
        }

        request.setAttribute("username", jwtUtil.extractUsername(token));
        request.setAttribute("role", role);
        return true;
    }
}
