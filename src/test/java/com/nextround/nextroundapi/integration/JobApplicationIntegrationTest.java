package com.nextround.nextroundapi.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.nextround.nextroundapi.BaseIntegrationTest;
import com.nextround.nextroundapi.dtos.JobApplicationRequest;
import com.nextround.nextroundapi.dtos.RegisterRequest;
import com.nextround.nextroundapi.entity.Company;
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
    private Company savedCompany;

    @BeforeEach
    void setUp() throws Exception {
        // Clear tables in reverse dependency order
        jobApplicationRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        // 1. Seed primary company
        savedCompany = companyRepository.saveAndFlush(
                new Company("Canva", "https://canva.com", "Design & Tech", "Manila, Philippines")
        );

        // 2. Register Tenant A
        userAToken = registerAndExtractJwt(
                "candidate.a@nextround.dev",
                "Password123!",
                "Alice",
                "Guillermo"
        );

        // 3. Register Tenant B
        userBToken = registerAndExtractJwt(
                "candidate.b@nextround.dev",
                "Password123!",
                "Bob",
                "Navarro"
        );
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

    private String createApplication(String jwtToken, String title) throws Exception {
        return createApplication(jwtToken, savedCompany, title, ApplicationStatus.APPLIED, WorkLocationType.HYBRID);
    }

    private String createApplication(String jwtToken, Company company, String title, ApplicationStatus status, WorkLocationType location) throws Exception {
        JobApplicationRequest request = new JobApplicationRequest(
                company.getId(),
                title,
                "https://canva.com/careers/sample",
                status,
                location,
                BigDecimal.valueOf(85000),
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
    @DisplayName("POST /api/v1/applications -> should persist job application and link to authenticated user")
    void createJobApplication_Success() throws Exception {
        JobApplicationRequest request = new JobApplicationRequest(
                savedCompany.getId(),
                "Junior Software Engineer",
                "https://canva.com/jobs/101",
                ApplicationStatus.APPLIED,
                WorkLocationType.HYBRID,
                BigDecimal.valueOf(60000),
                BigDecimal.valueOf(75000),
                "PHP",
                LocalDate.now()
        );

        MvcResult result = mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").isNotEmpty())
                .andExpect(jsonPath("$.jobTitle").value("Junior Software Engineer"))
                .andExpect(jsonPath("$.applicationStatus").value("APPLIED"))
                .andExpect(jsonPath("$.workLocation").value("HYBRID"))
                .andExpect(jsonPath("$.company.id").value(savedCompany.getId().toString()))
                .andReturn();

        String rawId = objectMapper.readTree(result.getResponse().getContentAsString()).get("id").asText();
        UUID createdId = UUID.fromString(rawId);

        var savedApp = jobApplicationRepository.findByIdWithDetails(createdId).orElseThrow();
        assertThat(savedApp.getUser().getEmail()).isEqualTo("candidate.a@nextround.dev");
        assertThat(savedApp.getCompany().getId()).isEqualTo(savedCompany.getId());
    }

    @Test
    @DisplayName("GET /api/v1/applications -> should only return applications belonging to caller")
    void getAllApplications_EnforcesTenantIsolation() throws Exception {
        createApplication(userAToken, "Backend Developer - Java");
        createApplication(userAToken, "Full-Stack Developer - Next.js");
        createApplication(userBToken, "Frontend Developer - React");

        mockMvc.perform(get("/api/v1/applications")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[?(@.jobTitle == 'Backend Developer - Java')]").exists())
                .andExpect(jsonPath("$.content[?(@.jobTitle == 'Full-Stack Developer - Next.js')]").exists());

        mockMvc.perform(get("/api/v1/applications")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].jobTitle").value("Frontend Developer - React"));
    }

    @Test
    @DisplayName("GET /api/v1/applications?companyId={id}&status={status} -> returns matching tenant applications only")
    void getAllApplications_CombinedCompanyAndStatusFilter_Success() throws Exception {
        Company secondCompany = companyRepository.saveAndFlush(
                new Company("GCash", "https://gcash.com", "Fintech", "Taguig, Philippines")
        );

        // Tenant A targets
        createApplication(userAToken, savedCompany, "Senior Backend Engineer", ApplicationStatus.INTERVIEWING, WorkLocationType.REMOTE);
        createApplication(userAToken, savedCompany, "Junior Backend Engineer", ApplicationStatus.APPLIED, WorkLocationType.ON_SITE);
        createApplication(userAToken, secondCompany, "Lead Systems Engineer", ApplicationStatus.INTERVIEWING, WorkLocationType.HYBRID);

        // Tenant B target (should not leak)
        createApplication(userBToken, savedCompany, "Security Engineer", ApplicationStatus.INTERVIEWING, WorkLocationType.REMOTE);

        mockMvc.perform(get("/api/v1/applications")
                        .header("Authorization", "Bearer " + userAToken)
                        .param("companyId", savedCompany.getId().toString())
                        .param("status", ApplicationStatus.INTERVIEWING.name()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(1))
                .andExpect(jsonPath("$.content[0].jobTitle").value("Senior Backend Engineer"))
                .andExpect(jsonPath("$.content[0].applicationStatus").value("INTERVIEWING"))
                .andExpect(jsonPath("$.content[0].workLocation").value("REMOTE"))
                .andExpect(jsonPath("$.content[0].company.id").value(savedCompany.getId().toString()));
    }

    @Test
    @DisplayName("GET /api/v1/applications/{id} -> Tenant B cannot view Tenant A's application")
    void getById_CrossTenantAccess_ReturnsNotFound() throws Exception {
        String applicationAId = createApplication(userAToken, "DevOps Engineer");

        mockMvc.perform(get("/api/v1/applications/" + applicationAId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("DELETE /api/v1/applications/{id} -> Tenant B cannot delete Tenant A's application")
    void delete_CrossTenantAttempt_ReturnsNotFoundAndPreservesData() throws Exception {
        String appAId = createApplication(userAToken, "Site Reliability Engineer");

        mockMvc.perform(delete("/api/v1/applications/" + appAId)
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());

        assertThat(jobApplicationRepository.existsById(UUID.fromString(appAId))).isTrue();
    }

    @Test
    @DisplayName("DELETE /api/v1/applications/{id} -> Owner deletes own applications; returns 204")
    void delete_JobApplicationOwner_Success() throws Exception {
        String appAId = createApplication(userAToken, "Platform Engineer");

        mockMvc.perform(delete("/api/v1/applications/" + appAId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isNoContent());

        assertThat(jobApplicationRepository.existsById(UUID.fromString(appAId))).isFalse();
    }

    @Test
    @DisplayName("PUT /api/v1/applications/{id} -> Tenant B cannot edit Tenant A's application")
    void put_CrossTenantAttempt_ReturnsNotFoundAndPreservesData() throws Exception {
        String applicationAId = createApplication(userAToken, "Data Engineer");

        JobApplicationRequest requestHijack = new JobApplicationRequest(
                savedCompany.getId(),
                "Hijacked Title",
                "https://canva.com/careers/x",
                ApplicationStatus.OFFER_RECEIVED,
                WorkLocationType.REMOTE,
                BigDecimal.valueOf(1),
                BigDecimal.valueOf(2),
                "PHP",
                LocalDate.now()
        );

        mockMvc.perform(put("/api/v1/applications/" + applicationAId)
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(requestHijack)))
                .andExpect(status().isNotFound());

        mockMvc.perform(get("/api/v1/applications/" + applicationAId)
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.jobTitle").value("Data Engineer"));
    }
}