package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.projetc_backend.entity.PracticeActivity; // Import the new entity

public record PracticeActivityRequest(
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotBlank(message = "Title is required")
        String title,
        @NotBlank(message = "Description is required")
        String description,
        @NotNull(message = "Activity skill is required")
        PracticeActivity.ActivitySkill skill, // Use the new ActivitySkill enum
        @NotNull(message = "Activity type is required")
        PracticeActivity.ActivityType activityType, // Use the new ActivityType enum
        String materialUrl,
        String transcriptText,
        String promptText,
        String expectedOutputText
) {}