package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.CompanyRequest;
import com.nextround.nextroundapi.dtos.CompanyResponse;
import com.nextround.nextroundapi.entity.Company;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.Role;
import com.nextround.nextroundapi.exception.CompanyAlreadyExistsException;
import com.nextround.nextroundapi.exception.CompanyHasLinkedApplicationsException;
import com.nextround.nextroundapi.exception.ResourceNotFoundException;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @InjectMocks
    private CompanyService companyService;

    private User currentUser;
    private UUID currentUserId;
    private Company sampleCompany;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        currentUserId = UUID.randomUUID();
        currentUser = new User("user@nextround.dev", "hash", "John", "Doe", Role.USER);
        currentUser.setId(currentUserId);

        companyId = UUID.randomUUID();
        sampleCompany = new Company("Google", "https://google.com", "Tech", "Mountain View", currentUser);
        sampleCompany.setId(companyId);
    }

    @Nested
    @DisplayName("Get Company By ID")
    class GetCompanyByIdTests {

        @Test
        @DisplayName("Should return company response when owned by user")
        void getCompanyById_Success() {
            when(companyRepository.findByIdAndUserId(companyId, currentUserId)).thenReturn(Optional.of(sampleCompany));

            CompanyResponse result = companyService.getCompanyById(companyId, currentUser);

            assertThat(result).isNotNull();
            assertThat(result.companyName()).isEqualTo("Google");
            verify(companyRepository).findByIdAndUserId(companyId, currentUserId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when company is not owned or absent")
        void getCompanyById_NotFound_ThrowsException() {
            when(companyRepository.findByIdAndUserId(companyId, currentUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.getCompanyById(companyId, currentUser))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("Company with id: " + companyId + " does not exist.");
        }
    }

    @Nested
    @DisplayName("Get All Companies (Search & Pagination)")
    class GetAllCompaniesTests {

        @Test
        @DisplayName("Should query by user ID only when search term is null or empty")
        void getAllCompanies_WithoutSearch_ReturnsAllUserCompanies() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Company> page = new PageImpl<>(List.of(sampleCompany));

            when(companyRepository.findByUserId(currentUserId, pageable)).thenReturn(page);

            Page<CompanyResponse> result = companyService.getAllCompanies("   ", currentUser, pageable);

            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().getFirst().companyName()).isEqualTo("Google");
            verify(companyRepository).findByUserId(currentUserId, pageable);
            verify(companyRepository, never()).findByUserIdAndCompanyNameContainingIgnoreCase(any(), any(), any());
        }

        @Test
        @DisplayName("Should query by user ID and trimmed search term when search is provided")
        void getAllCompanies_WithSearch_ReturnsFilteredCompanies() {
            Pageable pageable = PageRequest.of(0, 10);
            Page<Company> page = new PageImpl<>(List.of(sampleCompany));

            when(companyRepository.findByUserIdAndCompanyNameContainingIgnoreCase(currentUserId, "goo", pageable))
                    .thenReturn(page);

            Page<CompanyResponse> result = companyService.getAllCompanies(" goo ", currentUser, pageable);

            assertThat(result.getContent()).hasSize(1);
            verify(companyRepository).findByUserIdAndCompanyNameContainingIgnoreCase(currentUserId, "goo", pageable);
        }
    }

    @Nested
    @DisplayName("Add Company")
    class AddCompanyTests {

        @Test
        @DisplayName("Should trim values, persist company with current user, and return response")
        void addCompany_Success() {
            CompanyRequest request = new CompanyRequest("  Amazon  ", " https://amazon.com ", " Tech ", " Seattle ");
            Company savedCompany = new Company("Amazon", "https://amazon.com", "Tech", "Seattle", currentUser);
            savedCompany.setId(UUID.randomUUID());

            when(companyRepository.existsByUserIdAndCompanyNameIgnoreCase(currentUserId, "Amazon")).thenReturn(false);
            when(companyRepository.save(any(Company.class))).thenReturn(savedCompany);

            CompanyResponse result = companyService.addCompany(request, currentUser);

            assertThat(result.companyName()).isEqualTo("Amazon");

            ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
            verify(companyRepository).save(captor.capture());
            Company captured = captor.getValue();
            assertThat(captured.getCompanyName()).isEqualTo("Amazon");
            assertThat(captured.getWebsiteUrl()).isEqualTo("https://amazon.com");
            assertThat(captured.getUser().getId()).isEqualTo(currentUserId);
        }

        @Test
        @DisplayName("Should throw CompanyAlreadyExistsException when name exists for current user")
        void addCompany_DuplicateNameForSameUser_ThrowsException() {
            CompanyRequest request = new CompanyRequest("google", "https://google.com", null, null);
            when(companyRepository.existsByUserIdAndCompanyNameIgnoreCase(currentUserId, "google")).thenReturn(true);

            assertThatThrownBy(() -> companyService.addCompany(request, currentUser))
                    .isInstanceOf(CompanyAlreadyExistsException.class)
                    .hasMessageContaining("Company already exists with name: google");

            verify(companyRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("Edit Company")
    class EditCompanyTests {

        @Test
        @DisplayName("Should update all fields on the managed entity without calling save")
        void editCompany_Success() {
            CompanyRequest request = new CompanyRequest("Alphabet", "https://abc.xyz", "Holding", "Mountain View");

            when(companyRepository.findByIdAndUserId(companyId, currentUserId)).thenReturn(Optional.of(sampleCompany));
            when(companyRepository.existsByUserIdAndCompanyNameIgnoreCase(currentUserId, "Alphabet")).thenReturn(false);

            CompanyResponse result = companyService.editCompany(companyId, request, currentUser);

            assertThat(result.companyName()).isEqualTo("Alphabet");
            assertThat(sampleCompany.getCompanyName()).isEqualTo("Alphabet");
            assertThat(sampleCompany.getWebsiteUrl()).isEqualTo("https://abc.xyz");
            assertThat(sampleCompany.getIndustry()).isEqualTo("Holding");
            assertThat(sampleCompany.getLocation()).isEqualTo("Mountain View");
            verify(companyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should skip the collision check when only the name's case changes")
        void editCompany_SameNameDifferentCase_SkipsCollisionCheck() {
            CompanyRequest request = new CompanyRequest("google", "https://google.com", "Tech", "Mountain View");
            when(companyRepository.findByIdAndUserId(companyId, currentUserId)).thenReturn(Optional.of(sampleCompany));

            companyService.editCompany(companyId, request, currentUser);

            assertThat(sampleCompany.getCompanyName()).isEqualTo("google");
            verify(companyRepository, never()).existsByUserIdAndCompanyNameIgnoreCase(any(), any());
        }

        @Test
        @DisplayName("Should convert blank optional fields to null")
        void editCompany_BlankOptionalFields_BecomeNull() {
            CompanyRequest request = new CompanyRequest("Google", "   ", "   ", "   ");
            when(companyRepository.findByIdAndUserId(companyId, currentUserId)).thenReturn(Optional.of(sampleCompany));

            companyService.editCompany(companyId, request, currentUser);

            assertThat(sampleCompany.getWebsiteUrl()).isNull();
            assertThat(sampleCompany.getIndustry()).isNull();
            assertThat(sampleCompany.getLocation()).isNull();
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when editing a company the user does not own")
        void editCompany_NotOwned_ThrowsNotFound() {
            when(companyRepository.findByIdAndUserId(companyId, currentUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.editCompany(
                    companyId, new CompanyRequest("X", null, "Tech", "Remote"), currentUser))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(companyRepository, never()).existsByUserIdAndCompanyNameIgnoreCase(any(), any());
        }

    }

    @Nested
    @DisplayName("Delete Company")
    class DeleteCompanyTests {

        @Test
        @DisplayName("Should delete company when owned and no linked job applications exist")
        void deleteCompany_Success() {
            when(companyRepository.findByIdAndUserId(companyId, currentUserId)).thenReturn(Optional.of(sampleCompany));
            when(jobApplicationRepository.existsByCompanyId(companyId)).thenReturn(false);

            companyService.deleteCompany(companyId, currentUser);

            verify(companyRepository).delete(sampleCompany);
        }

        @Test
        @DisplayName("Should throw CompanyHasLinkedApplicationsException when linked applications exist")
        void deleteCompany_LinkedApplications_ThrowsException() {
            when(companyRepository.findByIdAndUserId(companyId, currentUserId)).thenReturn(Optional.of(sampleCompany));
            when(jobApplicationRepository.existsByCompanyId(companyId)).thenReturn(true);

            assertThatThrownBy(() -> companyService.deleteCompany(companyId, currentUser))
                    .isInstanceOf(CompanyHasLinkedApplicationsException.class)
                    .hasMessageContaining("linked job applications");

            verify(companyRepository, never()).delete(any());
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting a company the user does not own")
        void deleteCompany_NotOwned_ThrowsNotFound() {
            when(companyRepository.findByIdAndUserId(companyId, currentUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.deleteCompany(companyId, currentUser))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(jobApplicationRepository, never()).existsByCompanyId(any());
            verify(companyRepository, never()).delete(any());
        }

    }

    @Nested
    @DisplayName("Resolve Company For User (Find-or-Create)")
    class ResolveCompanyForUserTests {

        @Test
        @DisplayName("Should throw ResourceNotFoundException when companyId is not owned by the user")
        void resolve_ById_NotOwned_ThrowsNotFound() {
            when(companyRepository.findByIdAndUserId(companyId, currentUserId)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> companyService.resolveCompanyForUser(companyId, null, currentUser))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(companyRepository, never()).save(any());
        }

        @Test
        @DisplayName("Should save a new company with trimmed name, null optional fields, and the current user")
        void resolve_ByName_New_CreatesCompany() {
            when(companyRepository.findByUserIdAndCompanyNameIgnoreCase(currentUserId, "Netflix"))
                    .thenReturn(Optional.empty());
            when(companyRepository.save(any(Company.class))).thenAnswer(inv -> inv.getArgument(0));

            Company resolved = companyService.resolveCompanyForUser(null, "  Netflix  ", currentUser);

            ArgumentCaptor<Company> captor = ArgumentCaptor.forClass(Company.class);
            verify(companyRepository).save(captor.capture());
            Company saved = captor.getValue();

            assertThat(saved.getCompanyName()).isEqualTo("Netflix");
            assertThat(saved.getWebsiteUrl()).isNull();
            assertThat(saved.getIndustry()).isNull();
            assertThat(saved.getLocation()).isNull();
            assertThat(saved.getUser()).isSameAs(currentUser);
            assertThat(resolved).isSameAs(saved);
        }
    }
}