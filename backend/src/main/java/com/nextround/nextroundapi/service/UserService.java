package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.*;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.Role;
import com.nextround.nextroundapi.exception.*;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import com.nextround.nextroundapi.mapper.UserMapper;
import org.springframework.transaction.annotation.Transactional;


import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserService {
    private final UserRepository userRepository;
    private final CompanyRepository companyRepository;
    private final JobApplicationRepository jobApplicationRepository;
    private final PasswordEncoder passwordEncoder;
    

    UserService(UserRepository userRepository, PasswordEncoder passwordEncoder, CompanyRepository companyRepository, JobApplicationRepository jobApplicationRepository){
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.companyRepository = companyRepository;
        this.jobApplicationRepository = jobApplicationRepository;
    }

    private User getAuthenticatedUser(){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if(authentication == null || !authentication.isAuthenticated()){
            throw new UnauthorizedException("User is not authenticated.");
        }
        return userRepository.findByEmail(authentication.getName()).orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found."));

    }


    private User getUserByIdInternal(UUID id){
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    @Transactional(readOnly = true)
    public UserResponse getUserById(UUID id){
        return UserMapper.toDto(getUserByIdInternal(id));
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers(){
        return userRepository.findAll().stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponse getUserByEmail(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        return UserMapper.toDto(user);
    }

    public String registerUser(String rawPassword){
        return passwordEncoder.encode(rawPassword);
    }

    public UserResponse createUser(UserRequest userRequest){
        if(userRepository.existsByEmail(userRequest.email())){
            throw new EmailAlreadyExistsException("Email already registered");
        }

        String hashedPassword = registerUser(userRequest.password());

        User newUser = new User(userRequest.email(), hashedPassword, userRequest.firstName(), userRequest.lastName(), Role.USER);
        User savedUser = userRepository.save(newUser);
        
        return UserMapper.toDto(savedUser);
    }

    public UserResponse editUser(UUID id, UserRequest userRequest){

        User userToBeEdited = getUserByIdInternal(id);
        userToBeEdited.setEmail(userRequest.email());
        userToBeEdited.setPasswordHash(userRequest.password());
        userToBeEdited.setFirstName(userRequest.firstName());
        userToBeEdited.setLastName(userRequest.lastName());

        User updatedUser = userRepository.save(userToBeEdited);
        return UserMapper.toDto(updatedUser);
    }

    public void deleteUser(UUID id){
        if(!userRepository.existsById(id)){
            throw new ResourceNotFoundException("User not found.");
        }
        userRepository.deleteById(id);
    }

    public void deleteCurrentUserAccount(DeleteAccountRequest deleteAccountRequest){
        User currentUser = getAuthenticatedUser();

        if(!passwordEncoder.matches(deleteAccountRequest.password(), currentUser.getPassword())){
            throw new InvalidCurrentPasswordException("Current password does not match.");
        }

        UUID userId = currentUser.getId();
        jobApplicationRepository.deleteAllByUserId(userId);
        companyRepository.deleteAllByUserId(userId);
        userRepository.delete(currentUser);
    }


    @Transactional
    public void changePassword(ChangePasswordRequest request){
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated.");
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found."));

        // Verify current password; throw 400 trigger if invalid
        if (!passwordEncoder.matches(request.currentPassword(), currentUser.getPassword())) {
            throw new InvalidCurrentPasswordException("Current password does not match.");
        }

        // Throw error if old password gets reused as new password
        if (passwordEncoder.matches(request.newPassword(), currentUser.getPassword())) {
            throw new IllegalArgumentException("New password must be different from current password.");
        }

        // Hash and persist new password
        currentUser.setPasswordHash(passwordEncoder.encode(request.newPassword()));
        userRepository.save(currentUser);
    }

    @Transactional(readOnly = true)
    public UserResponse getCurrentUserProfile() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new UnauthorizedException("User is not authenticated.");
        }

        User currentUser = userRepository.findByEmail(authentication.getName())
                .orElseThrow(() -> new UsernameNotFoundException("Authenticated user not found."));

        return UserMapper.toDto(currentUser);
    }

    public UserResponse updateCurrentUserProfile(UpdateProfileRequest request){
        User currentUser = getAuthenticatedUser();
        currentUser.setFirstName(request.firstName().trim());
        currentUser.setLastName(request.lastName().trim());

        User updatedUser = userRepository.save(currentUser);

        return UserMapper.toDto(updatedUser);
    }
}
