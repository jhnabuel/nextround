package com.nextround.nextroundapi.controller;

import com.nextround.nextroundapi.dtos.CompanyRequest;
import com.nextround.nextroundapi.dtos.CompanyResponse;
import com.nextround.nextroundapi.dtos.JobApplicationResponse;
import com.nextround.nextroundapi.service.CompanyService;
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
@RequestMapping("api/v1/companies")
public class CompanyController {
    private final CompanyService companyService;
    private final JobApplicationService jobApplicationService;

    CompanyController(CompanyService companyService, JobApplicationService jobApplicationService){
        this.companyService = companyService;
        this.jobApplicationService = jobApplicationService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> createCompany(@Valid @RequestBody CompanyRequest companyRequest){
        CompanyResponse newCompany = companyService.addCompany(companyRequest);
        return new ResponseEntity<>(newCompany, HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getCompanyById(@PathVariable UUID id){
        return ResponseEntity.ok(companyService.getCompanyById(id));
    }

    @GetMapping
    public ResponseEntity<Page<CompanyResponse>> getAllCompanies(Pageable pageable){
        return ResponseEntity.ok(companyService.getAllCompanies(pageable));
    }

    @GetMapping("/{id}/applications")
    public ResponseEntity<Page<JobApplicationResponse>> getApplicationsByCompany(@PathVariable UUID id, Pageable pageable){
        return ResponseEntity.ok(jobApplicationService.getApplicationsByCompanyId(id, pageable));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable UUID id, @Valid @RequestBody CompanyRequest companyRequest){
        CompanyResponse updatedCompany = companyService.editCompany(id, companyRequest);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id){
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

}
