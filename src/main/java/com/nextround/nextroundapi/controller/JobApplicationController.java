package com.nextround.nextroundapi.controller;


import com.nextround.nextroundapi.service.JobApplicationService;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class JobApplicationController {
    private final JobApplicationService jobApplicationService;

    JobApplicationController(JobApplicationService jobApplicationService){
        this.jobApplicationService = jobApplicationService;
    }
}
