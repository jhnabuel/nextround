package com.nextround.nextroundapi.service;


import com.nextround.nextroundapi.dtos.CompanyResponse;
import com.nextround.nextroundapi.dtos.JobApplicationRequest;
import com.nextround.nextroundapi.dtos.JobApplicationResponse;
import com.nextround.nextroundapi.dtos.UserResponse;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.JobApplication;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class JobApplicationService {
    private final JobApplicationRepository jobApplicationRepository;
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;

    JobApplicationService(JobApplicationRepository jobApplicationRepository, UserRepository userRepository, CompanyRepository companyRepository){
        this.jobApplicationRepository = jobApplicationRepository;
        this.userRepository = userRepository;
        this.companyRepository = companyRepository;
    }

    private JobApplicationResponse mapToDto(JobApplication jobApplication){
        UserResponse userResponse = new UserResponse(
                jobApplication.getUser().getId(),
                jobApplication.getUser().getEmail(),
                jobApplication.getUser().getFirstName(),
                jobApplication.getUser().getLastName(),
                jobApplication.getUser().getCreatedAt(),
                jobApplication.getUser().getUpdatedAt()
        );

        CompanyResponse companyResponse = new CompanyResponse(
                jobApplication.getCompany().getId(),
                jobApplication.getCompany().getCompanyName(),
                jobApplication.getCompany().getWebsiteUrl(),
                jobApplication.getCompany().getIndustry(),
                jobApplication.getCompany().getLocation(),
                jobApplication.getCompany().getCreatedAt(),
                jobApplication.getCompany().getUpdatedAt()
        );



        return new JobApplicationResponse(jobApplication.getId(),
                userResponse,
                companyResponse,
                jobApplication.getJobTitle(),
                jobApplication.getJobURL(),
                jobApplication.getStatus(),
                jobApplication.getWorkLocation(),
                jobApplication.getSalaryMin(),
                jobApplication.getSalaryMax(),
                jobApplication.getCurrency(),
                jobApplication.getAppliedDate(),
                jobApplication.getCreatedAt(),
                jobApplication.getUpdatedAt());
    }

    private JobApplication getJobApplicationIdInternal(UUID id){
        return jobApplicationRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Job application with id: " + id + " does not exist."));
    }

    public  JobApplicationResponse getJobApplicationById(UUID id){
        return mapToDto(getJobApplicationIdInternal(id));
    }

    public JobApplicationResponse createJobApplication(JobApplicationRequest jobApplicationRequest){
        User user = userRepository.findById(jobApplicationRequest.userId()).orElseThrow(() -> new IllegalArgumentException("User not found."));
        Company company = companyRepository.findById(jobApplicationRequest.companyID()).orElseThrow(() -> new IllegalArgumentException("Company not found."));
        JobApplication newJobApplication = new JobApplication(user,
                company,
                jobApplicationRequest.jobTitle(),
                jobApplicationRequest.jobURL(),
                jobApplicationRequest.applicationStatus(),
                jobApplicationRequest.workLocation(),
                jobApplicationRequest.salaryMin(),
                jobApplicationRequest.salaryMax(),
                jobApplicationRequest.currency(),
                jobApplicationRequest.appliedDate());

        JobApplication addJobApplication = jobApplicationRepository.save(newJobApplication);
        return mapToDto(addJobApplication);
    }
}
