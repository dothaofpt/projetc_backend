package org.example.projetc_backend.dto;

import java.util.List;

public record QuizPageResponse(
        List<QuizResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {}