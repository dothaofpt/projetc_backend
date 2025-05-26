package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

import java.time.LocalDateTime;

public record ProgressRequest(
        @NotNull(message = "User ID is required")
        Integer userId,
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotNull(message = "Skill is required")
        @Pattern(regexp = "LISTENING|SPEAKING|READING|WRITING|VOCABULARY|GRAMMAR", message = "Skill must be one of: LISTENING, SPEAKING, READING, WRITING, VOCABULARY, GRAMMAR")
        String skill,
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "NOT_STARTED|IN_PROGRESS|COMPLETED", message = "Status must be one of: NOT_STARTED, IN_PROGRESS, COMPLETED")
        String status,
        @NotNull(message = "Completion percentage is required")
        Integer completionPercentage
) {}