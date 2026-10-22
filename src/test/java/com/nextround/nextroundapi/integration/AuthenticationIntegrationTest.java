package com.nextround.nextroundapi.integration;

import com.nextround.nextroundapi.BaseIntegrationTest;
import com.nextround.nextroundapi.dtos.LoginRequest;
import com.nextround.nextroundapi.dtos.RegisterRequest;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.Role;
import com.nextround.nextroundapi.repository.UserRepository;
import com.nextround.nextroundapi.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.security.test.web.servlet.response.SecurityMockMvcResultMatchers.unauthenticated;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

public class AuthenticationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

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


    @Test
    @DisplayName("POST /api/v1/auth/login -> should let user login into the website")
    void loginUser_Success() throws Exception{
        User testUser = new User("candidate@example.com",
                passwordEncoder.encode("Password123!"),
                "Test",
                "User",
                Role.USER);
        // Set other required fields (roles, enabled status, etc.)
        userRepository.save(testUser);
        LoginRequest loginRequest = new LoginRequest("candidate@example.com", "Password123!");

        mockMvc.perform(post("/api/v1/auth/login").with(csrf()).contentType(MediaType.APPLICATION_JSON).content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().isOk()).andExpect(jsonPath("$.token").exists())
                .andExpect(content().contentType(MediaType.APPLICATION_JSON));
    }

    @Test
    @DisplayName("POST /api/v1/auth/login -> should fail if user has wrong password.")
    void loginUser_Failure_ShouldReturnError() throws Exception{
        User testUser = new User("candidate@example.com",
                passwordEncoder.encode("Password123!"),
                "Test",
                "User",
                Role.USER);
        // Set other required fields (roles, enabled status, etc.)
        userRepository.save(testUser);
        LoginRequest loginRequest = new LoginRequest("candidate@example.com", "Password12");

        mockMvc.perform(post("/api/v1/auth/login")
                        .with(csrf())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(loginRequest)))
                .andExpect(status().is4xxClientError())
                .andExpect(unauthenticated());

    }

    @Test
    @DisplayName("GET /api/v1/applications -> fails with 401 Unauthorized when no token is provided")
    void accessProtectedEndpoint_NoToken_Fails() throws Exception{
        mockMvc.perform(get("/api/v1/applications"))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/users -> regular user with ROLE_USER gets 403 Forbidden")
    void regularUser_CannotAccessUserManagement_ReturnsForbidden() throws Exception {
        User regularUser = userRepository.save(new User(
                "candidate@example.com",
                passwordEncoder.encode("Password123!"),
                "Regular",
                "User",
                Role.USER
        ));

        String regularUserToken = jwtService.generateToken(regularUser);

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + regularUserToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("GET /api/v1/applications -> token for deleted user returns 401 Unauthorized")
    void protectedEndpoint_tokenForDeletedUser_returns401() throws Exception {
        // 1. Persist user and create a valid signed token
        User ghostUser = userRepository.save(new User(
                "ghost@nextround.dev",
                passwordEncoder.encode("Password123!"),
                "Ghost",
                "User",
                Role.USER
        ));
        String token = jwtService.generateToken(ghostUser);

        // 2. Delete the user from the database
        userRepository.delete(ghostUser);

        // 3. Act & Assert: JWT filter/UserDetailsService should fail and return 401
        mockMvc.perform(get("/api/v1/applications")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("GET /api/v1/users -> ADMIN is authorized")
    void admin_CanAccessUserManagement() throws Exception {
        User admin = userRepository.save(new User(
                "admin@nextround.dev",
                passwordEncoder.encode("Password123!"),
                "Admin",
                "User",
                Role.ADMIN
        ));

        String adminToken = jwtService.generateToken(admin);

        mockMvc.perform(get("/api/v1/users")
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("Unclassified endpoint is denied")
    void unclassifiedEndpoint_IsDenied() throws Exception {
        User user = userRepository.save(new User(
                "candidate@example.com",
                passwordEncoder.encode("Password123!"),
                "Test",
                "User",
                Role.USER
        ));

        String token = jwtService.generateToken(user);

        mockMvc.perform(get("/api/v1/unclassified-test")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isForbidden());
    }
}
