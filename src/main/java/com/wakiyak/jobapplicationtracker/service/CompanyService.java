package com.wakiyak.jobapplicationtracker.service;

import com.wakiyak.jobapplicationtracker.dtos.CompanyResponse;
import com.wakiyak.jobapplicationtracker.entity.Company;
import com.wakiyak.jobapplicationtracker.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.rmi.server.UID;
import java.util.UUID;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    CompanyService(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    private Company getCompanyByIdInternal(UUID id){
        return companyRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Company with id: " + id + " does not exist."));
    }

    private CompanyResponse mapToDto(Company company){
        return new CompanyResponse(company.getId(),
                company.getCompanyName(), company.getWebsiteUrl(), company.getLocation());
    }

    public CompanyResponse getCompanyById(UUID id){
        return mapToDto(getCompanyByIdInternal(id));
    }
}
