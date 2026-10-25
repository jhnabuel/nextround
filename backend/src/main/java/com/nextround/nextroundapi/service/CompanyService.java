package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.CompanyRequest;
import com.nextround.nextroundapi.dtos.CompanyResponse;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.exception.CompanyAlreadyExistsException;
import com.nextround.nextroundapi.exception.CompanyHasLinkedApplicationsException;
import com.nextround.nextroundapi.exception.ResourceNotFoundException;
import com.nextround.nextroundapi.mapper.CompanyMapper;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@Transactional
public class CompanyService {
    private final CompanyRepository companyRepository;
    private final JobApplicationRepository jobApplicationRepository;

    CompanyService(CompanyRepository companyRepository, JobApplicationRepository jobApplicationRepository){
        this.companyRepository = companyRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    private Company getCompanyByIdInternal(UUID id){
        return companyRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Company with id: " + id + " does not exist."));
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(UUID id){
        return CompanyMapper.toDto(getCompanyByIdInternal(id));
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(Pageable pageable){
        return companyRepository.findAll(pageable).map(CompanyMapper::toDto);
    }

    @Transactional
    public CompanyResponse addCompany(CompanyRequest request){
        String trimmedName = request.companyName().trim();

        if (companyRepository.existsByCompanyNameIgnoreCase(trimmedName)) {
            throw new CompanyAlreadyExistsException("Company already exists with name: " + trimmedName);
        }

        Company company = new Company(
                trimmedName,
                request.websiteUrl() != null ? request.websiteUrl().trim() : null,
                request.industry() != null ? request.industry().trim() : null,
                request.location() != null ? request.location().trim() : null
        );

        Company savedCompany = companyRepository.save(company);
        return CompanyMapper.toDto(savedCompany);
    }


    public CompanyResponse editCompany(UUID id, CompanyRequest request){
        Company companyToBeEdited = getCompanyByIdInternal(id);
        companyToBeEdited.setCompanyName(request.companyName());
        companyToBeEdited.setWebsiteUrl(request.websiteUrl());
        companyToBeEdited.setIndustry(request.industry());
        companyToBeEdited.setLocation(request.location());

        Company updatedCompany = companyRepository.save(companyToBeEdited);
        return CompanyMapper.toDto(updatedCompany);
    }

    public void deleteCompany(UUID id){
        if(!companyRepository.existsById(id)){
            throw new ResourceNotFoundException("Company does not exist.");
        }

        if(jobApplicationRepository.existsByCompanyId(id)){
            throw new CompanyHasLinkedApplicationsException("Cannot delete company with id: " + id
                    + " because it has linked job applications.");
        }
        companyRepository.deleteById(id);
    }
}
