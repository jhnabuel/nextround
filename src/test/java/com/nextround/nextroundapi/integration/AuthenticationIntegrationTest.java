package com.nextround.nextroundapi.integration;

import com.nextround.nextroundapi.BaseIntegrationTest;
import com.nextround.nextroundapi.dtos.RegisterRequest;
import com.nextround.nextroundapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void cleanUp(){
        userRepository.deleteAll();
    }

    @Test
    @DisplayName("POST /api/v1/auth/register -> should save user into Testcontainers PostgreSQL")
    void registerUser_Success() throws Exception{
        RegisterRequest request = new RegisterRequest("candidate@example.com", "Password123!", "John", "Doe");
        mockMvc.perform(post("/api/v1/auth/register")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))).andExpect(status().isCreated()).andExpect(jsonPath("$.token").isString());

        assertThat(userRepository.findByEmail("candidate@example.com")).isPresent();
    }





}
