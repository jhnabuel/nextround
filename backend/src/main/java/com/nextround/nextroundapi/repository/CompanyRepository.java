package com.nextround.nextroundapi.repository;

import com.nextround.nextroundapi.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {

    Optional<Company> findByIdAndUserId(UUID id, UUID userId);

    Page<Company> findByUserId(UUID userId, Pageable pageable);

    Page<Company> findByUserIdAndCompanyNameContainingIgnoreCase(UUID userId, String companyName, Pageable pageable);

    Optional<Company> findByUserIdAndCompanyNameIgnoreCase(UUID userId, String companyName);

    boolean existsByUserIdAndCompanyNameIgnoreCase(UUID userId, String companyName);

    boolean existsByIdAndUserId(UUID id, UUID userId);

    @Modifying
    @Query("DELETE FROM Company c WHERE c.user.id = :userId")
    void deleteAllByUserId(@Param("userId") UUID userId);
}