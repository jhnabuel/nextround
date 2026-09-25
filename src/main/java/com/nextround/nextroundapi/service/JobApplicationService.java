package com.nextround.nextroundapi.service;


import com.nextround.nextroundapi.dtos.CompanyResponse;
import com.nextround.nextroundapi.dtos.JobApplicationRequest;
import com.nextround.nextroundapi.dtos.JobApplicationResponse;
import com.nextround.nextroundapi.dtos.UserResponse;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.JobApplication;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.ApplicationStatus;
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
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    JobApplicationService(JobApplicationRepository jobApplicationRepository, UserRepository userRepository, CompanyRepository companyRepository){
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }
    private User getAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found."));
    }

    private void validateSalaryRange(JobApplicationRequest request) {
        if (request.salaryMin() != null && request.salaryMax() != null &&
                request.salaryMin().compareTo(request.salaryMax()) > 0) {
            throw new InvalidSalaryRangeException("Invalid range: Minimum salary cannot exceed maximum salary.");
        }
    }
    // ==========================================
    // READ (Tenant-Scoped)
    // ==========================================

    @Transactional(readOnly = true)
    public JobApplicationResponse getJobApplicationById(UUID id){
        User currentUser = getAuthenticatedUser();
        JobApplication application = jobApplicationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found with id: " + id));

        return JobApplicationMapper.toDto(application);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getAllJobApplicationsForCurrentUser(Pageable pageable){
        User currentUser = getAuthenticatedUser();
        return jobApplicationRepository.findByUserId(currentUser.getId(), pageable).map(JobApplicationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getApplicationsByCompanyId(UUID companyId, Pageable pageable){
        User currentUser = getAuthenticatedUser();
        return jobApplicationRepository.findByCompanyIdAndUserId(companyId, currentUser.getId(), pageable)
                .map(JobApplicationMapper::toDto);
    }

    @Transactional(readOnly = true)
    public Page<JobApplicationResponse> getApplicationsByStatus(ApplicationStatus status, Pageable pageable){
        return jobApplicationRepository.findByStatus(status, pageable).map(JobApplicationMapper::toDto);
    }


    @Transactional
    public JobApplicationResponse createJobApplication(JobApplicationRequest jobApplicationRequest){
        validateSalaryRange(jobApplicationRequest);

        User currentUser = getAuthenticatedUser();

        Company company = companyRepository.findById(jobApplicationRequest.companyId())
                .orElseThrow(() -> new ResourceNotFoundException("Company not found."));

        JobApplication newJobApplication = new JobApplication(currentUser,
                company,
                jobApplicationRequest.jobTitle(),
                jobApplicationRequest.jobUrl(),
                jobApplicationRequest.applicationStatus(),
                jobApplicationRequest.workLocation(),
                jobApplicationRequest.salaryMin(),
                jobApplicationRequest.salaryMax(),
                jobApplicationRequest.currency(),
                jobApplicationRequest.appliedDate());

        JobApplication addJobApplication = jobApplicationRepository.save(newJobApplication);
        return JobApplicationMapper.toDto(addJobApplication);
    }

    @Transactional
    public JobApplicationResponse editJobApplication(UUID id, JobApplicationRequest jobApplicationRequest){
        validateSalaryRange(jobApplicationRequest);

        User currentUser = getAuthenticatedUser();
        JobApplication jobApplicationToBeEdited = jobApplicationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found with id: " + id));


        if (!jobApplicationToBeEdited.getCompany().getId().equals(jobApplicationRequest.companyId())) {
            Company newCompany = companyRepository.findById(jobApplicationRequest.companyId())
                    .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + jobApplicationRequest.companyId()));
            jobApplicationToBeEdited.setCompany(newCompany);
        }

        jobApplicationToBeEdited.setJobTitle(jobApplicationRequest.jobTitle());
        jobApplicationToBeEdited.setJobUrl(jobApplicationRequest.jobUrl());
        jobApplicationToBeEdited.setStatus(jobApplicationRequest.applicationStatus());
        jobApplicationToBeEdited.setWorkLocation(jobApplicationRequest.workLocation());
        jobApplicationToBeEdited.setSalaryMin(jobApplicationRequest.salaryMin());
        jobApplicationToBeEdited.setSalaryMax(jobApplicationRequest.salaryMax());
        jobApplicationToBeEdited.setCurrency(jobApplicationRequest.currency());
        jobApplicationToBeEdited.setAppliedDate(jobApplicationRequest.appliedDate());

        return JobApplicationMapper.toDto(jobApplicationRepository.save(jobApplicationToBeEdited));
    }

    @Transactional
    public void deleteJobApplication(UUID id){
        User currentUser = getAuthenticatedUser();
        JobApplication application = jobApplicationRepository.findByIdAndUserId(id, currentUser.getId())
                .orElseThrow(() -> new ResourceNotFoundException("Job application not found with id: " + id));

        jobApplicationRepository.delete(application);
    }




}
