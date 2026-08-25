package com.wakiyak.jobapplicationtracker.controller;

import com.wakiyak.jobapplicationtracker.dtos.CompanyRequest;
import com.wakiyak.jobapplicationtracker.dtos.CompanyResponse;
import com.wakiyak.jobapplicationtracker.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("api/v1/companies")
public class CompanyController {
    private final CompanyService companyService;

    CompanyController(CompanyService companyService){
        this.companyService = companyService;
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

    public ResponseEntity<List<CompanyResponse>> getAllCompanies(){
        return ResponseEntity.ok(companyService.getAllCompanies());
    }

    @PatchMapping("/{id}")
    public ResponseEntity<CompanyResponse> updateCompany(@PathVariable UUID id, @RequestBody CompanyRequest companyRequest){
        CompanyResponse updatedCompany = companyService.editCompany(id, companyRequest);
        return ResponseEntity.ok(updatedCompany);
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCompany(@PathVariable UUID id){
        companyService.deleteCompany(id);
        return ResponseEntity.noContent().build();
    }

}
