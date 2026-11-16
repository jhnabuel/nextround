package com.nextround.nextroundapi.controller;


import com.nextround.nextroundapi.dtos.JobApplicationRequest;
import com.nextround.nextroundapi.dtos.JobApplicationResponse;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    public JobApplicationController(JobApplicationService jobApplicationService) {
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> create(
            @Valid @RequestBody JobApplicationRequest request,
            @AuthenticationPrincipal User currentUser) {
        return new ResponseEntity<>(
                jobApplicationService.createJobApplication(request, currentUser), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getById(
            @PathVariable UUID id, @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(jobApplicationService.getJobApplicationById(id, currentUser));
    }

    @GetMapping
    public ResponseEntity<Page<JobApplicationResponse>> getAll(
            @RequestParam(required = false) ApplicationStatus status,
            @RequestParam(required = false) UUID companyId,
            @AuthenticationPrincipal User currentUser,
            @PageableDefault(size = 15, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(
                jobApplicationService.getApplications(companyId, status, currentUser, pageable));
    }

    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> update(
            @PathVariable UUID id,
            @Valid @RequestBody JobApplicationRequest request,
            @AuthenticationPrincipal User currentUser) {
        return ResponseEntity.ok(jobApplicationService.editJobApplication(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(
            @PathVariable UUID id, @AuthenticationPrincipal User currentUser) {
        jobApplicationService.deleteJobApplication(id, currentUser);
        return ResponseEntity.noContent().build();
    }
}