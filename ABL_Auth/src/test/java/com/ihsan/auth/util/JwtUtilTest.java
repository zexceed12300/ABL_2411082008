package com.ihsan.auth.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class JwtUtilTest {

    private JwtUtil jwtUtil;
    private final String secret = "mySecretKeyForTestingJwtTokenGenerationAndValidationLongEnough!";
    private final long expiration = 3600000; // 1 hour

    @BeforeEach
    void setUp() {
        jwtUtil = new JwtUtil(secret, expiration);
    }

    @Test
    void testGenerateAndValidateToken() {
        String token = jwtUtil.generateToken("user123@example.com", "ADMIN");
        assertThat(token).isNotNull().isNotEmpty();

        assertThat(jwtUtil.validateToken(token)).isTrue();
        assertThat(jwtUtil.extractEmail(token)).isEqualTo("user123@example.com");
        assertThat(jwtUtil.extractRole(token)).isEqualTo("ADMIN");
    }

    @Test
    void testInvalidToken() {
        assertThat(jwtUtil.validateToken("invalid.token.string")).isFalse();
    }

    @Test
    void testExpiredToken() {
        JwtUtil shortLivedJwt = new JwtUtil(secret, -1000); // already expired
        String token = shortLivedJwt.generateToken("user_expired@example.com", "CUSTOMER");
        assertThat(jwtUtil.validateToken(token)).isFalse();
    }
}
