package com.ihsan.auth.service;

import com.ihsan.auth.dto.AuthResponse;
import com.ihsan.auth.dto.LoginRequest;
import com.ihsan.auth.dto.RegisterRequest;
import com.ihsan.auth.dto.ValidateResponse;
import com.ihsan.auth.entity.User;
import com.ihsan.auth.repository.UserRepository;
import com.ihsan.auth.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @InjectMocks
    private AuthService authService;

    private User sampleUser;

    @BeforeEach
    void setUp() {
        sampleUser = new User("testuser", "encoded_secret", "CUSTOMER");
        sampleUser.setId(1L);
    }

    @Test
    void testRegisterSuccessCustomer() {
        RegisterRequest request = new RegisterRequest("testuser", "secret123", "CUSTOMER");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);
        when(passwordEncoder.encode("secret123")).thenReturn("encoded_secret");
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);
        when(jwtUtil.generateToken("testuser", "CUSTOMER")).thenReturn("jwt_token_sample");

        AuthResponse response = authService.register(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt_token_sample");
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
        verify(userRepository).save(any(User.class));
    }

    @Test
    void testRegisterInvalidRoleThrowsBadRequest() {
        RegisterRequest request = new RegisterRequest("testuser", "secret123", "INVALID_ROLE");
        when(userRepository.existsByUsername("testuser")).thenReturn(false);

        assertThatThrownBy(() -> authService.register(request))
                .isInstanceOf(ResponseStatusException.class)
                .hasFieldOrPropertyWithValue("statusCode", HttpStatus.BAD_REQUEST);
    }

    @Test
    void testLoginSuccess() {
        LoginRequest request = new LoginRequest("testuser", "secret123");
        when(userRepository.findByUsername("testuser")).thenReturn(Optional.of(sampleUser));
        when(passwordEncoder.matches("secret123", "encoded_secret")).thenReturn(true);
        when(jwtUtil.generateToken("testuser", "CUSTOMER")).thenReturn("jwt_token_sample");

        AuthResponse response = authService.login(request);

        assertThat(response).isNotNull();
        assertThat(response.getToken()).isEqualTo("jwt_token_sample");
        assertThat(response.getUsername()).isEqualTo("testuser");
    }

    @Test
    void testValidateTokenSuccess() {
        String token = "Bearer valid_token";
        when(jwtUtil.validateToken("valid_token")).thenReturn(true);
        when(jwtUtil.extractUsername("valid_token")).thenReturn("testuser");
        when(jwtUtil.extractRole("valid_token")).thenReturn("CUSTOMER");

        ValidateResponse response = authService.validate(token);

        assertThat(response.isValid()).isTrue();
        assertThat(response.getUsername()).isEqualTo("testuser");
        assertThat(response.getRole()).isEqualTo("CUSTOMER");
    }
}
