package com.nextround.nextroundapi.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.nextround.nextroundapi.BaseIntegrationTest;
import com.nextround.nextroundapi.dtos.CompanyRequest;
import com.nextround.nextroundapi.dtos.JobApplicationRequest;
import com.nextround.nextroundapi.dtos.RegisterRequest;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.enums.WorkLocationType;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class JobApplicationIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private UserRepository userRepository;

    private String userAToken;
    private String userBToken;
    private Company companyOwnedByA;

    @BeforeEach
    void setUp() throws Exception {
        jobApplicationRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        userAToken = registerAndExtractJwt("alice@nextround.dev", "Password123!", "Alice", "Guillermo");
        userBToken = registerAndExtractJwt("bob@nextround.dev", "Password123!", "Bob", "Navarro");

        companyOwnedByA = createCompany(userAToken, "Canva");
    }

    private String registerAndExtractJwt(String email, String password, String firstName, String lastName) throws Exception {
        RegisterRequest request = new RegisterRequest(email, password, firstName, lastName);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseNode.get("token").asText();
    }

    private Company createCompany(String token, String name) throws Exception {
        CompanyRequest request = new CompanyRequest(name, "https://example.com", "Tech", "Remote");
        MvcResult result = mockMvc.perform(post("/api/v1/companies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return companyRepository.findById(UUID.fromString(node.get("id").asText())).orElseThrow();
    }

    private String createApplication(String jwtToken, UUID companyId, String companyName, String title) throws Exception {
        JobApplicationRequest request = new JobApplicationRequest(
                companyId,
                companyName,
                title,
                "https://canva.com/careers/1",
                ApplicationStatus.APPLIED,
                WorkLocationType.REMOTE,
                BigDecimal.valueOf(80000),
                BigDecimal.valueOf(110000),
                "PHP",
                LocalDate.now()
        );

        MvcResult result = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + jwtToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();

        JsonNode responseNode = objectMapper.readTree(result.getResponse().getContentAsString());
        return responseNode.get("id").asText();
    }

    @Test
    @DisplayName("GET /api/v1/applications/{id} -> userCannotReadAnotherUsersApplication (returns 404)")
    void userCannotReadAnotherUsersApplication() throws Exception {
        String appId = createApplication(userAToken, companyOwnedByA.getId(), null, "Frontend Engineer");

        mockMvc.perform(get("/api/v1/applications/{id}", appId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/applications -> userCannotAttachApplicationToAnotherUsersCompany (returns 404)")
    void userCannotAttachApplicationToAnotherUsersCompany() throws Exception {
        JobApplicationRequest request = new JobApplicationRequest(
                companyOwnedByA.getId(), // Owned by User A
                null,
                "Intruder Role",
                null,
                ApplicationStatus.APPLIED,
                WorkLocationType.REMOTE,
                null,
                null,
                "PHP",
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + userBToken) // Sent by User B
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("POST /api/v1/applications -> creates application and resolves new company inline by companyName")
    void createApplication_WithCompanyName_FindOrCreateSuccess() throws Exception {
        JobApplicationRequest request = new JobApplicationRequest(
                null,
                "Automattic",
                "Full Stack Developer",
                "https://automattic.com/jobs",
                ApplicationStatus.APPLIED,
                WorkLocationType.REMOTE,
                BigDecimal.valueOf(90000),
                BigDecimal.valueOf(120000),
                "USD",
                LocalDate.now()
        );

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.jobTitle").value("Full Stack Developer"))
                .andExpect(jsonPath("$.company.companyName").value("Automattic"));

        User bob = userRepository.findByEmail("bob@nextround.dev").orElseThrow();
        assertThat(companyRepository.existsByUserIdAndCompanyNameIgnoreCase(bob.getId(), "Automattic")).isTrue();
    }

    @Test
    @DisplayName("PUT /api/v1/applications/{id} -> User B cannot edit User A's application")
    void put_CrossTenantAttempt_ReturnsNotFoundAndPreservesData() throws Exception {
        String appId = createApplication(userAToken, companyOwnedByA.getId(), null, "Data Analyst");

        JobApplicationRequest requestHijack = new JobApplicationRequest(
                companyOwnedByA.getId(),
                null,
                "Hijacked Title",
                null,
                ApplicationStatus.ACCEPTED,
                WorkLocationType.REMOTE,
                null,
                null,
                "PHP",
                LocalDate.now()
        );

        mockMvc.perform(put("/api/v1/applications/" + appId)
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestHijack)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/applications/" + appId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Data Analyst"));
    }

    @Test
    @DisplayName("DELETE /api/v1/applications/{id} -> User B cannot delete User A's application")
    void delete_CrossTenantAttempt_ReturnsNotFoundAndPreservesData() throws Exception {
        String appId = createApplication(userAToken, companyOwnedByA.getId(), null, "DevOps Engineer");

        mockMvc.perform(delete("/api/v1/applications/" + appId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());

        assertThat(jobApplicationRepository.existsById(UUID.fromString(appId))).isTrue();
    }
}