package org.example.projetc_backend.dto;

import java.util.List;

public record PracticeActivityPageResponse(
        List<PracticeActivityResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {}