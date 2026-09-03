package com.nextround.nextroundapi.service;

import com.nextround.nextroundapi.dtos.UserRequest;
import com.nextround.nextroundapi.dtos.UserResponse;
import com.nextround.nextroundapi.entity.User;
import com.nextround.nextroundapi.exception.ResourceNotFoundException;
import com.nextround.nextroundapi.repository.UserRepository;
import org.springframework.stereotype.Service;
import com.nextround.nextroundapi.mapper.UserMapper;


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
        return userRepository.findById(id).orElseThrow(() -> new ResourceNotFoundException("Account not found with id: " + id));
    }

    public UserResponse getUserById(UUID id){
        return UserMapper.toDto(getUserByIdInternal(id));
    }

    public List<UserResponse> getAllUsers(){
        return userRepository.findAll().stream().map(UserMapper::toDto).collect(Collectors.toList());
    }

    public UserResponse createUser(UserRequest userRequest){
        User newUser = new User(userRequest.email(), userRequest.passwordHash(), userRequest.firstName(), userRequest.lastName());
        User savedUser = userRepository.save(newUser);
        return UserMapper.toDto(savedUser);
    }

    public UserResponse editUser(UUID id, UserRequest userRequest){
        User userToBeEdited = getUserByIdInternal(id);
        userToBeEdited.setEmail(userRequest.email());
        userToBeEdited.setPasswordHash(userRequest.passwordHash());
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
}
