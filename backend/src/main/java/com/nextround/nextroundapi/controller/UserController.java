package com.nextround.nextroundapi.controller;

import com.nextround.nextroundapi.dtos.*;
import com.nextround.nextroundapi.service.JobApplicationService;
import com.nextround.nextroundapi.service.UserService;
import jakarta.validation.Valid;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

    @RestController
    @RequestMapping("/api/v1/users")
    public class UserController {
        private final UserService userService;

        UserController(UserService userService){
            this.userService = userService;
        }

        @PostMapping
        public ResponseEntity<UserResponse> createUser(@Valid @RequestBody UserRequest request){
            UserResponse createdUser = userService.createUser(request);
            return new ResponseEntity<>(createdUser, HttpStatus.CREATED);
        }

        @GetMapping("/{id}")
        public ResponseEntity<UserResponse> getUserById(@PathVariable UUID id){
            return ResponseEntity.ok(userService.getUserById(id));
        }

        @GetMapping
        public ResponseEntity<List<UserResponse>> getAllUsers(){
            return ResponseEntity.ok(userService.getAllUsers());
        }

        @PatchMapping("/{id}")
        public ResponseEntity<UserResponse> updateUser(@PathVariable UUID id,
                                                       @Valid @RequestBody UserRequest userRequest){
            UserResponse updatedUser = userService.editUser(id, userRequest);
            return ResponseEntity.ok(updatedUser);
        }

        @DeleteMapping("/{id}")
        public ResponseEntity<Void> deleteUser(@PathVariable UUID id){
            userService.deleteUser(id);
            return ResponseEntity.noContent().build();
        }

        @GetMapping("/me")
        public ResponseEntity<UserResponse> getCurrentUserProfile(
                @AuthenticationPrincipal UserDetails principal) {
            return ResponseEntity.ok(userService.getCurrentUserProfile(principal.getUsername()));
        }

        @PatchMapping("/me")
        public ResponseEntity<UserResponse> updateCurrentUserProfile(
                @AuthenticationPrincipal UserDetails principal,
                @Valid @RequestBody UpdateProfileRequest request) {
            return ResponseEntity.ok(
                    userService.updateCurrentUserProfile(principal.getUsername(), request)
            );
        }

        @DeleteMapping("/me")
        public ResponseEntity<Void> deleteCurrentUserAccount(
                @AuthenticationPrincipal UserDetails principal,
                @Valid @RequestBody DeleteAccountRequest request) {
            userService.deleteCurrentUserAccount(principal.getUsername(), request);
            return ResponseEntity.noContent().build();
        }

        @PatchMapping("/me/password")
        public ResponseEntity<Void> changePassword(
                @AuthenticationPrincipal UserDetails principal,
                @Valid @RequestBody ChangePasswordRequest request) {
            userService.changePassword(principal.getUsername(), request);
            return ResponseEntity.noContent().build();
        }
    }
