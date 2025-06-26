package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.LearningMaterial; // Import enum từ entity

public record LearningMaterialResponse(
        Integer materialId,
        Integer lessonId,
        LearningMaterial.MaterialType materialType, // Sử dụng enum trực tiếp
        String materialUrl,
        String description,
        String transcriptText // Bổ sung
) {}