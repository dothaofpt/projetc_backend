package org.example.projetc_backend.dto;

import java.util.List;

public record QuizResultPageResponse(
        List<QuizResultResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {}