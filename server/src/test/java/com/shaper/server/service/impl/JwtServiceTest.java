package com.shaper.server.service.impl;

import com.shaper.server.CustomUserDetails;
import com.shaper.server.model.entity.HrUser;
import com.shaper.server.model.entity.User;
import com.shaper.server.model.enums.UserRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;
    private User testUser;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        
        // Set test values using reflection
        ReflectionTestUtils.setField(jwtService, "secret", "bZNatHk6UxEnAtz9TfNKGAobhCZbCvaFEs58ZeG3GNhCOvVhlDE0ST7WtQculYJJ");
        ReflectionTestUtils.setField(jwtService, "jwtExpiration", 3600000L);
        ReflectionTestUtils.setField(jwtService, "refreshExpiration", 604800000L);

        // Create test user
        testUser = new HrUser();
        testUser.setId(UUID.randomUUID());
        testUser.setEmail("test@example.com");
        testUser.setFirstName("John");
        testUser.setLastName("Doe");
        testUser.setRole(UserRole.HR_MANAGER);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Test
    void shouldGenerateValidToken() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertNotNull(token);
        assertFalse(token.isEmpty());
        assertTrue(token.split("\\.").length == 3); // JWT has 3 parts
    }

    @Test
    void shouldExtractUsernameFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedUsername = jwtService.extractUserName(token);

        // Then
        assertEquals(testUser.getEmail(), extractedUsername);
    }

    @Test
    void shouldExtractUserIdFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedUserId = jwtService.extractUserId(token);

        // Then
        assertEquals(testUser.getId().toString(), extractedUserId);
    }

    @Test
    void shouldExtractRoleFromToken() {
        // Given
        String token = jwtService.generateToken(testUser);

        // When
        String extractedRole = jwtService.extractRole(token);

        // Then
        assertEquals(testUser.getRole().name(), extractedRole);
    }

    @Test
    void shouldValidateTokenSuccessfully() {
        // Given
        String token = jwtService.generateToken(testUser);
        CustomUserDetails userDetails = new CustomUserDetails(testUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, userDetails);

        // Then
        assertTrue(isValid);
    }

    @Test
    void shouldRejectTokenForDifferentUser() {
        // Given
        String token = jwtService.generateToken(testUser);
        
        User differentUser = new HrUser();
        differentUser.setEmail("different@example.com");
        differentUser.setRole(UserRole.HR_MANAGER);
        CustomUserDetails differentUserDetails = new CustomUserDetails(differentUser);

        // When
        boolean isValid = jwtService.isTokenValid(token, differentUserDetails);

        // Then
        assertFalse(isValid);
    }

    @Test
    void shouldGenerateRefreshToken() {
        // When
        String refreshToken = jwtService.generateRefreshToken(testUser);

        // Then
        assertNotNull(refreshToken);
        assertFalse(refreshToken.isEmpty());
        
        // Should be able to extract username from refresh token
        String extractedUsername = jwtService.extractUserName(refreshToken);
        assertEquals(testUser.getEmail(), extractedUsername);
    }

    @Test
    void shouldIncludeUserDetailsInTokenClaims() {
        // When
        String token = jwtService.generateToken(testUser);

        // Then
        assertEquals(testUser.getEmail(), jwtService.extractUserName(token));
        assertEquals(testUser.getId().toString(), jwtService.extractUserId(token));
        assertEquals(testUser.getRole().name(), jwtService.extractRole(token));
    }
}