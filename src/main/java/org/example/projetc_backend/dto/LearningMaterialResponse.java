package org.example.projetc_backend.dto;

public record LearningMaterialResponse(
        Integer materialId,
        Integer lessonId,
        String materialType,
        String materialUrl,
        String description
) {}