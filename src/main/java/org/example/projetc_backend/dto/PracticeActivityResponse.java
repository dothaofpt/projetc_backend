package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.PracticeActivity; // Import the new entity
import java.time.LocalDateTime;

public record PracticeActivityResponse(
        Integer activityId,
        Integer lessonId,
        String title,
        String description,
        PracticeActivity.ActivitySkill skill,
        PracticeActivity.ActivityType activityType,
        String materialUrl,
        String transcriptText,
        String promptText,
        String expectedOutputText,
        LocalDateTime createdAt
) {}