package com.nextround.nextroundapi.service;


import com.nextround.nextroundapi.dtos.CompanyRequest;
import com.nextround.nextroundapi.dtos.CompanyResponse;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.exception.CompanyHasLinkedApplicationsException;
import com.nextround.nextroundapi.exception.ResourceNotFoundException;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import org.checkerframework.checker.units.qual.C;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @InjectMocks
    private CompanyService companyService;

    @Test
    public void getCompanyId_whenCompanyExists_returnsCompanyResponse(){
        UUID companyId = UUID.randomUUID();

        Company mockCompany = new Company("Google", "https://google.com", "Tech", "San Francisco, CA");

        when(companyRepository.findById(companyId)).thenReturn(Optional.of(mockCompany));

        CompanyResponse result = companyService.getCompanyById(companyId);

        assertEquals("Google", result.companyName());
        verify(companyRepository, times(1)).findById(companyId);
    }

    @Test
    public void getCompanyById_whenCompanyDoesNotExist_throwsResourceNotFoundException() {
        // Arrange
        UUID companyId = UUID.randomUUID();
        when(companyRepository.findById(companyId)).thenReturn(Optional.empty());

        // Act & Assert
        assertThrows(ResourceNotFoundException.class, () -> companyService.getCompanyById(companyId));
    }

    @Test
    public void createCompany_withValidRequest_returnsCompanyResponse(){
        // Arrange
        UUID companyId = UUID.randomUUID();
        CompanyRequest request = new CompanyRequest("Amazon", "https://amazon.com", "Tech", "San Francisco, CA");
        Company savedCompany = new Company("Amazon", "https://amazon.com", "Tech", "San Francisco, CA");
        savedCompany.setId(companyId);

        when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

        //Act
        CompanyResponse result = companyService.addCompany(request);

        // Assert
        assertEquals(companyId, result.id());
        assertEquals("Amazon", result.companyName());
        assertNotNull(result.id());

        // Verify
        verify(companyRepository, times(1)).save(any(Company.class));
    }
    @Test
    public void deleteCompany_whenCompanyHasNoLinkedApplications_deletesCompany(){
        UUID companyId = UUID.randomUUID();

        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(jobApplicationRepository.existsByCompanyId(companyId)).thenReturn(false);

        doNothing().when(companyRepository).deleteById(companyId);

        companyService.deleteCompany(companyId);
        verify(companyRepository, times(1)).deleteById(companyId);
    }

    @Test
    public void deleteCompany_whenCompanyDoesNotExist_throwsException(){
        UUID companyId = UUID.randomUUID();

        when(companyRepository.existsById(companyId)).thenReturn(false);

        assertThrows(ResourceNotFoundException.class, () -> companyService.deleteCompany(companyId));

        verify(companyRepository, never()).deleteById(any());
        verifyNoInteractions(jobApplicationRepository);
    }

    @Test
    public void deleteCompany_whenLinkedApplicationsExist_throwsException(){
        UUID companyId = UUID.randomUUID();

        when(companyRepository.existsById(companyId)).thenReturn(true);
        when(jobApplicationRepository.existsByCompanyId(companyId)).thenReturn(true);

        assertThrows(CompanyHasLinkedApplicationsException.class, () -> companyService.deleteCompany(companyId));

        verify(companyRepository, never()).deleteById(any());
    }

    @Test
    public void updateCompany_shouldUpdateAndReturnUpdatedCompany_returnsCompanyResponse(){
        UUID companyId = UUID.randomUUID();
        Company existingCompany = new Company("Amazon", "https://amazon.com", "Tech", "San Francisco, CA");
        existingCompany.setId(companyId);

        CompanyRequest updateCompany = new CompanyRequest("Amazon", "https://amazon.com", "Tech", "Los Angeles, CA");



        when(companyRepository.findById(companyId)).thenReturn(Optional.of(existingCompany));
        when(companyRepository.save(any(Company.class))).thenAnswer(invocation -> invocation.getArgument(0));


        CompanyResponse result = companyService.editCompany(companyId, updateCompany);

        assertNotNull(result);
        assertEquals(companyId, result.id());
        assertEquals("Los Angeles, CA", result.location()); // Verifies the field was updated
        verify(companyRepository, times(1)).findById(companyId);
        verify(companyRepository, times(1)).save(any(Company.class));
    }

}
