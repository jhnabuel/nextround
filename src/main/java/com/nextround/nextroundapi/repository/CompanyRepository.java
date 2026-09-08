package com.nextround.nextroundapi.repository;

import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
}
