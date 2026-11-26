package com.nextround.nextroundapi.integration;

import tools.jackson.databind.ObjectMapper;
import com.nextround.nextroundapi.BaseIntegrationTest;
import com.nextround.nextroundapi.dtos.UpdateProfileRequest;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.Role;
import com.nextround.nextroundapi.repository.UserRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserProfileIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    // Swap with your existing auth helper if BaseIntegrationTest already provides one
    private String registerAndLogin(String email, String password, String firstName, String lastName) throws Exception {
        Map<String, String> registerBody = Map.of(
                "email", email,
                "password", password,
                "firstName", firstName,
                "lastName", lastName
        );
        mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(registerBody)))
                .andExpect(status().isCreated());

        MvcResult loginResult = mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of("email", email, "password", password))))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readTree(loginResult.getResponse().getContentAsString())
                .get("token")
                .asText();
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me updates and trims name for regular user")
    void updateProfile_ValidRequest_Returns200AndPersistsChanges() throws Exception {
        String token = registerAndLogin("john@example.com", "Password123!", "OldFirst", "OldLast");
        String originalPasswordHash = userRepository.findByEmail("john@example.com")
                .orElseThrow()
                .getPassword();

        UpdateProfileRequest request = new UpdateProfileRequest("  John  ", "  Abuel  ");

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Abuel"))
                .andExpect(jsonPath("$.email").value("john@example.com"))
                .andExpect(jsonPath("$.role").value(Role.USER.name()));

        // Verify persistence via GET /me and direct DB check
        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Abuel"));

        User persisted = userRepository.findByEmail("john@example.com").orElseThrow();
        assertEquals("John", persisted.getFirstName());
        assertEquals("Abuel", persisted.getLastName());
        assertEquals(originalPasswordHash, persisted.getPassword());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me returns 400 for blank or oversized names without modifying DB")
    void updateProfile_InvalidRequest_Returns400AndLeavesDbUnchanged() throws Exception {
        String token = registerAndLogin("jane@example.com", "Password123!", "Jane", "Doe");
        String tooLong = "a".repeat(51);

        UpdateProfileRequest[] invalidPayloads = {
                new UpdateProfileRequest("   ", "Doe"),
                new UpdateProfileRequest("Jane", ""),
                new UpdateProfileRequest(tooLong, "Doe"),
                new UpdateProfileRequest("Jane", tooLong)
        };

        for (UpdateProfileRequest invalid : invalidPayloads) {
            mockMvc.perform(patch("/api/v1/users/me")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }

        User persisted = userRepository.findByEmail("jane@example.com").orElseThrow();
        assertEquals("Jane", persisted.getFirstName());
        assertEquals("Doe", persisted.getLastName());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me returns 401 when unauthenticated")
    void updateProfile_Unauthenticated_Returns401() throws Exception {
        UpdateProfileRequest request = new UpdateProfileRequest("John", "Abuel");

        mockMvc.perform(patch("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me only updates the authenticated user")
    void updateProfile_UserIsolation_DoesNotModifyOtherUsers() throws Exception {
        String tokenA = registerAndLogin("usera@example.com", "Password123!", "Alice", "Smith");
        registerAndLogin("userb@example.com", "Password123!", "Bob", "Jones");

        UpdateProfileRequest request = new UpdateProfileRequest("Alicia", "Updated");

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alicia"));

        User userB = userRepository.findByEmail("userb@example.com").orElseThrow();
        assertEquals("Bob", userB.getFirstName());
        assertEquals("Jones", userB.getLastName());
    }
}