package com.nextround.nextroundapi.repository;

import com.nextround.nextroundapi.entity.JobApplication;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {

    Page<JobApplication> findByUserId(UUID userId, Pageable pageable);
    Page<JobApplication> findByCompanyId(UUID companyId, Pageable pageable);
    Page<JobApplication> findByStatus(ApplicationStatus status, Pageable pageable);
}
