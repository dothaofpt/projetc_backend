package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.List; // Để thêm danh sách các từ vựng vào bộ

public record FlashcardSetRequest(
        @NotBlank(message = "Title is required")
        String title,
        String description,
        Integer creatorUserId, // Có thể null nếu là bộ hệ thống tạo
        @NotNull(message = "isSystemCreated is required")
        Boolean isSystemCreated,
        List<Integer> wordIds // Danh sách các wordId để thêm vào bộ
) {}