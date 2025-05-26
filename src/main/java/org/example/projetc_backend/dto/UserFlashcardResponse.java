package org.example.projetc_backend.dto;

public record UserFlashcardResponse(
        Integer id,
        Integer userId,
        Integer wordId,
        Boolean isKnown
) {}