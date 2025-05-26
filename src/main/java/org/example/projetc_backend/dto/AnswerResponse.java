package org.example.projetc_backend.dto;

import com.fasterxml.jackson.annotation.JsonProperty;

public record AnswerResponse(
        Integer answerId,
        Integer questionId,
        @JsonProperty("content")
        String answerText,
        Boolean isCorrect
) {}
