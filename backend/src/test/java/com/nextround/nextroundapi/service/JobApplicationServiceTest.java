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
import com.nextround.nextroundapi.repository.JobApplicationRepository;
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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobApplicationServiceTest {

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private CompanyService companyService;

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
        currentUser = new User("john@nextround.dev", "hash", "John", "Doe", Role.USER);
        currentUser.setId(currentUserId);

        companyId = UUID.randomUUID();
        sampleCompany = new Company("Canva", "https://canva.com", "Design", "Manila", currentUser);
        sampleCompany.setId(companyId);

        applicationId = UUID.randomUUID();
        sampleApplication = new JobApplication(
                currentUser,
                sampleCompany,
                "Backend Engineer",
                "https://canva.com/jobs/1",
                ApplicationStatus.APPLIED,
                WorkLocationType.REMOTE,
                BigDecimal.valueOf(90000),
                BigDecimal.valueOf(120000),
                "PHP",
                LocalDate.of(2026, 11, 1)
        );
        sampleApplication.setId(applicationId);
    }

    @Nested
    @DisplayName("Create Job Application")
    class CreateJobApplicationTests {

        @Test
        @DisplayName("Should delegate to companyService.resolveCompanyForUser and persist application")
        void createJobApplication_Success() {
            JobApplicationRequest request = new JobApplicationRequest(
                    companyId, null, "Backend Engineer", "https://canva.com/jobs/1",
                    ApplicationStatus.APPLIED, WorkLocationType.REMOTE,
                    BigDecimal.valueOf(90000), BigDecimal.valueOf(120000), "PHP", LocalDate.of(2026, 11, 1)
            );

            when(companyService.resolveCompanyForUser(companyId, null, currentUser)).thenReturn(sampleCompany);
            when(jobApplicationRepository.save(any(JobApplication.class))).thenReturn(sampleApplication);

            JobApplicationResponse response = jobApplicationService.createJobApplication(request, currentUser);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(applicationId);
            assertThat(response.jobTitle()).isEqualTo("Backend Engineer");

            ArgumentCaptor<JobApplication> captor = ArgumentCaptor.forClass(JobApplication.class);
            verify(jobApplicationRepository).save(captor.capture());
            JobApplication saved = captor.getValue();
            assertThat(saved.getUser().getId()).isEqualTo(currentUserId);
            assertThat(saved.getCompany().getId()).isEqualTo(companyId);
        }

        @Test
        @DisplayName("Should throw InvalidSalaryRangeException when salaryMin > salaryMax")
        void createJobApplication_InvalidSalaryRange_ThrowsException() {
            JobApplicationRequest request = new JobApplicationRequest(
                    companyId, null, "Backend Engineer", null,
                    ApplicationStatus.APPLIED, WorkLocationType.REMOTE,
                    BigDecimal.valueOf(150000), BigDecimal.valueOf(100000), "PHP", null
            );

            assertThatThrownBy(() -> jobApplicationService.createJobApplication(request, currentUser))
                    .isInstanceOf(InvalidSalaryRangeException.class)
                    .hasMessageContaining("Minimum salary cannot exceed maximum salary");

            verifyNoInteractions(companyService);
            verify(jobApplicationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Read and Query Isolation")
    class ReadJobApplicationTests {

        @Test
        @DisplayName("Should fetch by application ID and user ID")
        void getJobApplicationById_Success() {
            when(jobApplicationRepository.findByIdAndUserId(applicationId, currentUserId))
                    .thenReturn(Optional.of(sampleApplication));

            JobApplicationResponse response = jobApplicationService.getJobApplicationById(applicationId, currentUser);

            assertThat(response.id()).isEqualTo(applicationId);
            verify(jobApplicationRepository).findByIdAndUserId(applicationId, currentUserId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException if application belongs to another tenant")
        void getJobApplicationById_NotOwned_ThrowsException() {
            when(jobApplicationRepository.findByIdAndUserId(applicationId, currentUserId))
                    .thenReturn(Optional.empty());

            assertThatThrownBy(() -> jobApplicationService.getJobApplicationById(applicationId, currentUser))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Job application not found with id: " + applicationId);
        }

        @Test
        @DisplayName("Should route to findByCompanyIdAndStatusAndUserId when both filters are set")
        void getApplications_CompanyAndStatus_RoutesCorrectly() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<JobApplication> page = new PageImpl<>(List.of(sampleApplication));

            when(jobApplicationRepository.findByCompanyIdAndStatusAndUserId(companyId, ApplicationStatus.APPLIED, currentUserId, pageable))
                    .thenReturn(page);

            Page<JobApplicationResponse> result = jobApplicationService.getApplications(companyId, ApplicationStatus.APPLIED, currentUser, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(jobApplicationRepository).findByCompanyIdAndStatusAndUserId(companyId, ApplicationStatus.APPLIED, currentUserId, pageable);
        }

        @Test
        @DisplayName("Should route to findAllByUserId when no filters are set")
        void getApplications_NoFilters_RoutesToFindAllByUserId() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<JobApplication> page = new PageImpl<>(List.of(sampleApplication));

            when(jobApplicationRepository.findAllByUserId(currentUserId, pageable)).thenReturn(page);

            Page<JobApplicationResponse> result = jobApplicationService.getApplications(null, null, currentUser, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(jobApplicationRepository).findAllByUserId(currentUserId, pageable);
        }
    }

    @Nested
    @DisplayName("Edit Job Application")
    class EditJobApplicationTests {

        @Test
        @DisplayName("Should update application and re-resolve company via companyService")
        void editJobApplication_Success() {
            UUID newCompanyId = UUID.randomUUID();
            Company newCompany = new Company("Atlassian", null, null, null, currentUser);
            newCompany.setId(newCompanyId);

            JobApplicationRequest request = new JobApplicationRequest(
                    newCompanyId, null, "Senior Engineer", null,
                    ApplicationStatus.INTERVIEWING, WorkLocationType.HYBRID,
                    BigDecimal.valueOf(100000), BigDecimal.valueOf(140000), "USD", null
            );

            when(jobApplicationRepository.findByIdAndUserId(applicationId, currentUserId)).thenReturn(Optional.of(sampleApplication));
            when(companyService.resolveCompanyForUser(newCompanyId, null, currentUser)).thenReturn(newCompany);

            JobApplicationResponse response = jobApplicationService.editJobApplication(applicationId, request, currentUser);

            assertThat(response.jobTitle()).isEqualTo("Senior Engineer");
            assertThat(sampleApplication.getCompany().getId()).isEqualTo(newCompanyId);
            assertThat(sampleApplication.getStatus()).isEqualTo(ApplicationStatus.INTERVIEWING);
            verify(jobApplicationRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Delete Job Application")
    class DeleteJobApplicationTests {

        @Test
        @DisplayName("Should delete application when verified owned by tenant")
        void deleteJobApplication_Success() {
            when(jobApplicationRepository.findByIdAndUserId(applicationId, currentUserId)).thenReturn(Optional.of(sampleApplication));

            jobApplicationService.deleteJobApplication(applicationId, currentUser);

            verify(jobApplicationRepository).delete(sampleApplication);
        }
    }
}