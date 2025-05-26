package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import jakarta.validation.constraints.Pattern;

public record QuestionRequest(
        @NotNull(message = "Quiz ID is required")
        Integer quizId,
        @NotBlank(message = "Question text is required")
        String questionText,
        @NotBlank(message = "Question type is required")
        @Pattern(regexp = "MULTIPLE_CHOICE|FILL_IN_THE_BLANK|LISTENING_COMPREHENSION|SPEAKING_PRONUNCIATION|READING_COMPREHENSION|WRITING_ESSAY", message = "Question type must be one of: MULTIPLE_CHOICE, FILL_IN_THE_BLANK, LISTENING_COMPREHENSION, SPEAKING_PRONUNCIATION, READING_COMPREHENSION, WRITING_ESSAY")
        String type
) {}