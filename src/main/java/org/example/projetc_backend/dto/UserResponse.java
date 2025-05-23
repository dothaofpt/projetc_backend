package org.example.projetc_backend.dto;

public record UserResponse(
        Integer userId,
        String username,
        String email,
        String fullName,
        String avatarUrl,
        String createdAt
) {}