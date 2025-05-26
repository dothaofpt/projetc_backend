package org.example.projetc_backend.dto;

public record VocabularyResponse(
        Integer wordId,
        String word,
        String meaning,
        String exampleSentence,
        String pronunciation,
        String audioUrl,
        String writingPrompt,
        String difficultyLevel
) {}