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
        if (request == null || request.userId() == null || request.quizId() == null) {
            throw new IllegalArgumentException("Request, userId, hoặc quizId không được để trống");
        }
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + request.quizId()));
        QuizResult quizResult = new QuizResult();
        quizResult.setUser(user);
        quizResult.setQuiz(quiz);
        quizResult.setScore(request.score() != null ? request.score() : 0);
        quizResult = quizResultRepository.save(quizResult);
        return mapToQuizResultResponse(quizResult);
    }

    public QuizResultResponse getQuizResultByUserAndQuiz(Integer userId, Integer quizId) {
        if (userId == null || quizId == null) {
            throw new IllegalArgumentException("User ID và Quiz ID không được để trống");
        }
        QuizResult quizResult = quizResultRepository.findByUserUserIdAndQuizQuizId(userId, quizId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kết quả cho user ID: " + userId + " và quiz ID: " + quizId));
        return mapToQuizResultResponse(quizResult);
    }

    public List<QuizResultResponse> getQuizResultsByUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống");
        }
        return quizResultRepository.findByUserUserId(userId).stream()
                .map(this::mapToQuizResultResponse)
                .collect(Collectors.toList());
    }

    public List<QuizResultResponse> findQuizResultsByQuiz(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống");
        }
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
                quizResult.getCompletedAt()
        );
    }
}