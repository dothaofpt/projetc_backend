package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Pattern;
import org.example.projetc_backend.entity.LearningMaterial; // Import enum từ entity

public record LearningMaterialSearchRequest(
        Integer lessonId,
        LearningMaterial.MaterialType materialType, // Sử dụng enum trực tiếp
        String description,
        Integer page,
        Integer size,
        String sortBy,
        String sortDir
) {
    public LearningMaterialSearchRequest {
        // Default values for pagination and sorting if not provided
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "materialId";
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}