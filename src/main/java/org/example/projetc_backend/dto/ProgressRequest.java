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
        // THAY ĐỔI: Thay thế "Skill" bằng "ActivityType"
        @NotNull(message = "Activity type is required")
        @Pattern(regexp = "READING_MATERIAL|FLASHCARDS|QUIZ|LISTENING_PRACTICE|SPEAKING_EXERCISE|WRITING_TASK|GRAMMAR_EXERCISE|VOCABULARY_BUILDER",
                message = "Activity type must be one of: READING_MATERIAL, FLASHCARDS, QUIZ, LISTENING_PRACTICE, SPEAKING_EXERCISE, WRITING_TASK, GRAMMAR_EXERCISE, VOCABULARY_BUILDER")
        String activityType, // Cập nhật tên trường và các giá trị regex
        @NotBlank(message = "Status is required")
        @Pattern(regexp = "NOT_STARTED|IN_PROGRESS|COMPLETED", message = "Status must be one of: NOT_STARTED, IN_PROGRESS, COMPLETED")
        String status,
        @NotNull(message = "Completion percentage is required")
        Integer completionPercentage
) {}