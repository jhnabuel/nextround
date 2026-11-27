package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.*;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.Role;
import com.nextround.nextroundapi.exception.*;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private JobApplicationRepository jobApplicationRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @InjectMocks
    private UserService userService;

    @Captor
    private ArgumentCaptor<User> userArgumentCaptor;

    private User sampleUser;
    private UUID sampleUserId;

    @BeforeEach
    void setUp() {
        sampleUserId = UUID.randomUUID();
        sampleUser = new User(
                "john@example.com",
                "$2a$10$encodedHashString",
                "John",
                "Doe",
                Role.USER
        );
        sampleUser.setId(sampleUserId);
    }

    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("Should create and persist a new user with hashed password and default ROLE_USER")
        void shouldCreateUserSuccessfully() {
            UserRequest request = new UserRequest(
                    "newuser@example.com",
                    "PlainPassword123!",
                    "Jane",
                    "Smith"
            );

            given(userRepository.existsByEmail(request.email())).willReturn(false);
            given(passwordEncoder.encode(request.password())).willReturn("$2a$10$hashedPassword");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> {
                User userToSave = invocation.getArgument(0);
                userToSave.setId(UUID.randomUUID());
                return userToSave;
            });

            UserResponse response = userService.createUser(request);

            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo("newuser@example.com");
            assertThat(response.firstName()).isEqualTo("Jane");
            assertThat(response.lastName()).isEqualTo("Smith");
            assertThat(response.role()).isEqualTo(Role.USER);

            verify(userRepository).save(userArgumentCaptor.capture());
            User capturedUser = userArgumentCaptor.getValue();
            assertThat(capturedUser.getPasswordHash()).isEqualTo("$2a$10$hashedPassword");
            assertThat(capturedUser.getRole()).isEqualTo(Role.USER);
        }

        @Test
        @DisplayName("Should throw EmailAlreadyExistsException when email is taken")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            UserRequest request = new UserRequest("john@example.com", "Password123!", "John", "Doe");
            given(userRepository.existsByEmail(request.email())).willReturn(true);

            assertThatThrownBy(() -> userService.createUser(request))
                    .isInstanceOf(EmailAlreadyExistsException.class)
                    .hasMessageContaining("Email already registered");

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("getUserById()")
    class GetUserByIdTests {

        @Test
        @DisplayName("Should return UserResponse when user exists")
        void shouldReturnUserResponseWhenUserExists() {
            given(userRepository.findById(sampleUserId)).willReturn(Optional.of(sampleUser));

            UserResponse response = userService.getUserById(sampleUserId);

            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(sampleUserId);
            assertThat(response.email()).isEqualTo(sampleUser.getEmail());
            verify(userRepository).findById(sampleUserId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
            UUID nonExistentId = UUID.randomUUID();
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(nonExistentId.toString());
        }
    }

    @Nested
    @DisplayName("getUserByEmail() & getCurrentUserProfile()")
    class GetUserByEmailTests {

        @Test
        @DisplayName("Should return UserResponse when email matches")
        void shouldReturnUserResponseWhenEmailMatches() {
            given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(sampleUser));

            UserResponse byEmail = userService.getUserByEmail("john@example.com");
            UserResponse profile = userService.getCurrentUserProfile("john@example.com");

            assertThat(byEmail.email()).isEqualTo("john@example.com");
            assertThat(profile.email()).isEqualTo("john@example.com");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when email does not exist")
        void shouldThrowResourceNotFoundExceptionWhenEmailDoesNotExist() {
            String nonExistentEmail = "ghost@example.com";
            given(userRepository.findByEmail(nonExistentEmail)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getCurrentUserProfile(nonExistentEmail))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(nonExistentEmail);
        }
    }

    @Nested
    @DisplayName("editUser()")
    class EditUserTests {

        @Test
        @DisplayName("Should trim fields, encode password, and update user")
        void shouldEncodePasswordAndUpdateUser() {
            UserRequest request = new UserRequest(
                    "  updated@example.com  ",
                    "NewPlainPass123!",
                    "  Johnny  ",
                    "  Updated  "
            );

            given(userRepository.findById(sampleUserId)).willReturn(Optional.of(sampleUser));
            given(passwordEncoder.encode("NewPlainPass123!")).willReturn("$2a$10$newEncodedHash");
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            UserResponse response = userService.editUser(sampleUserId, request);

            assertThat(response.email()).isEqualTo("updated@example.com");
            assertThat(response.firstName()).isEqualTo("Johnny");
            assertThat(response.lastName()).isEqualTo("Updated");

            verify(userRepository).save(userArgumentCaptor.capture());
            assertThat(userArgumentCaptor.getValue().getPasswordHash()).isEqualTo("$2a$10$newEncodedHash");
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when editing non-existent user")
        void shouldThrowWhenEditingMissingUser() {
            UUID missingId = UUID.randomUUID();
            UserRequest request = new UserRequest("a@example.com", "Pass123!", "A", "B");
            given(userRepository.findById(missingId)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.editUser(missingId, request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(passwordEncoder, never()).encode(anyString());
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("updateCurrentUserProfile()")
    class UpdateCurrentUserProfileTests {

        @Test
        @DisplayName("Should trim and update user first name and last name")
        void shouldUpdateUserFirstNameAndLastName() {
            UpdateProfileRequest request = new UpdateProfileRequest("  John  ", "  Abuel  ");

            given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(sampleUser));
            given(userRepository.save(any(User.class))).willAnswer(invocation -> invocation.getArgument(0));

            UserResponse response = userService.updateCurrentUserProfile("john@example.com", request);

            assertThat(response.firstName()).isEqualTo("John");
            assertThat(response.lastName()).isEqualTo("Abuel");
            assertThat(response.email()).isEqualTo("john@example.com");
            assertThat(response.role()).isEqualTo(Role.USER);

            verify(userRepository).findByEmail("john@example.com");
            verify(userRepository).save(sampleUser);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void shouldThrowResourceNotFoundWhenUserMissing() {
            UpdateProfileRequest request = new UpdateProfileRequest("John", "Abuel");
            given(userRepository.findByEmail("missing@example.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateCurrentUserProfile("missing@example.com", request))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("missing@example.com");

            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("changePassword()")
    class ChangePasswordTests {

        @Test
        @DisplayName("Should encode and save new password when current password matches")
        void shouldChangePasswordSuccessfully() {
            ChangePasswordRequest request = new ChangePasswordRequest("OldPass123!", "NewPass123!");

            given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(sampleUser));
            given(passwordEncoder.matches("OldPass123!", sampleUser.getPassword())).willReturn(true);
            given(passwordEncoder.matches("NewPass123!", sampleUser.getPassword())).willReturn(false);
            given(passwordEncoder.encode("NewPass123!")).willReturn("$2a$10$newEncodedPass");

            userService.changePassword("john@example.com", request);

            verify(userRepository).save(userArgumentCaptor.capture());
            assertThat(userArgumentCaptor.getValue().getPasswordHash()).isEqualTo("$2a$10$newEncodedPass");
        }

        @Test
        @DisplayName("Should throw InvalidCurrentPasswordException when current password does not match")
        void shouldThrowWhenCurrentPasswordInvalid() {
            ChangePasswordRequest request = new ChangePasswordRequest("WrongPass!", "NewPass123!");

            given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(sampleUser));
            given(passwordEncoder.matches("WrongPass!", sampleUser.getPassword())).willReturn(false);

            assertThatThrownBy(() -> userService.changePassword("john@example.com", request))
                    .isInstanceOf(InvalidCurrentPasswordException.class)
                    .hasMessageContaining("Current password does not match");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw IllegalArgumentException when new password matches current password")
        void shouldThrowWhenNewPasswordReusesCurrent() {
            ChangePasswordRequest request = new ChangePasswordRequest("SamePass123!", "SamePass123!");

            given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(sampleUser));
            given(passwordEncoder.matches("SamePass123!", sampleUser.getPassword())).willReturn(true);

            assertThatThrownBy(() -> userService.changePassword("john@example.com", request))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("different from current password");

            verify(userRepository, never()).save(any(User.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void shouldThrowWhenUserMissing() {
            ChangePasswordRequest request = new ChangePasswordRequest("OldPass123!", "NewPass123!");
            given(userRepository.findByEmail("missing@example.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.changePassword("missing@example.com", request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verify(userRepository, never()).save(any(User.class));
        }
    }


    @Nested
    @DisplayName("deleteUser()")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete applications, companies, and user when entity exists")
        void shouldDeleteUserAndChildEntities() {
            given(userRepository.existsById(sampleUserId)).willReturn(true);

            assertThatCode(() -> userService.deleteUser(sampleUserId))
                    .doesNotThrowAnyException();

            verify(jobApplicationRepository).deleteAllByUserId(sampleUserId);
            verify(companyRepository).deleteAllByUserId(sampleUserId);
            verify(userRepository).deleteById(sampleUserId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException and skip all deletes when user does not exist")
        void shouldThrowExceptionWhenDeletingNonExistentUser() {
            UUID nonExistentId = UUID.randomUUID();
            given(userRepository.existsById(nonExistentId)).willReturn(false);

            assertThatThrownBy(() -> userService.deleteUser(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining("User not found.");

            verifyNoInteractions(jobApplicationRepository, companyRepository);
            verify(userRepository, never()).deleteById(any());
        }
    }

    @Nested
    @DisplayName("deleteCurrentUserAccount()")
    class DeleteCurrentUserAccountTests {

        @Test
        @DisplayName("Should verify password and delete applications, companies, and user")
        void shouldDeleteCurrentUserAccount() {
            DeleteAccountRequest request = new DeleteAccountRequest("CurrentPass123!");

            given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(sampleUser));
            given(passwordEncoder.matches("CurrentPass123!", sampleUser.getPassword())).willReturn(true);

            assertThatCode(() -> userService.deleteCurrentUserAccount("john@example.com", request))
                    .doesNotThrowAnyException();

            verify(jobApplicationRepository).deleteAllByUserId(sampleUserId);
            verify(companyRepository).deleteAllByUserId(sampleUserId);
            verify(userRepository).delete(sampleUser);
        }

        @Test
        @DisplayName("Should throw InvalidCurrentPasswordException and skip deletes when password mismatches")
        void shouldThrowWhenPasswordDoesNotMatch() {
            DeleteAccountRequest request = new DeleteAccountRequest("WrongPass!");

            given(userRepository.findByEmail("john@example.com")).willReturn(Optional.of(sampleUser));
            given(passwordEncoder.matches("WrongPass!", sampleUser.getPassword())).willReturn(false);

            assertThatThrownBy(() -> userService.deleteCurrentUserAccount("john@example.com", request))
                    .isInstanceOf(InvalidCurrentPasswordException.class)
                    .hasMessageContaining("Current password does not match");

            verifyNoInteractions(jobApplicationRepository, companyRepository);
            verify(userRepository, never()).delete(any(User.class));
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void shouldThrowWhenUserNotFound() {
            DeleteAccountRequest request = new DeleteAccountRequest("CurrentPass123!");
            given(userRepository.findByEmail("missing@example.com")).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.deleteCurrentUserAccount("missing@example.com", request))
                    .isInstanceOf(ResourceNotFoundException.class);

            verifyNoInteractions(jobApplicationRepository, companyRepository, passwordEncoder);
            verify(userRepository, never()).delete(any(User.class));
        }
    }
}