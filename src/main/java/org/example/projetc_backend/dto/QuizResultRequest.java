package org.example.projetc_backend.dto;

public class QuizResultRequest {
    private Integer userId;
    private Integer quizId;
    private Integer score;

    // Constructors
    public QuizResultRequest() {}

    public QuizResultRequest(Integer userId, Integer quizId, Integer score) {
        this.userId = userId;
        this.quizId = quizId;
        this.score = score;
    }

    // Getters and Setters
    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getQuizId() {
        return quizId;
    }

    public void setQuizId(Integer quizId) {
        this.quizId = quizId;
    }

    public Integer getScore() {
        return score;
    }

    public void setScore(Integer score) {
        this.score = score;
    }
}