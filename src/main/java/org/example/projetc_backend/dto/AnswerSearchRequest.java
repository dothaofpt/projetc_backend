package org.example.projetc_backend.dto;// org.example.projetc_backend.dto.AnswerSearchRequest
import jakarta.validation.constraints.Pattern;

public record AnswerSearchRequest(
        Integer questionId,
        Boolean isCorrect,
        Boolean isActive,
        Boolean isDeleted, // THÊM DÒNG NÀY để có thể tìm kiếm theo trạng thái xóa mềm
        String answerText,
        Integer page,
        Integer size,
        String sortBy,
        String sortDir
) {
    public AnswerSearchRequest {
        // Default values for pagination and sorting if not provided
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        // Cập nhật sortBy để bao gồm isDeleted nếu cần sort theo nó
        if (sortBy == null || sortBy.isBlank() ||
                !(sortBy.equals("answerId") || sortBy.equals("answerText") ||
                        sortBy.equals("isCorrect") || sortBy.equals("isActive") ||
                        sortBy.equals("isDeleted"))) { // Cập nhật các trường có thể sort
            sortBy = "answerId";
        }
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}