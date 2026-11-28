package com.nextround.nextroundapi.integration;

import com.nextround.nextroundapi.BaseIntegrationTest;
import com.nextround.nextroundapi.dtos.ChangePasswordRequest;
import com.nextround.nextroundapi.dtos.DeleteAccountRequest;
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
import tools.jackson.databind.ObjectMapper;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

class UserProfileIntegrationTest extends BaseIntegrationTest {

    private static final String DEFAULT_PASSWORD = "Password123!";

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private UserRepository userRepository;

    private String registerAndLogin(String email, String firstName, String lastName) throws Exception {
        return registerAndLogin(email, DEFAULT_PASSWORD, firstName, lastName);
    }

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
                .asString();
    }

    // ==========================================
    // PROFILE UPDATE TESTS
    // ==========================================
    @Test
    @DisplayName("PATCH /api/v1/users/me updates and trims name for regular user")
    void updateProfile_ValidRequest_Returns200AndPersistsChanges() throws Exception {
        String token = registerAndLogin("john@example.com", "OldFirst", "OldLast");
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

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("John"))
                .andExpect(jsonPath("$.lastName").value("Abuel"));

        User persisted = userRepository.findByEmail("john@example.com").orElseThrow();
        assertThat(persisted.getFirstName()).isEqualTo("John");
        assertThat(persisted.getLastName()).isEqualTo("Abuel");
        assertThat(persisted.getPassword()).isEqualTo(originalPasswordHash);
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me returns 400 for blank or oversized names without modifying DB")
    void updateProfile_InvalidRequest_Returns400AndLeavesDbUnchanged() throws Exception {
        String token = registerAndLogin("jane@example.com", "Jane", "Doe");
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
        assertThat(persisted.getFirstName()).isEqualTo("Jane");
        assertThat(persisted.getLastName()).isEqualTo("Doe");
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
        String tokenA = registerAndLogin("usera@example.com", "Alice", "Smith");
        registerAndLogin("userb@example.com", "Bob", "Jones");

        UpdateProfileRequest request = new UpdateProfileRequest("Alicia", "Updated");

        mockMvc.perform(patch("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.firstName").value("Alicia"));

        User userB = userRepository.findByEmail("userb@example.com").orElseThrow();
        assertThat(userB.getFirstName()).isEqualTo("Bob");
        assertThat(userB.getLastName()).isEqualTo("Jones");
    }

    // ==========================================
    // CHANGE PASSWORD TESTS
    // ==========================================
    @Test
    @DisplayName("PATCH /api/v1/users/me/password updates password and allows login with new credentials")
    void changePassword_ValidRequest_Returns204AndUpdatesCredentials() throws Exception {
        String token = registerAndLogin("pwuser@example.com", "Alex", "Cruz");
        ChangePasswordRequest request = new ChangePasswordRequest(DEFAULT_PASSWORD, "NewPass123!");

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "pwuser@example.com",
                                "password", DEFAULT_PASSWORD
                        ))))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(post("/api/v1/auth/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(Map.of(
                                "email", "pwuser@example.com",
                                "password", "NewPass123!"
                        ))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.token").isNotEmpty());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me/password returns 400 when current password is wrong")
    void changePassword_WrongCurrentPassword_Returns400AndKeepsOldPassword() throws Exception {
        String token = registerAndLogin("wrongpw@example.com", "Alex", "Cruz");
        String originalHash = userRepository.findByEmail("wrongpw@example.com")
                .orElseThrow()
                .getPassword();

        ChangePasswordRequest request = new ChangePasswordRequest("WrongPass123!", "NewPass123!");

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        User persisted = userRepository.findByEmail("wrongpw@example.com").orElseThrow();
        assertThat(persisted.getPassword()).isEqualTo(originalHash);
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me/password returns 400 when new password matches current password")
    void changePassword_SameAsCurrentPassword_Returns400() throws Exception {
        String token = registerAndLogin("samepw@example.com", "Alex", "Cruz");
        ChangePasswordRequest request = new ChangePasswordRequest(DEFAULT_PASSWORD, DEFAULT_PASSWORD);

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me/password returns 400 for blank password fields")
    void changePassword_BlankFields_Returns400() throws Exception {
        String token = registerAndLogin("blankpw@example.com", "Alex", "Cruz");

        ChangePasswordRequest[] invalidPayloads = {
                new ChangePasswordRequest("   ", "NewPass123!"),
                new ChangePasswordRequest(DEFAULT_PASSWORD, "   ")
        };

        for (ChangePasswordRequest invalid : invalidPayloads) {
            mockMvc.perform(patch("/api/v1/users/me/password")
                            .header("Authorization", "Bearer " + token)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(invalid)))
                    .andExpect(status().isBadRequest());
        }
    }

    @Test
    @DisplayName("PATCH /api/v1/users/me/password returns 401 when unauthenticated")
    void changePassword_Unauthenticated_Returns401() throws Exception {
        ChangePasswordRequest request = new ChangePasswordRequest(DEFAULT_PASSWORD, "NewPass123!");

        mockMvc.perform(patch("/api/v1/users/me/password")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    // ==========================================
    // DELETE ACCOUNT TESTS
    // ==========================================
    @Test
    @DisplayName("DELETE /api/v1/users/me deletes authenticated user and invalidates token")
    void deleteAccount_shouldProperlyDeleteAccount() throws Exception {
        String tokenA = registerAndLogin("deluser@example.com", "Alice", "Smith");
        DeleteAccountRequest request = new DeleteAccountRequest(DEFAULT_PASSWORD);

        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findByEmail("deluser@example.com")).isEmpty();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/me returns 400 and preserves account when password is wrong")
    void deleteAccount_WrongPassword_Returns400AndKeepsAccount() throws Exception {
        String token = registerAndLogin("keepme@example.com", "Alice", "Smith");
        DeleteAccountRequest request = new DeleteAccountRequest("WrongPassword!");

        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findByEmail("keepme@example.com")).isPresent();
    }

    @Test
    @DisplayName("DELETE /api/v1/users/me returns 400 and preserves account when password is blank")
    void deleteAccount_BlankPassword_Returns400AndKeepsAccount() throws Exception {
        String token = registerAndLogin("blankdel@example.com", "Alice", "Smith");
        DeleteAccountRequest request = new DeleteAccountRequest("   ");

        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        assertThat(userRepository.findByEmail("blankdel@example.com")).isPresent();
    }

    @Test
    @DisplayName("DELETE /api/v1/users/me returns 401 when unauthenticated")
    void deleteAccount_Unauthenticated_Returns401() throws Exception {
        DeleteAccountRequest request = new DeleteAccountRequest(DEFAULT_PASSWORD);

        mockMvc.perform(delete("/api/v1/users/me")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @DisplayName("DELETE /api/v1/users/me only deletes the authenticated user")
    void deleteAccount_UserIsolation_DoesNotAffectOtherUsers() throws Exception {
        String tokenA = registerAndLogin("deletea@example.com", "Alice", "Smith");
        String tokenB = registerAndLogin("deleteb@example.com", "Bob", "Jones");

        DeleteAccountRequest request = new DeleteAccountRequest(DEFAULT_PASSWORD);

        mockMvc.perform(delete("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenA)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNoContent());

        assertThat(userRepository.findByEmail("deletea@example.com")).isEmpty();
        assertThat(userRepository.findByEmail("deleteb@example.com")).isPresent();

        mockMvc.perform(get("/api/v1/users/me")
                        .header("Authorization", "Bearer " + tokenB))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.email").value("deleteb@example.com"));
    }
}