package com.nextround.nextroundapi.service;


import com.nextround.nextroundapi.dtos.CompanyResponse;
import com.nextround.nextroundapi.dtos.JobApplicationRequest;
import com.nextround.nextroundapi.dtos.JobApplicationResponse;
import com.nextround.nextroundapi.dtos.UserResponse;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.JobApplication;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.enums.WorkLocationType;
import com.nextround.nextroundapi.exception.InvalidSalaryRangeException;
import com.nextround.nextroundapi.exception.ResourceNotFoundException;
import com.nextround.nextroundapi.exception.UnauthorizedException;
import com.nextround.nextroundapi.exception.UsernameNotFoundException;
import com.nextround.nextroundapi.mapper.JobApplicationMapper;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final CompanyService companyService;

    public JobApplicationService(JobApplicationRepository jobApplicationRepository,
                                 CompanyService companyService) {
        this.jobApplicationRepository = jobApplicationRepository;
        this.companyService = companyService;
    }

    private void validateSalaryRange(JobApplicationRequest request) {
        if (request.salaryMin() != null && request.salaryMax() != null
                && request.salaryMin().compareTo(request.salaryMax()) > 0) {
            throw new InvalidSalaryRangeException("Invalid range: Minimum salary cannot exceed maximum salary.");
        }
    }

    private JobApplication getOwnedApplication(UUID id, User user) {
        return jobApplicationRepository.findByIdAndUserId(id, user.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public JobApplicationResponse getJobApplicationById(UUID id, User user) {
        return JobApplicationMapper.toDto(getOwnedApplication(id, user));
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getApplications(
            UUID companyId, ApplicationStatus status, User user, Pageable pageable) {
        UUID userId = user.getId();
        Page<JobApplication> page;
        if (companyId != null && status != null) {
            page = jobApplicationRepository.findByCompanyIdAndStatusAndUserId(companyId, status, userId, pageable);
        } else if (companyId != null) {
            page = jobApplicationRepository.findByCompanyIdAndUserId(companyId, userId, pageable);
        } else if (status != null) {
            page = jobApplicationRepository.findByStatusAndUserId(status, userId, pageable);
        } else {
            page = jobApplicationRepository.findAllByUserId(userId, pageable);
        }
        return page.map(JobApplicationMapper::toDto);
    }

    public JobApplicationResponse createJobApplication(JobApplicationRequest request, User user) {
        validateSalaryRange(request);

        Company company = companyService.resolveCompanyForUser(
                request.companyId(), request.companyName(), user);

        JobApplication application = new JobApplication(user, company,
                request.jobTitle(), request.jobUrl(), request.applicationStatus(),
                request.workLocation(), request.salaryMin(), request.salaryMax(),
                request.currency(), request.appliedDate());

        return JobApplicationMapper.toDto(jobApplicationRepository.save(application));
    }

    public JobApplicationResponse editJobApplication(UUID id, JobApplicationRequest request, User user) {
        validateSalaryRange(request);

        JobApplication application = getOwnedApplication(id, user);
        application.setCompany(companyService.resolveCompanyForUser(
                request.companyId(), request.companyName(), user));

        application.setJobTitle(request.jobTitle());
        application.setJobUrl(request.jobUrl());
        application.setStatus(request.applicationStatus() != null
                ? request.applicationStatus() : ApplicationStatus.APPLIED);
        application.setWorkLocation(request.workLocation() != null
                ? request.workLocation() : WorkLocationType.ON_SITE);
        application.setSalaryMin(request.salaryMin());
        application.setSalaryMax(request.salaryMax());
        application.setCurrency(request.currency() != null ? request.currency() : "PHP");
        application.setAppliedDate(request.appliedDate() != null
                ? request.appliedDate() : application.getAppliedDate());

        return JobApplicationMapper.toDto(application); // managed entity, no save() needed
    }

    public void deleteJobApplication(UUID id, User user) {
        jobApplicationRepository.delete(getOwnedApplication(id, user));
    }
}