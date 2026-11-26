package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.UpdateProfileRequest;
import com.nextround.nextroundapi.dtos.UserRequest;
import com.nextround.nextroundapi.dtos.UserResponse;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.Role;
import com.nextround.nextroundapi.exception.EmailAlreadyExistsException;
import com.nextround.nextroundapi.exception.ResourceNotFoundException;
import com.nextround.nextroundapi.exception.UnauthorizedException;
import com.nextround.nextroundapi.exception.UsernameNotFoundException;
import com.nextround.nextroundapi.repository.UserRepository;
import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class UserServiceTest {

    @Mock
    private UserRepository userRepository;

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

    // ==========================================
    // CREATE USER TESTS
    // ==========================================
    @Nested
    @DisplayName("createUser()")
    class CreateUserTests {

        @Test
        @DisplayName("Should successfully create and persist a new user with hashed password and default ROLE_USER")
        void shouldCreateUserSuccessfully() {
            // Arrange
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

            // Act
            UserResponse response = userService.createUser(request);

            // Assert
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
        @DisplayName("Should throw EmailAlreadyExistsException and skip persistence when email is taken")
        void shouldThrowExceptionWhenEmailAlreadyExists() {
            // Arrange
            UserRequest request = new UserRequest(
                    "john@example.com",
                    "Password123!",
                    "John",
                    "Doe"
            );

            given(userRepository.existsByEmail(request.email())).willReturn(true);

            // Act & Assert
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
            // Arrange
            given(userRepository.findById(sampleUserId)).willReturn(Optional.of(sampleUser));

            // Act
            UserResponse response = userService.getUserById(sampleUserId);

            // Assert
            assertThat(response).isNotNull();
            assertThat(response.id()).isEqualTo(sampleUserId);
            assertThat(response.email()).isEqualTo(sampleUser.getEmail());
            verify(userRepository, times(1)).findById(sampleUserId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when user does not exist")
        void shouldThrowResourceNotFoundExceptionWhenUserNotFound() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            given(userRepository.findById(nonExistentId)).willReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> userService.getUserById(nonExistentId))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(nonExistentId.toString());

            verify(userRepository, times(1)).findById(nonExistentId);
        }
    }
    @Nested
    @DisplayName("getUserByEmail")
    class GetUserByEmailTests{
        @Test
        @DisplayName("Should return UserResponse when email matches")
        void shouldReturnUserResponseWhenEmailMatches(){
            String email = "john@example.com";
            given(userRepository.findByEmail(email)).willReturn(Optional.of(sampleUser));

            UserResponse response = userService.getUserByEmail(email);

            assertThat(response).isNotNull();
            assertThat(response.email()).isEqualTo(email);
            verify(userRepository, times(1)).findByEmail(email);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when email does not exist")
        void shouldThrowResourceNotFoundExceptionWhenEmailDoesNotExist(){
            String nonExistentEmail = "ghost@example.com";
            given(userRepository.findByEmail(nonExistentEmail)).willReturn(Optional.empty());

            assertThatThrownBy(() -> userService.getUserByEmail(nonExistentEmail))
                    .isInstanceOf(ResourceNotFoundException.class)
                    .hasMessageContaining(nonExistentEmail);
        }
    }

    @Nested
    @DisplayName("updateUser()")
    class UpdateCurrentUserProfileTests{
        @AfterEach
        void clearSecurityContext() {
            SecurityContextHolder.clearContext();
        }

        @Test
        @DisplayName("Should update user first name and last name")
        void shouldUpdateUserFirstNameandLastName(){
            // Arrange
            User existingUser = new User("john@example.com", "encoded-password",
                    "OldFirst",
                    "OldLast",
                    Role.USER);

            UpdateProfileRequest updateProfileRequest = new UpdateProfileRequest("John", "Doe");

            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("john@example.com");
            SecurityContextHolder.getContext().setAuthentication(auth);

            when(userRepository.findByEmail("john@example.com")).thenReturn(Optional.of(existingUser));
            when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            UserResponse response = userService.updateCurrentUserProfile(updateProfileRequest);

            // Assert
            assertEquals("John", response.firstName());
            assertEquals("Doe", response.lastName());
            assertEquals("john@example.com", response.email());
            assertEquals(Role.USER, response.role());

            verify(userRepository).findByEmail("john@example.com");
            verify(userRepository).save(existingUser);
        }


        @Test
        @DisplayName("Should throw UnauthorizedException when user is not authenticated.")
        void shouldThrowExceptionWhenUserIsNotAuthenticated(){
            //Arrange
            SecurityContextHolder.clearContext();

            UpdateProfileRequest request = new UpdateProfileRequest("John", "Abuel");

            // Act & Assert
            assertThrows(
                    UnauthorizedException.class,
                    () -> userService.updateCurrentUserProfile(request)
            );

            verifyNoInteractions(userRepository);
        }

        @Test
        @DisplayName("Should throw UsernameNotFoundException when authenticated user does not exist")
        void shouldThrowUsernameNotFoundWhenUserMissing() {
            // Arrange
            UpdateProfileRequest request = new UpdateProfileRequest("John", "Abuel");

            Authentication auth = mock(Authentication.class);
            when(auth.isAuthenticated()).thenReturn(true);
            when(auth.getName()).thenReturn("missing@example.com");
            SecurityContextHolder.getContext().setAuthentication(auth);

            when(userRepository.findByEmail("missing@example.com")).thenReturn(Optional.empty());

            // Act & Assert
            assertThrows(
                    UsernameNotFoundException.class,
                    () -> userService.updateCurrentUserProfile(request)
            );

            verify(userRepository).findByEmail("missing@example.com");
            verify(userRepository, never()).save(any(User.class));
        }
    }

    @Nested
    @DisplayName("deleteUser()")
    class DeleteUserTests {

        @Test
        @DisplayName("Should delete user when entity exists")
        void shouldDeleteUserWhenExists() {
            // Arrange
            when(userRepository.existsById(sampleUserId)).thenReturn(true);

            // Act
            userService.deleteUser(sampleUserId);

            // Assert
            verify(userRepository).deleteById(sampleUserId);
        }

        @Test
        @DisplayName("Should throw ResourceNotFoundException when deleting non-existent user")
        void shouldThrowExceptionWhenDeletingNonExistentUser() {
            // Arrange
            UUID nonExistentId = UUID.randomUUID();
            when(userRepository.existsById(nonExistentId)).thenReturn(false);


            // Act & Assert
            assertThatThrownBy(() -> userService.deleteUser(nonExistentId)).isInstanceOf(ResourceNotFoundException.class)
                            .hasMessageContaining("User not found.");


            verify(userRepository, never()).deleteById(any());
        }
    }
}
