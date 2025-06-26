// src/main/java/org/example/projetc_backend/dto/QuizResultSearchRequest.java
package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;

public record QuizResultSearchRequest(
        Integer userId,
        Integer quizId,
        Double minScore, // Có thể là Double để chứa điểm số thực
        Double maxScore, // Có thể là Double để chứa điểm số thực
        @Min(0) Integer page,
        @Min(1) Integer size,
        String sortBy, // Tên trường để sắp xếp (ví dụ: "completedAt", "score")
        String sortDir // Hướng sắp xếp ("ASC" hoặc "DESC")
) {
    // Constructor Canonical để thiết lập giá trị mặc định nếu null/invalid
    public QuizResultSearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "resultId"; // Giả sử id của QuizResult là resultId
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
        // minScore/maxScore không cần mặc định null, để cho tìm kiếm linh hoạt
    }
}