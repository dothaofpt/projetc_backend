package org.example.projetc_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnswerResponse(
        Integer answerId,
        Integer questionId,
        @JsonProperty("content")
        String answerText,
        Boolean isCorrect,
        Boolean isActive, // Trường này là quan trọng để frontend biết trạng thái của câu trả lời
        Boolean isDeleted // THAY ĐỔI MỚI: Thêm trường này
) {}