package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.Vocabulary; // Import enum từ entity

public record VocabularyResponse(
        Integer wordId,
        String word,
        String meaning,
        String exampleSentence,
        String pronunciation,
        String audioUrl,
        String imageUrl, // Bổ sung
        String writingPrompt,
        Vocabulary.DifficultyLevel difficultyLevel // Sử dụng enum trực tiếp
) {}