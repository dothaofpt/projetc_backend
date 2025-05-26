package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record VocabularyDTO(
        Integer wordId,
        @NotBlank(message = "Word is required")
        String word,
        @NotBlank(message = "Meaning is required")
        String meaning,
        String exampleSentence,
        String pronunciation,
        String audioUrl,
        String writingPrompt,
        @NotNull(message = "Difficulty level is required")
        @Pattern(regexp = "EASY|MEDIUM|HARD", message = "Difficulty level must be one of: EASY, MEDIUM, HARD")
        String difficultyLevel
) {}