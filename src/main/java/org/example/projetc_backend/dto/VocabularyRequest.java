package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.example.projetc_backend.entity.Vocabulary; // Import enum từ entity

public record VocabularyRequest(
        @NotBlank(message = "Word is required")
        String word,
        @NotBlank(message = "Meaning is required")
        String meaning,
        String exampleSentence,
        String pronunciation,
        String audioUrl,
        String imageUrl, // Bổ sung
        String writingPrompt,
        @NotNull(message = "Difficulty level is required")
        Vocabulary.DifficultyLevel difficultyLevel // Sử dụng enum trực tiếp
) {}