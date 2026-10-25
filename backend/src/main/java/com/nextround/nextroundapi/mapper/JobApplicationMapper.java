package com.nextround.nextroundapi.mapper;

import com.nextround.nextroundapi.dtos.JobApplicationResponse;
import com.nextround.nextroundapi.entity.JobApplication;

public class JobApplicationMapper {
    public static JobApplicationResponse toDto(JobApplication jobApplication){
        return new JobApplicationResponse(
                jobApplication.getId(),
                UserMapper.toDto(jobApplication.getUser()),
                CompanyMapper.toDto(jobApplication.getCompany()),
                jobApplication.getJobTitle(),
                jobApplication.getJobUrl(),
                jobApplication.getStatus(),
                jobApplication.getWorkLocation(),
                jobApplication.getSalaryMin(),
                jobApplication.getSalaryMax(),
                jobApplication.getCurrency(),
                jobApplication.getAppliedDate(),
                jobApplication.getCreatedAt(),
                jobApplication.getUpdatedAt()
        );
    }
}