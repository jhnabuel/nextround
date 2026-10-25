package com.nextround.nextroundapi.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.nextround.nextroundapi.BaseIntegrationTest;
import com.nextround.nextroundapi.dtos.ChangePasswordRequest;
import com.nextround.nextroundapi.dtos.LoginRequest;
import com.nextround.nextroundapi.dtos.RegisterRequest;
import com.nextround.nextroundapi.repository.UserRepository;
import lombok.extern.java.Log;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class ChangePasswordIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    private String userEmail;
    private String jwtToken;

    @BeforeEach
    void setUp() throws Exception {
        userRepository.deleteAllInBatch();

        userEmail = "candidate@nextround.dev";
        RegisterRequest registerRequest = new RegisterRequest(
                userEmail,
                "OldPassword123!",
                "Candidate",
                "User"
        );

        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerRequest)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        jwtToken = responseNode.get("token").asText();
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me/password -> returns 400 when current password is wrong")
    void changePassword_WrongCurrentPassword_ReturnsBadRequest() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(
                "WrongPassword123!",
                "BrandNewPassword123!"
        );

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Current password does not match."));
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me/password -> success updates credentials; old password fails login, new password succeeds")
    void changePassword_Success_UpdatesCredentials() throws Exception {
        ChangePasswordRequest changeRequest = new ChangePasswordRequest(
                "OldPassword123!",
                "BrandNewPassword123!"
        );

        // 1. Change password
        mockMvc.perform(patch("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(changeRequest)))
                .andExpect(status().isNoContent());

        // 2. Old password should fail authentication (401)
        LoginRequest oldLoginRequest = new LoginRequest(userEmail, "OldPassword123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(oldLoginRequest)))
                .andExpect(status().isUnauthorized());

        // 3. New password should authenticate successfully (200)
        LoginRequest newLoginRequest = new LoginRequest(userEmail, "BrandNewPassword123!");
        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(newLoginRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("GET /api/v1/users/me -> returns authenticated user profile details")
    void getCurrentUserProfile_Success() throws Exception {
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + jwtToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value(userEmail))
                .andExpect(jsonPath("$.firstName").value("Candidate"))
                .andExpect(jsonPath("$.lastName").value("User"));
    }
}