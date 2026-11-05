package com.ihsan.order.query.middleware;

import com.ihsan.order.query.utils.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
public class JwtInterceptor implements HandlerInterceptor {

    private final JwtUtil jwtUtil;

    public JwtInterceptor(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }

        if (!(handler instanceof HandlerMethod handlerMethod)) {
            return true;
        }

        RequireRole requireRole = handlerMethod.getMethodAnnotation(RequireRole.class);
        if (requireRole == null) {
            requireRole = handlerMethod.getBeanType().getAnnotation(RequireRole.class);
        }

        String authHeader = request.getHeader("Authorization");
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7).trim()
                : null;

        // Endpoint tanpa @RequireRole bersifat publik
        if (requireRole == null) {
            if (token != null && jwtUtil.validateToken(token)) {
                request.setAttribute("email", jwtUtil.extractEmail(token));
                request.setAttribute("role", jwtUtil.extractRole(token));
            }
            return true;
        }

        // Endpoint memerlukan otentikasi
        if (token == null) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Missing or invalid Authorization header\"}");
            return false;
        }

        if (!jwtUtil.validateToken(token)) {
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Unauthorized\", \"message\": \"Token is invalid or expired\"}");
            return false;
        }

        String role = jwtUtil.extractRole(token);
        String email = jwtUtil.extractEmail(token);

        // Validasi role jika didefinisikan pada anotasi
        if (requireRole.value().length > 0) {
            boolean authorized = false;
            for (String allowedRole : requireRole.value()) {
                if (allowedRole.equalsIgnoreCase(role)) {
                    authorized = true;
                    break;
                }
            }
            if (!authorized) {
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json");
                response.getWriter().write("{\"error\": \"Forbidden\", \"message\": \"Access denied: insufficient permissions\"}");
                return false;
            }
        }

        request.setAttribute("email", email);
        request.setAttribute("role", role);
        return true;
    }
}
