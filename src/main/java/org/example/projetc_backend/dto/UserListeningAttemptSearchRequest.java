package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record UserListeningAttemptSearchRequest(
        Integer userId,
        Integer practiceActivityId,
        @Min(value = 0, message = "Minimum accuracy score must be non-negative")
        Integer minAccuracyScore,
        @Min(value = 0, message = "Maximum accuracy score must be non-negative")
        Integer maxAccuracyScore,
        @Min(value = 0, message = "Page number must be non-negative")
        int page,
        @Min(value = 1, message = "Page size must be at least 1")
        int size
) {}