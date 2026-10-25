package com.nextround.nextroundapi.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.test.util.ReflectionTestUtils;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    @Autowired
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // Inject test values directly into the @Value fields
        ReflectionTestUtils.setField(
                jwtService,
                "secretKey",
                "RtMOcERxaOlYUCSaw6r/dHKEyDoioCdgIuQG23zeBDA="
        );
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
    }

    @Test
    void generateAndValidateToken_shouldSucceed() {
        UserDetails testUser = new User("test@example.com", "irrelevant-password", java.util.List.of());

        String token = jwtService.generateToken(testUser);
        System.out.println("Generated token: " + token);

        String extractedUsername = jwtService.extractUsername(token);
        assertEquals("test@example.com", extractedUsername);

        boolean isValid = jwtService.isTokenValid(token, testUser);
        assertTrue(isValid);
    }
}