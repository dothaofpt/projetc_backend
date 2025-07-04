package org.example.projetc_backend.dto;

import java.util.List;

// This DTO does not need direct changes, but its 'content' type refers to QuizResponse.
// Since QuizResponse is updated, this will implicitly reflect those changes.
public record QuizPageResponse(
        List<QuizResponse> content, // Content now contains the updated QuizResponse
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {}