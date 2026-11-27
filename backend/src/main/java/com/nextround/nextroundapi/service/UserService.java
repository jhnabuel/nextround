package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.*;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.enums.Role;
import com.nextround.nextroundapi.exception.*;
import com.nextround.nextroundapi.repository.CompanyRepository;
import com.nextround.nextroundapi.repository.JobApplicationRepository;
import com.nextround.nextroundapi.repository.UserRepository;
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

    private User getUserByEmailInternal(String email){
        return userRepository.findByEmail(email).orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));
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
        return UserMapper.toDto(getUserByEmailInternal(email));
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

    public UserResponse editUser(UUID id, UserRequest userRequest) {
        User userToBeEdited = getUserByIdInternal(id);
        userToBeEdited.setEmail(userRequest.email().trim());
        userToBeEdited.setPasswordHash(passwordEncoder.encode(userRequest.password()));
        userToBeEdited.setFirstName(userRequest.firstName().trim());
        userToBeEdited.setLastName(userRequest.lastName().trim());

        User updatedUser = userRepository.save(userToBeEdited);
        return UserMapper.toDto(updatedUser);
    }

    public void deleteUser(UUID id) {
        if (!userRepository.existsById(id)) {
            throw new ResourceNotFoundException("User not found.");
        }
        jobApplicationRepository.deleteAllByUserId(id);
        companyRepository.deleteAllByUserId(id);
        userRepository.deleteById(id);
    }

    public void deleteCurrentUserAccount(String email, DeleteAccountRequest deleteAccountRequest){
        User currentUser = getUserByEmailInternal(email);

        if(!passwordEncoder.matches(deleteAccountRequest.password(), currentUser.getPassword())){
            throw new InvalidCurrentPasswordException("Current password does not match.");
        }

        UUID userId = currentUser.getId();
        jobApplicationRepository.deleteAllByUserId(userId);
        companyRepository.deleteAllByUserId(userId);
        userRepository.delete(currentUser);
    }


    @Transactional
    public void changePassword(String email, ChangePasswordRequest request){
        User currentUser = getUserByEmailInternal(email);

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
    public UserResponse getCurrentUserProfile(String email) {
        return UserMapper.toDto(getUserByEmailInternal(email));
    }

    public UserResponse updateCurrentUserProfile(String email, UpdateProfileRequest request){
        User currentUser = getUserByEmailInternal(email);
        currentUser.setFirstName(request.firstName().trim());
        currentUser.setLastName(request.lastName().trim());

        User updatedUser = userRepository.save(currentUser);

        return UserMapper.toDto(updatedUser);
    }
}
