package com.ihsan.auth.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.ihsan.auth.dto.AuthResponse;
import com.ihsan.auth.dto.LoginRequest;
import com.ihsan.auth.dto.RegisterRequest;
import com.ihsan.auth.dto.ValidateResponse;
import com.ihsan.auth.service.AuthService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.server.ResponseStatusException;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@ExtendWith(MockitoExtension.class)
public class AuthControllerTest {

    private MockMvc mockMvc;

    @Mock
    private AuthService authService;

    @InjectMocks
    private AuthController authController;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.standaloneSetup(authController).build();
    }

    @Test
    void testRegisterEndpointSuccess() throws Exception {
        RegisterRequest request = new RegisterRequest("john", "secret123", "CUSTOMER");
        AuthResponse response = new AuthResponse("token123", "john", "CUSTOMER", "User registered successfully");

        when(authService.register(any(RegisterRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.username").value("john"))
                .andExpect(jsonPath("$.role").value("CUSTOMER"));
    }

    @Test
    void testRegisterDuplicateUsernameReturnsBadRequest() throws Exception {
        RegisterRequest request = new RegisterRequest("john", "secret123", "CUSTOMER");
        when(authService.register(any(RegisterRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.BAD_REQUEST, "Username already exists"));

        mockMvc.perform(post("/api/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLoginEndpointSuccess() throws Exception {
        LoginRequest request = new LoginRequest("john", "secret123");
        AuthResponse response = new AuthResponse("token123", "john", "USER", "Login successful");

        when(authService.login(any(LoginRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").value("token123"))
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void testLoginInvalidCredentialsReturnsUnauthorized() throws Exception {
        LoginRequest request = new LoginRequest("john", "wrongpass");
        when(authService.login(any(LoginRequest.class)))
                .thenThrow(new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid username or password"));

        mockMvc.perform(post("/api/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void testValidateEndpointSuccess() throws Exception {
        ValidateResponse response = new ValidateResponse(true, "john", "USER", "Token is valid");
        when(authService.validate("Bearer token123")).thenReturn(response);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer token123"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.valid").value(true))
                .andExpect(jsonPath("$.username").value("john"));
    }

    @Test
    void testValidateEndpointInvalid() throws Exception {
        ValidateResponse response = new ValidateResponse(false, null, null, "Invalid or expired token");
        when(authService.validate("Bearer badtoken")).thenReturn(response);

        mockMvc.perform(get("/api/auth/validate")
                        .header("Authorization", "Bearer badtoken"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.valid").value(false));
    }
}
