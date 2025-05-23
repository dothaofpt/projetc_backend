package org.example.projetc_backend.service;


import org.example.projetc_backend.dto.QuizResultRequest;
import org.example.projetc_backend.dto.QuizResultResponse;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.entity.QuizResult;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.repository.QuizRepository;
import org.example.projetc_backend.repository.QuizResultRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizResultService {

    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    public QuizResultService(QuizResultRepository quizResultRepository, UserRepository userRepository, QuizRepository quizRepository) {
        this.quizResultRepository = quizResultRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
    }

    public QuizResultResponse saveQuizResult(QuizResultRequest request) {
        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Quiz quiz = quizRepository.findById(request.getQuizId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        QuizResult quizResult = new QuizResult();
        quizResult.setUser(user);
        quizResult.setQuiz(quiz);
        quizResult.setScore(request.getScore());
        quizResult = quizResultRepository.save(quizResult);
        return mapToQuizResultResponse(quizResult);
    }

    public QuizResultResponse getQuizResultByUserAndQuiz(Integer userId, Integer quizId) {
        QuizResult quizResult = quizResultRepository.findByUserUserIdAndQuizQuizId(userId, quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz result not found"));
        return mapToQuizResultResponse(quizResult);
    }

    public List<QuizResultResponse> getQuizResultsByUser(Integer userId) {
        return quizResultRepository.findByUserUserId(userId).stream()
                .map(this::mapToQuizResultResponse)
                .collect(Collectors.toList());
    }

    public List<QuizResultResponse> getQuizResultsByQuiz(Integer quizId) {
        return quizResultRepository.findByQuizQuizId(quizId).stream()
                .map(this::mapToQuizResultResponse)
                .collect(Collectors.toList());
    }

    private QuizResultResponse mapToQuizResultResponse(QuizResult quizResult) {
        return new QuizResultResponse(
                quizResult.getResultId(),
                quizResult.getUser().getUserId(),
                quizResult.getQuiz().getQuizId(),
                quizResult.getScore(),
                quizResult.getCompletedAt().toString()
        );
    }
}