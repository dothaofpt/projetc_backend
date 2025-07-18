package org.example.projetc_backend.dto;

import java.util.List;

public record VocabularyPageResponse(
        List<VocabularyResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}