package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.CompanyRequest;
import com.nextround.nextroundapi.dtos.CompanyResponse;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.exception.ResourceNotFoundException;
import com.nextround.nextroundapi.repository.CompanyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class CompanyService {
    private final CompanyRepository companyRepository;

    CompanyService(CompanyRepository companyRepository){
        this.companyRepository = companyRepository;
    }

    private Company getCompanyByIdInternal(UUID id){
        return companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Company with id: " + id + " does not exist."));
    }

    private CompanyResponse mapToDto(Company company){
        return new CompanyResponse(company.getId(),
                company.getCompanyName(), company.getWebsiteUrl(), company.getIndustry(), company.getLocation(), company.getCreatedAt(), company.getUpdatedAt());
    }

    public CompanyResponse getCompanyById(UUID id){
        return mapToDto(getCompanyByIdInternal(id));
    }

    public List<CompanyResponse> getAllCompanies(){
        return companyRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public CompanyResponse addCompany(CompanyRequest requestDTO){
        Company newCompany = new Company(requestDTO.companyName(), requestDTO.websiteUrl(), requestDTO.industry(), requestDTO.location());
        Company savedCompany = companyRepository.save(newCompany);
        return mapToDto(savedCompany);
    }

    public CompanyResponse editCompany(UUID id, CompanyRequest request){
        Company companyToBeEdited = getCompanyByIdInternal(id);
        companyToBeEdited.setCompanyName(request.companyName());
        companyToBeEdited.setWebsiteUrl(request.websiteUrl());
        companyToBeEdited.setIndustry(request.industry());
        companyToBeEdited.setLocation(request.location());

        Company updatedCompany = companyRepository.save(companyToBeEdited);
        return mapToDto(updatedCompany);
    }

    public void deleteCompany(UUID id){
        if(!companyRepository.existsById(id)){
            throw new ResourceNotFoundException("Company does not exist.");
        }
        companyRepository.deleteById(id);
    }
}
