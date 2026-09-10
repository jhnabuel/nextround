package com.nextround.nextroundapi.controller;


import com.nextround.nextroundapi.dtos.JobApplicationRequest;
import com.nextround.nextroundapi.dtos.JobApplicationResponse;
import com.nextround.nextroundapi.enums.ApplicationStatus;
import com.nextround.nextroundapi.service.JobApplicationService;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/applications")
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    JobApplicationController(JobApplicationService jobApplicationService){
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping
    public ResponseEntity<JobApplicationResponse> createNewJobApplication(@Valid @RequestBody JobApplicationRequest jobApplicationRequest){
        JobApplicationResponse createdJobApplication = jobApplicationService.createJobApplication(jobApplicationRequest);
        return new ResponseEntity<>(createdJobApplication, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> getJobApplicationById(@PathVariable UUID id){
        return ResponseEntity.ok(jobApplicationService.getJobApplicationById(id));
    }

    @GetMapping
    public ResponseEntity<Page<JobApplicationResponse>> getAllJobApplications(@RequestParam(required = false)ApplicationStatus status, Pageable pageable){
        if (status != null){
            return ResponseEntity.ok(jobApplicationService.getApplicationsByStatus(status, pageable));
        }
        return ResponseEntity.ok(jobApplicationService.getAllJobApplications(pageable));
    }


    @PutMapping("/{id}")
    public ResponseEntity<JobApplicationResponse> updateJobApplication(@PathVariable UUID id,
                                                                       @Valid @RequestBody JobApplicationRequest jobApplicationRequest){
        JobApplicationResponse updatedJobApplication = jobApplicationService.editJobApplication(id, jobApplicationRequest);
        return ResponseEntity.ok(updatedJobApplication);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJobApplication(@PathVariable UUID id){
        jobApplicationService.deleteJobApplication(id);
        return ResponseEntity.noContent().build();
    }
}
