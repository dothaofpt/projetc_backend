package org.example.projetc_backend.dto;

import java.time.LocalDateTime;
import java.util.List;

public record FlashcardSetResponse(
        Integer setId,
        String title,
        String description,
        Integer creatorUserId,
        Boolean isSystemCreated,
        LocalDateTime createdAt,
        List<FlashcardResponse> flashcards // ĐÃ THAY ĐỔI TỪ vocabularies SANG flashcards
) {}