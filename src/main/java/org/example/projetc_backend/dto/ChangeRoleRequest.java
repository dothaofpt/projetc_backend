package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;

public record ChangeRoleRequest(
        @NotBlank(message = "Role is required")
        @Pattern(regexp = "ROLE_ADMIN|ROLE_USER", message = "Role must be either ROLE_ADMIN or ROLE_USER")
        String role
) {}