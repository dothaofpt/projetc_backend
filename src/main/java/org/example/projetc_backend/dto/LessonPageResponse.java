package org.example.projetc_backend.dto;

import java.util.List;

public record LessonPageResponse(
        List<LessonResponse> content,
        long totalElements,
        int totalPages,
        int page,
        int size
) {}