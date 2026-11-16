package com.nextround.nextroundapi.repository;

import com.nextround.nextroundapi.entity.JobApplication;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
    Optional<JobApplication> findByIdAndUserId(UUID id, UUID userId);

    // Paginated list for the currently logged-in user
    @EntityGraph(attributePaths = "company")
    Page<JobApplication> findAllByUserId(UUID userId, Pageable pageable);

    // Paginated list filtered by target company for the user
    Page<JobApplication> findByCompanyIdAndUserId(UUID companyId, UUID userId, Pageable pageable);

    //Paginated list filtered by status for the user
    Page<JobApplication> findByStatusAndUserId(ApplicationStatus status, UUID userId, Pageable pageable);


    boolean existsByCompanyId(UUID companyId);

    // JobApplicationRepository
    Page<JobApplication> findByCompanyIdAndStatusAndUserId(UUID companyId, ApplicationStatus status, UUID userId, Pageable pageable);
}
