package org.example.projetc_backend.dto;

public record UserUpdateRequest(
        String fullName,
        String avatarUrl
) {}
