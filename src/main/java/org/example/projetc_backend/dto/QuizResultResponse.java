package org.example.projetc_backend.dto;

public class QuizResultResponse {
    private final Integer resultId;
    private final Integer userId;
    private final Integer quizId;
    private final Integer score;
    private final String completedAt;

    public QuizResultResponse(Integer resultId, Integer userId, Integer quizId, Integer score, String completedAt) {
        this.resultId = resultId;
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
        this.completedAt = completedAt;
    }

    // Getters
    public Integer getResultId() {
        return resultId;
    }

    public Integer getUserId() {
        return userId;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public Integer getScore() {
        return score;
    }

    public String getCompletedAt() {
        return completedAt;
    }
}