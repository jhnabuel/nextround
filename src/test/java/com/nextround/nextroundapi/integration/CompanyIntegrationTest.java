package com.nextround.nextroundapi.integration;

import com.nextround.nextroundapi.BaseIntegrationTest;
import com.nextround.nextroundapi.dtos.CompanyRequest;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.Role;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.UserRepository;
import com.nextround.nextroundapi.service.JwtService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

public class CompanyIntegrationTest extends BaseIntegrationTest {

    @Autowired
    private CompanyRepository companyRepository;

    @Autowired
    private JobApplicationRepository jobApplicationRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtService jwtService;

    private String userToken;
    private String adminToken;
    private Company savedCompany;

    @BeforeEach
    void setUp() {
        jobApplicationRepository.deleteAllInBatch();
        companyRepository.deleteAllInBatch();
        userRepository.deleteAllInBatch();

        User regularUser = userRepository.saveAndFlush(new User(
                "user@nextround.dev",
                passwordEncoder.encode("Password123!"),
                "Regular",
                "User",
                Role.USER
        ));
        userToken = jwtService.generateToken(regularUser);

        User adminUser = userRepository.saveAndFlush(new User(
                "admin@nextround.dev",
                passwordEncoder.encode("Password123!"),
                "Admin",
                "User",
                Role.ADMIN
        ));
        adminToken = jwtService.generateToken(adminUser);

        savedCompany = companyRepository.saveAndFlush(
                new Company("Canva", "https://canva.com", "Design & Tech", "Manila, Philippines")
        );
    }

    @Test
    @DisplayName("POST /api/v1/companies -> regular user can create new company")
    void createCompany_RegularUser_Success() throws Exception {
        CompanyRequest request = new CompanyRequest("Atlassian", "https://atlassian.com", "Tech", "Sydney");

        mockMvc.perform(post("/api/v1/companies")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.companyName").value("Atlassian"));

        assertThat(companyRepository.existsByCompanyNameIgnoreCase("Atlassian")).isTrue();
    }

    @Test
    @DisplayName("POST /api/v1/companies -> duplicate company name (case-insensitive) returns 409 Conflict")
    void createCompany_DuplicateName_ReturnsConflict() throws Exception {
        CompanyRequest duplicateRequest = new CompanyRequest(
                "canva", // Collides case-insensitively with "Canva"
                "https://canva.com",
                "Design",
                "Manila"
        );

        mockMvc.perform(post("/api/v1/companies")
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(duplicateRequest)))
                .andExpect(status().isConflict());
    }

    @Test
    @DisplayName("PUT /api/v1/companies/{id} -> regular user gets 403 Forbidden")
    void updateCompany_RegularUser_ReturnsForbidden() throws Exception {
        CompanyRequest updateRequest = new CompanyRequest("Canva Inc.", "https://canva.com", "Design", "Manila");

        mockMvc.perform(put("/api/v1/companies/" + savedCompany.getId())
                        .header("Authorization", "Bearer " + userToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("DELETE /api/v1/companies/{id} -> regular user gets 403 Forbidden")
    void deleteCompany_RegularUser_ReturnsForbidden() throws Exception {
        mockMvc.perform(delete("/api/v1/companies/" + savedCompany.getId())
                        .header("Authorization", "Bearer " + userToken))
                .andExpect(status().isForbidden());
    }

    @Test
    @DisplayName("PUT /api/v1/companies/{id} -> admin user succeeds (200 OK)")
    void updateCompany_AdminUser_Success() throws Exception {
        CompanyRequest updateRequest = new CompanyRequest("Canva Global", "https://canva.com", "Design & Tech", "Sydney");

        mockMvc.perform(put("/api/v1/companies/" + savedCompany.getId())
                        .header("Authorization", "Bearer " + adminToken)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.companyName").value("Canva Global"));

        Company updated = companyRepository.findById(savedCompany.getId()).orElseThrow();
        assertThat(updated.getCompanyName()).isEqualTo("Canva Global");
    }

    @Test
    @DisplayName("DELETE /api/v1/companies/{id} -> admin user succeeds (204 No Content)")
    void deleteCompany_AdminUser_Success() throws Exception {
        mockMvc.perform(delete("/api/v1/companies/" + savedCompany.getId())
                        .header("Authorization", "Bearer " + adminToken))
                .andExpect(status().isNoContent());

        assertThat(companyRepository.existsById(savedCompany.getId())).isFalse();
    }
}