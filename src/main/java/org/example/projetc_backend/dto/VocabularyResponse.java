// VocabularyResponse
package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.Vocabulary;

public record VocabularyResponse(
        Integer wordId,
        String word,
        String meaning,
        String exampleSentence,
        String pronunciation,
        String audioUrl,
        String imageUrl,
        String writingPrompt,
        Vocabulary.DifficultyLevel difficultyLevel
) {}