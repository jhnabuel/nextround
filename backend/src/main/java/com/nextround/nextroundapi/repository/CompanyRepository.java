package com.nextround.nextroundapi.repository;

import com.nextround.nextroundapi.entity.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
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
}