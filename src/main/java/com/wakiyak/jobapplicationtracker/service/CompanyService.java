package com.wakiyak.jobapplicationtracker.service;

import com.wakiyak.jobapplicationtracker.dtos.CompanyRequest;
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
                company.getCompanyName(), company.getWebsiteUrl(), company.getLocation(), company.getCreatedAt(), company.getUpdatedAt());
    }

    public CompanyResponse getCompanyById(UUID id){
        return mapToDto(getCompanyByIdInternal(id));
    }

    public CompanyResponse addCompany(CompanyRequest requestDTO){
        Company newCompany = new Company(requestDTO.companyName(), requestDTO.websiteUrl(), requestDTO.location());
        Company savedCompany = companyRepository.save(newCompany);
        return mapToDto(savedCompany);
    }

    public CompanyResponse editCompany(UUID id, CompanyRequest request){
        Company companyToBeEdited = getCompanyByIdInternal(id);
        companyToBeEdited.setCompanyName(request.companyName());
        companyToBeEdited.setWebsiteUrl(request.websiteUrl());
        companyToBeEdited.setLocation(request.location());

        Company updatedCompany = companyRepository.save(companyToBeEdited);
        return mapToDto(updatedCompany);
    }

    public void deleteCompany(UUID id){
        if(!companyRepository.existsById(id)){
            throw new IllegalArgumentException("Company with id: " + id + " does not exist.");
        }
        companyRepository.deleteById(id);
    }
}
