package org.example.projetc_backend.dto;

public record UserUpdateRequest(
        String username,
        String email,
        String password,
        String fullName,
        String avatarUrl,
        String role
) {}