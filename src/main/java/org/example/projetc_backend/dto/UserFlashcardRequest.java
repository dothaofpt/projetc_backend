package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotNull;

public record UserFlashcardRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Word ID is required")
        Integer wordId,
        @NotNull(message = "Known status is required")
        Boolean isKnown
) {}