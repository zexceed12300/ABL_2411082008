package com.ihsan.auth.service;

import com.ihsan.auth.dto.AuthResponse;
import com.ihsan.auth.dto.LoginRequest;
import com.ihsan.auth.dto.RegisterRequest;
import com.ihsan.auth.dto.ValidateResponse;
import com.ihsan.auth.entity.User;
import com.ihsan.auth.repository.UserRepository;
import com.ihsan.auth.util.JwtUtil;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;

    public AuthService(UserRepository userRepository,
                       PasswordEncoder passwordEncoder,
                       JwtUtil jwtUtil) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.jwtUtil = jwtUtil;
    }

    public AuthResponse register(RegisterRequest request) {
        if (request.getUsername() == null || request.getUsername().isBlank() ||
                request.getPassword() == null || request.getPassword().isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username and password cannot be empty");
        }

        if (userRepository.existsByUsername(request.getUsername())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists");
        }

        String role = (request.getRole() != null && !request.getRole().isBlank())
                ? request.getRole().trim().toUpperCase()
                : "CUSTOMER";

        if (!role.equals("ADMIN") && !role.equals("CUSTOMER")) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Role must be either ADMIN or CUSTOMER");
        }

        String encodedPassword = passwordEncoder.encode(request.getPassword());
        User user = new User(request.getUsername(), encodedPassword, role);
        userRepository.save(user);

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole(), "User registered successfully");
    }

    public AuthResponse login(LoginRequest request) {
        if (request.getUsername() == null || request.getPassword() == null) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        User user = userRepository.findByUsername(request.getUsername())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        if (!passwordEncoder.matches(request.getPassword(), user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password");
        }

        String token = jwtUtil.generateToken(user.getUsername(), user.getRole());
        return new AuthResponse(token, user.getUsername(), user.getRole(), "Login successful");
    }

    public ValidateResponse validate(String tokenHeader) {
        if (tokenHeader == null || tokenHeader.isBlank()) {
            return new ValidateResponse(false, null, null, "Authorization token is missing");
        }

        String token = tokenHeader.startsWith("Bearer ")
                ? tokenHeader.substring(7).trim()
                : tokenHeader.trim();

        if (!jwtUtil.validateToken(token)) {
            return new ValidateResponse(false, null, null, "Invalid or expired token");
        }

        String username = jwtUtil.extractUsername(token);
        String role = jwtUtil.extractRole(token);

        return new ValidateResponse(true, username, role, "Token is valid");
    }
}
