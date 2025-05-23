package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotNull;

public record LessonVocabularyRequest(
        @NotNull(message = "Lesson ID is required")
        Integer lessonId,
        @NotNull(message = "Word ID is required")
        Integer wordId
) {}
