package org.example.projetc_backend.dto;

import java.util.List;

public record FlashcardPageResponse(
        List<FlashcardResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}