package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;
import lombok.Builder;

@Builder
public record UserWritingAttemptSearchRequest(
        Integer userId,
        Integer practiceActivityId,
        @Min(value = 0, message = "Minimum overall score must be non-negative")
        Integer minOverallScore,
        @Min(value = 0, message = "Maximum overall score must be non-negative")
        Integer maxOverallScore,
        @Min(value = 0, message = "Page number must be non-negative")
        int page,
        @Min(value = 1, message = "Page size must be at least 1")
        int size
) {}