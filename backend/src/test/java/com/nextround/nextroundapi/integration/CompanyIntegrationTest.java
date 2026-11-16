package com.nextround.nextroundapi.integration;

import com.fasterxml.jackson.databind.JsonNode;
import com.nextround.nextroundapi.BaseIntegrationTest;
import com.nextround.nextroundapi.dtos.CompanyRequest;
import com.nextround.nextroundapi.dtos.JobApplicationRequest;
import com.nextround.nextroundapi.dtos.RegisterRequest;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.UserRepository;
import com.nextround.nextroundapi.service.CompanyService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MvcResult;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CompanyIntegrationTest extends BaseIntegrationTest {

    @Autowired private CompanyRepository companyRepository;
    @Autowired private JobApplicationRepository jobApplicationRepository;
    @Autowired private UserRepository userRepository;
    @Autowired private CompanyService companyService;

    private String userAToken;
    private String userBToken;
    private User userA;
    private User userB;

    @BeforeEach
    void setUp() throws Exception {
        jobApplicationRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        userAToken = registerAndExtractJwt("alice@nextround.dev", "Password123!", "Alice", "Smith");
        userA = userRepository.findByEmail("alice@nextround.dev").orElseThrow();

        userBToken = registerAndExtractJwt("bob@nextround.dev", "Password123!", "Bob", "Jones");
        userB = userRepository.findByEmail("bob@nextround.dev").orElseThrow();
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

    // ---------- tenant isolation ----------

    @Test
    @DisplayName("GET /companies/{id} -> User B cannot read User A's company (404)")
    void userCannotReadAnotherUsersCompany() throws Exception {
        Company companyA = createCompany(userAToken, "Stripe");

        mockMvc.perform(get("/api/v1/companies/{id}", companyA.getId())
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("PATCH /companies/{id} -> User B cannot edit User A's company (404), data unchanged")
    void userCannotEditAnotherUsersCompany() throws Exception {
        Company companyA = createCompany(userAToken, "Stripe");
        CompanyRequest update = new CompanyRequest("Stripe Global", null, "Tech", "Remote");

        mockMvc.perform(patch("/api/v1/companies/{id}", companyA.getId())
                        .header("Authorization", "Bearer " + userBToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(update)))
                .andExpect(status().isNotFound());

        assertThat(companyRepository.findById(companyA.getId()).orElseThrow().getCompanyName())
                .isEqualTo("Stripe");
    }

    @Test
    @DisplayName("DELETE /companies/{id} -> User B cannot delete User A's company (404)")
    void userCannotDeleteAnotherUsersCompany() throws Exception {
        Company companyA = createCompany(userAToken, "Stripe");

        mockMvc.perform(delete("/api/v1/companies/{id}", companyA.getId())
                        .header("Authorization", "Bearer " + userBToken))
                .andExpect(status().isNotFound());

        assertThat(companyRepository.existsById(companyA.getId())).isTrue();
    }

    @Test
    @DisplayName("Different users can create companies with the same name")
    void differentUsersCanCreateSameCompanyWithoutConflict() throws Exception {
        createCompany(userAToken, "Google");
        createCompany(userBToken, "Google");

        assertThat(companyRepository.findByUserId(userA.getId(), Pageable.unpaged()).getTotalElements()).isEqualTo(1);
        assertThat(companyRepository.findByUserId(userB.getId(), Pageable.unpaged()).getTotalElements()).isEqualTo(1);
    }

    // ---------- conflicts ----------

    @Test
    @DisplayName("Same user creating a duplicate (case-insensitive) name returns 409")
    void sameUserCreatingDuplicateCompany_ReturnsConflict() throws Exception {
        createCompany(userAToken, "Google");

        CompanyRequest duplicate = new CompanyRequest("google", null, "Tech", "Remote");
        mockMvc.perform(post("/api/v1/companies")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicate)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("DELETE -> 409 when the company has linked applications")
    void deleteCompanyWithApplications_ReturnsConflict() throws Exception {
        Company company = createCompany(userAToken, "Stripe");
        JobApplicationRequest app = new JobApplicationRequest(
                company.getId(), null, "Backend Engineer", null, null, null, null, null, null, null);

        mockMvc.perform(post("/api/v1/applications")
                        .header("Authorization", "Bearer " + userAToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(app)))
                .andExpect(status().isCreated());

        mockMvc.perform(delete("/api/v1/companies/{id}", company.getId())
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isConflict());

        assertThat(companyRepository.existsById(company.getId())).isTrue();
    }

    // ---------- search ----------

    @Test
    @DisplayName("Search returns matches for the current user only, sorted by name")
    void searchCompanies_FiltersAndEnforcesTenantIsolation() throws Exception {
        createCompany(userAToken, "Google");
        createCompany(userAToken, "GoodRx");
        createCompany(userAToken, "Canva");
        createCompany(userBToken, "Google Cloud");

        mockMvc.perform(get("/api/v1/companies")
                        .param("search", "goo")
                        .param("sort", "companyName,asc")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(2))
                .andExpect(jsonPath("$.content[0].companyName").value("GoodRx"))
                .andExpect(jsonPath("$.content[1].companyName").value("Google"));
    }

    @Test
    @DisplayName("Search with no matches returns an empty page")
    void searchWithNoMatches_ReturnsEmptyPage() throws Exception {
        createCompany(userAToken, "Google");

        mockMvc.perform(get("/api/v1/companies")
                        .param("search", "zzz")
                        .header("Authorization", "Bearer " + userAToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content.length()").value(0));
    }

    // ---------- auth ----------

    @Test
    @DisplayName("Unauthenticated request is rejected with 401")
    void unauthenticatedRequest_IsRejected() throws Exception {
        mockMvc.perform(get("/api/v1/companies"))
                .andExpect(status().isUnauthorized());
    }

    // ---------- concurrency ----------

    @Test
    @DisplayName("Concurrent find-or-create (mixed case) yields exactly one row")
    void concurrentCreatesOfSameCompanyYieldOneRow() throws Exception {
        String[] names = {"Atlassian", "atlassian", "ATLASSIAN", "Atlassian"};
        int threads = names.length;
        ExecutorService executor = Executors.newFixedThreadPool(threads);
        CountDownLatch startLatch = new CountDownLatch(1);
        CountDownLatch finishLatch = new CountDownLatch(threads);
        List<Exception> errors = Collections.synchronizedList(new ArrayList<>());
        AtomicInteger successCount = new AtomicInteger();

        for (String name : names) {
            executor.submit(() -> {
                try {
                    startLatch.await();
                    companyService.resolveCompanyForUser(null, name, userA);
                    successCount.incrementAndGet();
                } catch (Exception e) {
                    errors.add(e);
                } finally {
                    finishLatch.countDown();
                }
            });
        }

        startLatch.countDown();
        boolean completed = finishLatch.await(10, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).as("threads finished; errors: %s", errors).isTrue();
        assertThat(successCount.get() + errors.size()).isEqualTo(threads);
        assertThat(successCount.get()).isGreaterThanOrEqualTo(1);

        long rows = companyRepository.findByUserId(userA.getId(), Pageable.unpaged())
                .stream()
                .filter(c -> c.getCompanyName().equalsIgnoreCase("Atlassian"))
                .count();
        assertThat(rows).as("errors: %s", errors).isEqualTo(1);
    }
}