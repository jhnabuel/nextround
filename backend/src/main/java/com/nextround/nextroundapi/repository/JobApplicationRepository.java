package com.nextround.nextroundapi.repository;

import com.nextround.nextroundapi.entity.JobApplication;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static com.nextround.nextroundapi.enums.ApplicationStatus.WISHLIST;


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

    @Query("SELECT j.status AS status, COUNT(j) AS count FROM JobApplication AS j WHERE j.user.id = :userId AND j.status <> :excludedStatus  GROUP BY j.status")
    List<JobApplicationStatusCount> countByApplicationStatus(@Param("userId") UUID userId, @Param("excludedStatus")ApplicationStatus excludedStatus);

    @Query(value = """
        SELECT CAST(DATE_TRUNC(:granularity, COALESCE(j.applied_date, j.created_at)) AS DATE) AS period,
               COUNT(*) AS count
        FROM job_applications j
        WHERE j.user_id = :userId
          AND j.status <> 'WISHLIST'
          AND COALESCE(j.applied_date, j.created_at) >= :startDate
        GROUP BY period
        ORDER BY period ASC
        """, nativeQuery = true)
    List<JobApplicationActivityCount> countActivityByPeriod(
            @Param("userId") UUID userId,
            @Param("granularity") String granularity,
            @Param("startDate") LocalDateTime startDate
    );


    @Modifying
    @Query("DELETE FROM JobApplication j WHERE j.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}
