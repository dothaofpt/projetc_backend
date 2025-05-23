package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;

public record MessageResponse(@NotBlank String message) {}