package org.example.projetc_backend.dto;

import java.util.List;

// Sử dụng record cho gọn gàng, hoặc class thông thường với constructor và getters nếu cần
public record QuestionPageResponse(
        List<QuestionResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {}