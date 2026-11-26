package com.nextround.nextroundapi.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.nextround.nextroundapi.BaseIntegrationTest;
import com.nextround.nextroundapi.dtos.CompanyRequest;
import com.nextround.nextroundapi.dtos.RegisterRequest;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.JobApplication;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.time.LocalDate;
import java.util.UUID;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class DashboardIntegrationTest extends BaseIntegrationTest {

    @Autowired private CompanyRepository companyRepository;
    @Autowired private JobApplicationRepository jobApplicationRepository;
    @Autowired private UserRepository userRepository;

    private String userAToken;
    private String userBToken;
    private User userA;
    private User userB;
    private Company companyA;
    private Company companyB;

    @BeforeEach
    void setUp() throws Exception {

        userAToken = registerAndExtractJwt("alice@nextround.dev", "Password123!", "Alice", "Smith");
        userA = userRepository.findByEmail("alice@nextround.dev").orElseThrow();
        companyA = createCompany(userAToken, "Stripe");

        userBToken = registerAndExtractJwt("bob@nextround.dev", "Password123!", "Bob", "Jones");
        userB = userRepository.findByEmail("bob@nextround.dev").orElseThrow();
        companyB = createCompany(userBToken, "Google");

        LocalDate today = LocalDate.now();

        // User A: 3 submitted + 1 WISHLIST (excluded)
        saveApplication(userA, companyA, ApplicationStatus.APPLIED, today);
        saveApplication(userA, companyA, ApplicationStatus.INTERVIEWING, today.minusWeeks(1));
        saveApplication(userA, companyA, ApplicationStatus.OFFER_RECEIVED, null); // tests COALESCE fallback
        saveApplication(userA, companyA, ApplicationStatus.WISHLIST, today);

        // User B: 1 REJECTED (must never leak into User A)
        saveApplication(userB, companyB, ApplicationStatus.REJECTED, today);
    }

    // ---------- helpers ----------

    private String registerAndExtractJwt(String email, String password, String firstName, String lastName) throws Exception {
        RegisterRequest request = new RegisterRequest(email, password, firstName, lastName);
        MvcResult result = mockMvc.perform(post("/api/v1/auth/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return node.get("token").asText();
    }

    private Company createCompany(String token, String name) throws Exception {
        CompanyRequest request = new CompanyRequest(name, "https://" + name.toLowerCase().replace(" ", "") + ".com", "Tech", "Remote");
        MvcResult result = mockMvc.perform(post("/api/v1/companies")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andReturn();
        JsonNode node = objectMapper.readTree(result.getResponse().getContentAsString());
        return companyRepository.findById(UUID.fromString(node.get("id").asText())).orElseThrow();
    }

    private void saveApplication(User user, Company company, ApplicationStatus status, LocalDate appliedDate) {
        JobApplication app = new JobApplication();
        app.setUser(user);
        app.setCompany(company);
        app.setJobTitle("Software Engineer");
        app.setStatus(status);
        app.setAppliedDate(appliedDate);
        jobApplicationRepository.save(app);
    }

    // ---------- stats & tenant isolation ----------

    @Test
    @DisplayName("GET /dashboard/stats -> isolates counts per user and excludes WISHLIST")
    void getStats_isolatesByAuthenticatedUserAndExcludesWishlist() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/stats")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(3))
                .andExpect(jsonPath("$.applied").value(1))
                .andExpect(jsonPath("$.interviewing").value(1))
                .andExpect(jsonPath("$.offers").value(1))
                .andExpect(jsonPath("$.rejected").value(0));

        mockMvc.perform(get("/api/v1/dashboard/stats")
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(1))
                .andExpect(jsonPath("$.rejected").value(1));
    }

    @Test
    @DisplayName("GET /dashboard/stats -> new user with no applications gets all zeros")
    void getStats_zeroStateUser_returnsAllZeros() throws Exception {
        String freshToken = registerAndExtractJwt("charlie@nextround.dev", "Password123!", "Charlie", "Kim");

        mockMvc.perform(get("/api/v1/dashboard/stats")
                        .header("Authorization", "Bearer " + freshToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.totalApplications").value(0))
                .andExpect(jsonPath("$.applied").value(0))
                .andExpect(jsonPath("$.interviewing").value(0))
                .andExpect(jsonPath("$.offers").value(0))
                .andExpect(jsonPath("$.rejected").value(0));
    }

    // ---------- activity ----------

    @Test
    @DisplayName("GET /dashboard/activity -> returns 12 weekly buckets, 6 monthly buckets, and 400 on invalid granularity")
    void getActivity_returnsZeroFilledBucketsAndRejectsInvalidGranularity() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/activity")
                        .param("granularity", "WEEK")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granularity").value("WEEK"))
                .andExpect(jsonPath("$.data", hasSize(12)))
                .andExpect(jsonPath("$.data[10].count").value(1))
                .andExpect(jsonPath("$.data[11].count").value(2));

        mockMvc.perform(get("/api/v1/dashboard/activity")
                        .param("granularity", "MONTH")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.granularity").value("MONTH"))
                .andExpect(jsonPath("$.data", hasSize(6)));

        mockMvc.perform(get("/api/v1/dashboard/activity")
                        .param("granularity", "YEAR")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isBadRequest());
    }

    // ---------- auth ----------

    @Test
    @DisplayName("Unauthenticated dashboard requests are rejected with 401")
    void dashboardEndpoints_rejectUnauthenticatedRequests() throws Exception {
        mockMvc.perform(get("/api/v1/dashboard/stats"))
                .andExpect(status().isUnauthorized());

        mockMvc.perform(get("/api/v1/dashboard/activity"))
                .andExpect(status().isUnauthorized());
    }
}