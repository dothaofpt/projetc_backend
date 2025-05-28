package org.example.projetc_backend.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;

public record QuestionRequest(
        @NotNull(message = "Quiz ID is required")
        Integer quizId,
        @NotBlank(message = "Question text is required")
        String questionText,
        @NotNull(message = "Skill is required")
        @Pattern(regexp = "LISTENING|SPEAKING|READING|WRITING|VOCABULARY|GRAMMAR", message = "Skill must be one of: LISTENING, SPEAKING, READING, WRITING, VOCABULARY, GRAMMAR")
        String skill
) {}