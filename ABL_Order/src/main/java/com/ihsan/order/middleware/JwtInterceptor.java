package com.ihsan.order.middleware;

import com.ihsan.order.security.JwtUtil;
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
        String username = jwtUtil.extractUsername(token);

        // PUT dan DELETE hanya boleh dilakukan oleh ADMIN
        if (("PUT".equalsIgnoreCase(method) || "DELETE".equalsIgnoreCase(method)) && !"ADMIN".equalsIgnoreCase(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Admin role required to modify or delete order\"}");
            return false;
        }

        // POST dan GET diizinkan untuk CUSTOMER dan ADMIN
        if (!"CUSTOMER".equalsIgnoreCase(role) && !"ADMIN".equalsIgnoreCase(role)) {
            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access denied: insufficient permissions\"}");
            return false;
        }

        request.setAttribute("username", username);
        request.setAttribute("role", role);
        return true;
    }
}
