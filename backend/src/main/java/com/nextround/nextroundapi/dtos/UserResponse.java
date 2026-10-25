package com.nextround.nextroundapi.dtos;

import com.nextround.nextroundapi.enums.Role;

import java.time.LocalDateTime;
import java.util.UUID;

public record UserResponse(UUID id,
                           String email,
                           String firstName,
                           String lastName,
                           Role role,
                           LocalDateTime createdAt,
                           LocalDateTime updatedAt) {
}
