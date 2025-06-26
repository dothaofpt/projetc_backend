package org.example.projetc_backend.dto;// org.example.projetc_backend.dto.AnswerRequest
import com.fasterxml.jackson.annotation.JsonProperty;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record AnswerRequest(
        @NotNull(message = "Question ID is required")
        Integer questionId,

        @NotBlank(message = "Nội dung câu trả lời không được để trống hoặc chỉ chứa khoảng trắng")
        @JsonProperty("content")
        String answerText,

        @NotNull(message = "Correctness is required")
        Boolean isCorrect,

        // THÊM DÒNG NÀY: Trường isActive là bắt buộc khi tạo/cập nhật câu trả lời
        @NotNull(message = "Active status is required")
        Boolean isActive
) {}