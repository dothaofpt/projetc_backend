package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record LearningMaterialRequest(
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotBlank(message = "Material type is required")
        @Pattern(regexp = "AUDIO|VIDEO|TEXT|IMAGE|PDF", message = "Material type must be one of: AUDIO, VIDEO, TEXT, IMAGE, PDF")
        String materialType,
        @NotBlank(message = "Material URL is required")
        String materialUrl,
        String description
) {}