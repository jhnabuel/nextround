package com.nextround.nextroundapi.repository;

import com.nextround.nextroundapi.entity.JobApplication;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface JobApplicationRepository extends JpaRepository<JobApplication, UUID> {
}
