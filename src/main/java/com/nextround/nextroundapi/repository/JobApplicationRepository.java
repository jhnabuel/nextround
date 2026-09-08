package com.nextround.nextroundapi.repository;

import com.nextround.nextroundapi.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    List<JobApplication> findByUserId(UUID userId);
    List<JobApplication> findByCompanyId(UUID companyId);

}
