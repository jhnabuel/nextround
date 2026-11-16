package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.CompanyRequest;
import com.nextround.nextroundapi.dtos.CompanyResponse;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.User;
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

    public CompanyService(CompanyRepository companyRepository,
                          JobApplicationRepository jobApplicationRepository) {
        this.companyRepository = companyRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    private Company getOwnedCompany(UUID id, UUID userId) {
        return companyRepository.findByIdAndUserId(id, userId)
                .orElseThrow(() -> new ResourceNotFoundException("Company with id: " + id + " does not exist."));
    }

    private static String clean(String value) {
        if (value == null) return null;
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    @Transactional(readOnly = true)
    public CompanyResponse getCompanyById(UUID id, User currentUser) {
        return CompanyMapper.toDto(getOwnedCompany(id, currentUser.getId()));
    }

    @Transactional(readOnly = true)
    public Page<CompanyResponse> getAllCompanies(String search, User currentUser, Pageable pageable) {
        String term = clean(search);
        Page<Company> page = (term == null)
                ? companyRepository.findByUserId(currentUser.getId(), pageable)
                : companyRepository.findByUserIdAndCompanyNameContainingIgnoreCase(
                currentUser.getId(), term, pageable);
        return page.map(CompanyMapper::toDto);
    }

    public CompanyResponse addCompany(CompanyRequest request, User currentUser) {
        String name = request.companyName().trim();

        if (companyRepository.existsByUserIdAndCompanyNameIgnoreCase(currentUser.getId(), name)) {
            throw new CompanyAlreadyExistsException("Company already exists with name: " + name);
        }

        Company company = new Company(name, clean(request.websiteUrl()),
                clean(request.industry()), clean(request.location()), currentUser);
        return CompanyMapper.toDto(companyRepository.save(company));
    }

    public CompanyResponse editCompany(UUID id, CompanyRequest request, User currentUser) {
        Company company = getOwnedCompany(id, currentUser.getId());
        String name = request.companyName().trim();

        if (!company.getCompanyName().equalsIgnoreCase(name)
                && companyRepository.existsByUserIdAndCompanyNameIgnoreCase(currentUser.getId(), name)) {
            throw new CompanyAlreadyExistsException("Company already exists with name: " + name);
        }

        company.setCompanyName(name);
        company.setWebsiteUrl(clean(request.websiteUrl()));
        company.setIndustry(clean(request.industry()));
        company.setLocation(clean(request.location()));

        // managed entity: dirty checking flushes on commit, no save() needed
        return CompanyMapper.toDto(company);
    }

    public void deleteCompany(UUID id, User currentUser) {
        Company company = getOwnedCompany(id, currentUser.getId());

        if (jobApplicationRepository.existsByCompanyId(id)) {
            throw new CompanyHasLinkedApplicationsException("Cannot delete company with id: " + id
                    + " because it has linked job applications.");
        }
        companyRepository.delete(company);
    }

    /** Resolves an existing company by id, or finds/creates one by name, scoped to the user. */
    public Company resolveCompanyForUser(UUID companyId, String companyName, User currentUser) {
        if (companyId != null) {
            return getOwnedCompany(companyId, currentUser.getId());
        }

        String name = clean(companyName);
        if (name == null) {
            throw new IllegalArgumentException("Either companyId or companyName must be provided.");
        }

        return companyRepository.findByUserIdAndCompanyNameIgnoreCase(currentUser.getId(), name)
                .orElseGet(() -> companyRepository.save(new Company(name, null, null, null, currentUser)));
    }
}
