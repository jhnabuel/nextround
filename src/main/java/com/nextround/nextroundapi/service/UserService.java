package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.UserRequest;
import com.nextround.nextroundapi.dtos.UserResponse;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class UserService {
    private final UserRepository userRepository;

    UserService(UserRepository userRepository){
        this.userRepository = userRepository;
    }

    private User getUserByIdInternal(UUID id){
        return userRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Account not found with id: " + id));
    }

    private UserResponse mapToDto(User user){
        return new UserResponse(user.getId(), user.getEmail(), user.getFirstName(), user.getLastName(), user.getCreatedAt(), user.getUpdatedAt());
    }

    public UserResponse getUserById(UUID id){
        return mapToDto(getUserByIdInternal(id));
    }

    public List<UserResponse> getAllUsers(){
        return userRepository.findAll().stream().map(this::mapToDto).collect(Collectors.toList());
    }

    public UserResponse createUser(UserRequest userRequest){
        User newUser = new User(userRequest.email(), userRequest.passwordHash(), userRequest.firstName(), userRequest.lastName());
        User savedUser = userRepository.save(newUser);
        return mapToDto(savedUser);
    }

    public UserResponse editUser(UUID id, UserRequest userRequest){
        User userToBeEdited = getUserByIdInternal(id);
        userToBeEdited.setEmail(userRequest.email());
        userToBeEdited.setPasswordHash(userRequest.passwordHash());
        userToBeEdited.setFirstName(userRequest.firstName());
        userToBeEdited.setLastName(userRequest.lastName());

        User updatedUser = userRepository.save(userToBeEdited);
        return mapToDto(updatedUser);
    }

    public void deleteUser(UUID id){
        if(!userRepository.existsById(id)){
            throw new IllegalArgumentException("User with id: " + id + " does not exist.");
        }
        userRepository.deleteById(id);
    }
}
