package com.nextround.nextroundapi.mapper;

import com.nextround.nextroundapi.dtos.UserResponse;
import com.nextround.nextroundapi.entity.User;

public class UserMapper {
    public static UserResponse toDto(User user){
        return new UserResponse(
                user.getId(),
                user.getEmail(),
                user.getFirstName(),
                user.getLastName(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }
}