package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.JobApplicationRequest;
import com.nextround.nextroundapi.dtos.JobApplicationResponse;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.JobApplication;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.enums.Role;
import com.nextround.nextroundapi.enums.WorkLocationType;
import com.nextround.nextroundapi.exception.InvalidSalaryRangeException;
import com.nextround.nextroundapi.exception.ResourceNotFoundException;
import com.nextround.nextroundapi.exception.UnauthorizedException;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.UserRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private Authentication authentication;

    @Mock
    private SecurityContext securityContext;

    @InjectMocks
    private JobApplicationService jobApplicationService;

    private User currentUser;
    private UUID currentUserId;
    private Company sampleCompany;
    private UUID companyId;
    private JobApplication sampleApplication;
    private UUID applicationId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        currentUser = new User("john@example.com", "hash", "John", "Doe", Role.USER);
        currentUser.setId(currentUserId);

        companyId = UUID.randomUUID();
        sampleCompany = new Company("Vercel", "https://vercel.com/careers", "Tech", "Remote");
        sampleCompany.setId(companyId);

        applicationId = UUID.randomUUID();
        sampleApplication = new JobApplication(
                currentUser,
                sampleCompany,
                "Backend Engineer",
                "https://stripe.com/jobs/123",
                ApplicationStatus.APPLIED,
                WorkLocationType.REMOTE,
                BigDecimal.valueOf(90000),
                BigDecimal.valueOf(120000),
                "USD",
                LocalDate.of(2026, 9, 1)
        );
        sampleApplication.setId(applicationId);

        SecurityContextHolder.setContext(securityContext);
    }

    @AfterEach
    void tearDown() {
        SecurityContextHolder.clearContext();
    }

    private void mockAuthenticatedUser() {
        when(securityContext.getAuthentication()).thenReturn(authentication);
        when(authentication.isAuthenticated()).thenReturn(true);
        when(authentication.getName()).thenReturn("john@example.com");
        when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(currentUser));
    }
    @Nested
    @DisplayName("Create Job Application Tests")
    class CreateJobApplicationTests {
        @Test
        @DisplayName("Should create application successfully when data and salary range are valid")
        void createJobApplication_Success() {
            mockAuthenticatedUser();
            JobApplicationRequest request = new JobApplicationRequest(companyId,
                    "Backend Engineer",
                    "https://stripe.com/jobs/123",
                    ApplicationStatus.APPLIED,
                    WorkLocationType.REMOTE,
                    BigDecimal.valueOf(90000),
                    BigDecimal.valueOf(120000),
                    "USD",
                    LocalDate.of(2026, 9, 1));


            when(companyRepository.findById(companyId)).thenReturn(Optional.of(sampleCompany));
            when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(sampleApplication);

            JobApplicationResponse response = jobApplicationService.createJobApplication(request);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(applicationId);
            assertThat(response.jobTitle()).isEqualTo("Backend Engineer");
            assertThat(response.applicationStatus()).isEqualTo(ApplicationStatus.APPLIED);

            ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
            verify(jobApplicationRepository).save(captor.capture());
            JobApplication saved = captor.getValue();

            assertThat(saved.getUser().getId()).isEqualTo(currentUserId);
            assertThat(saved.getCompany().getId()).isEqualTo(companyId);
            assertThat(saved.getSalaryMin()).isEqualByComparingTo(BigDecimal.valueOf(90000));
        }


        @Test
        @DisplayName("Should throw InvalidSalaryRangeException when salaryMin exceeds salaryMax")
        void createJobApplication_InvalidSalaryRange_throwsException() {
            JobApplicationRequest request = new JobApplicationRequest(
                    companyId,
                    "Backend Engineer",
                    "https://stripe.com/jobs/123",
                    ApplicationStatus.APPLIED,
                    WorkLocationType.REMOTE,
                    BigDecimal.valueOf(150000), // salaryMin > salaryMax
                    BigDecimal.valueOf(100000),
                    "USD",
                    LocalDate.of(2026, 9, 1)
            );

            assertThatThrownBy(() -> jobApplicationService.createJobApplication(request))
                    .isInstanceOf(InvalidSalaryRangeException.class)
                    .hasMessageContaining("Minimum salary cannot exceed maximum salary");

            verify(companyRepository, never()).findById(any());
            verify(jobApplicationRepository, never()).save(any());
        }


        @Test
        @DisplayName("Should throw UnauthorizedException when security context has no authentication")
        void createJobApplication_NotAuthenticated_ThrowsException() {
            JobApplicationRequest request = new JobApplicationRequest(
                    companyId,
                    "Backend Engineer",
                    "https://stripe.com/jobs/123",
                    ApplicationStatus.APPLIED,
                    WorkLocationType.REMOTE,
                    BigDecimal.valueOf(90000),
                    BigDecimal.valueOf(120000),
                    "USD",
                    LocalDate.of(2026, 9, 1)
            );

            when(securityContext.getAuthentication()).thenReturn(null);

            assertThatThrownBy(() -> jobApplicationService.createJobApplication(request))
                    .isInstanceOf(UnauthorizedException.class)
                    .hasMessageContaining("User is not authenticated");

            verify(jobApplicationRepository, never()).save(any());
        }

    }
    @Nested
    @DisplayName("Delete Job Application Tests")
    class DeleteJobApplicationTests{
        @Test
        @DisplayName("Should delete application entity when tenant ownership is verified")
        void deleteJobApplication_Success() {
            mockAuthenticatedUser();
            when(jobApplicationRepository.findByIdAndUserId(applicationId, currentUserId))
                    .thenReturn(Optional.of(sampleApplication));

            jobApplicationService.deleteJobApplication(applicationId);

            verify(jobApplicationRepository).delete(sampleApplication);
        }

        @Test
        @DisplayName("Should not delete and throw ResourceNotFoundException when application belongs to another user")
        void deleteJobApplication_UnauthorizedTenant_ThrowsException() {
            mockAuthenticatedUser();
            when(jobApplicationRepository.findByIdAndUserId(applicationId, currentUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobApplicationService.deleteJobApplication(applicationId))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(jobApplicationRepository, never()).delete(any());
        }
    }

    @Nested
    @DisplayName("Update Job Application Tests")
    class UpdateJobApplicationTests{
        @Test
        @DisplayName("Should update application and assign new company when companyId changes.")
        void editJobApplication_changeCompanyId_returnsJobApplicationResponse(){
            mockAuthenticatedUser();
            UUID newCompanyId = UUID.randomUUID();
            Company newCompany = new Company("Amazon", "https://amazon.com/careers", "Tech", "Seattle");
            newCompany.setId(newCompanyId);

            JobApplicationRequest request = new JobApplicationRequest(
                    newCompanyId, "Backend Engineer", "https://stripe.com/jobs/123",
                    ApplicationStatus.APPLIED, WorkLocationType.REMOTE,
                    BigDecimal.valueOf(90000), BigDecimal.valueOf(120000),
                    "USD", LocalDate.of(2026, 9, 1));

            when(jobApplicationRepository.findByIdAndUserId(applicationId, currentUserId))
                    .thenReturn(Optional.of(sampleApplication));
            when(companyRepository.findById(newCompanyId)).thenReturn(Optional.of(newCompany));
            when(jobApplicationRepository.save(any(JobApplication.class))).thenAnswer(inv -> inv.getArgument(0));

            JobApplicationResponse response = jobApplicationService.editJobApplication(applicationId, request);

            assertThat(response.company().id()).isEqualTo(newCompanyId);
            verify(companyRepository).findById(newCompanyId);

        }

        @Test
        @DisplayName("Should fail update when application belong to another user")
        void editJobApplication_UnauthorizedTenant_ThrowsException(){
            mockAuthenticatedUser();
            JobApplicationRequest request = new JobApplicationRequest(
                    companyId,
                    "Updated Title",
                    "https://example.com",
                    ApplicationStatus.OFFER_RECEIVED,
                    WorkLocationType.REMOTE,
                    BigDecimal.valueOf(100000),
                    BigDecimal.valueOf(120000),
                    "USD",
                    LocalDate.now()
            );

            when(jobApplicationRepository.findByIdAndUserId(applicationId, currentUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobApplicationService.editJobApplication(applicationId, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(jobApplicationRepository, never()).save(any());
        }
    }


    @Nested
    @DisplayName("Read and Tenant Isolation Tests")
    class ReadJobApplicationTests {
        @Test
        @DisplayName("Should return application by id scoped to authenticated user")
        void getJobApplicationById_shouldReturnJobApplicationResponse(){
            mockAuthenticatedUser();
            when(jobApplicationRepository.findByIdAndUserId(applicationId, currentUserId)).thenReturn(Optional.of(sampleApplication));

            JobApplicationResponse response = jobApplicationService.getJobApplicationById(applicationId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(applicationId);
            verify(jobApplicationRepository).findByIdAndUserId(applicationId, currentUserId);
        }

        @Test
        @DisplayName("Should return paginated list strictly scoped by current user ID")
        void getAllJobApplicationsForCurrentUser_shouldReturnPaginatedResponse(){
            mockAuthenticatedUser();
            Pageable pageable = PageRequest.of(0,10);
            Page<JobApplication> page = new PageImpl<>(List.of(sampleApplication));

            when(jobApplicationRepository.findAllByUserId(currentUserId, pageable)).thenReturn(page);

            Page<JobApplicationResponse> response = jobApplicationService.getAllJobApplicationsForCurrentUser(pageable);

            assertThat(response).isNotNull();
            assertThat(response.getContent()).hasSize(1);
            assertThat(response.getContent().getFirst().id()).isEqualTo(applicationId);
            verify(jobApplicationRepository).findAllByUserId(currentUserId, pageable);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when application belongs to another user")
        void getJobApplicationById_belongsToAnotherTenant_throwsException(){
            mockAuthenticatedUser();
            UUID randomApplicationId = UUID.randomUUID();

            when(jobApplicationRepository.findByIdAndUserId(randomApplicationId, currentUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobApplicationService.getJobApplicationById(randomApplicationId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Job application not found with id: " + randomApplicationId);

            verify(jobApplicationRepository).findByIdAndUserId(randomApplicationId, currentUserId);
        }
    }
}
