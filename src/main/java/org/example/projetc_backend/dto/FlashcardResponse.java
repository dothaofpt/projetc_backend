package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.Vocabulary;

public record FlashcardResponse(
        Integer wordId,
        String word,
        String meaning,
        String exampleSentence,
        String pronunciation,
        String audioUrl,
        String writingPrompt,
        String difficultyLevel,
        boolean isKnown
) {
    public static FlashcardResponse fromVocabulary(Vocabulary vocabulary, boolean isKnown) {
        return new FlashcardResponse(
                vocabulary.getWordId(),
                vocabulary.getWord() != null ? vocabulary.getWord() : "",
                vocabulary.getMeaning() != null ? vocabulary.getMeaning() : "",
                vocabulary.getExampleSentence() != null ? vocabulary.getExampleSentence() : "",
                vocabulary.getPronunciation() != null ? vocabulary.getPronunciation() : "",
                vocabulary.getAudioUrl() != null ? vocabulary.getAudioUrl() : "",
                vocabulary.getWritingPrompt() != null ? vocabulary.getWritingPrompt() : "",
                vocabulary.getDifficultyLevel() != null ? vocabulary.getDifficultyLevel().toString() : "EASY",
                isKnown
        );
    }
}