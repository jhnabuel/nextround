package com.nextround.nextroundapi.dtos;

public record AuthResponse(
        String token,
        String tokenType,
        long expiresIn,
        UserResponse user
) {
    // Convenient secondary constructor with standard defaults
    public AuthResponse(String token, long expiresIn, UserResponse user) {
        this(token, "Bearer", expiresIn, user);
    }

    public AuthResponse(String token, UserResponse user) {
        this(token, "Bearer", 86400000L, user);
    }
}