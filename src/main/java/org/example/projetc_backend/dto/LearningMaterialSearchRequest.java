package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Pattern;

public record LearningMaterialSearchRequest(
        Integer lessonId,
        @Pattern(regexp = "AUDIO|VIDEO|TEXT|IMAGE|PDF|^$", message = "Material type must be one of: AUDIO, VIDEO, TEXT, IMAGE, PDF or empty")
        String materialType,
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