package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import jakarta.validation.constraints.Pattern;

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,
        String fullName,
        @NotBlank(message = "Role is required")
        @Pattern(regexp = "ROLE_ADMIN|ROLE_USER", message = "Role must be either ROLE_ADMIN or ROLE_USER")
        String role
) {}