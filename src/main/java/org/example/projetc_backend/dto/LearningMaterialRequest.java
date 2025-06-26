package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import org.example.projetc_backend.entity.LearningMaterial; // Import enum từ entity

public record LearningMaterialRequest(
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotNull(message = "Material type is required")
        LearningMaterial.MaterialType materialType, // Sử dụng enum trực tiếp
        @NotBlank(message = "Material URL is required")
        String materialUrl,
        String description,
        String transcriptText // Bổ sung
) {}