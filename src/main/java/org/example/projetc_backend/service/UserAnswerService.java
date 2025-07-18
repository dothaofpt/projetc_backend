package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserAnswerRequest;
import org.example.projetc_backend.dto.UserAnswerResponse;
import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.entity.QuizResult;
import org.example.projetc_backend.entity.UserAnswer;
import org.example.projetc_backend.repository.QuestionRepository;
import org.example.projetc_backend.repository.QuizResultRepository;
import org.example.projetc_backend.repository.UserAnswerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class UserAnswerService {

    private final UserAnswerRepository userAnswerRepository;
    private final QuizResultRepository quizResultRepository; // Cần để liên kết với QuizResult
    private final QuestionRepository questionRepository;     // Cần để liên kết với Question

    public UserAnswerService(UserAnswerRepository userAnswerRepository, QuizResultRepository quizResultRepository, QuestionRepository questionRepository) {
        this.userAnswerRepository = userAnswerRepository;
        this.quizResultRepository = quizResultRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Saves a single user's answer for a specific question within a quiz result.
     * @param request The UserAnswerRequest containing the answer details.
     * @return UserAnswerResponse of the saved answer.
     * @throws IllegalArgumentException if quizResult or question not found, or request is invalid.
     */
    @Transactional
    public UserAnswerResponse submitUserAnswer(UserAnswerRequest request) {
        if (request == null || request.quizResultId() == null || request.questionId() == null || request.userAnswerText() == null) {
            throw new IllegalArgumentException("UserAnswerRequest, quizResultId, questionId, and userAnswerText cannot be null.");
        }

        // Kiểm tra QuizResult và Question có tồn tại không
        QuizResult quizResult = quizResultRepository.findById(request.quizResultId())
                .orElseThrow(() -> new IllegalArgumentException("QuizResult not found with ID: " + request.quizResultId()));
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + request.questionId()));

        // Tùy chọn: Ngăn người dùng gửi nhiều câu trả lời cho cùng một câu hỏi trong một lần làm bài
        // UserAnswer existingAnswer = userAnswerRepository.findByQuizResultResultIdAndQuestionQuestionId(
        //     request.quizResultId(), request.questionId());
        // if (existingAnswer != null) {
        //     throw new IllegalArgumentException("User has already submitted an answer for this question in this quiz session.");
        // }

        UserAnswer userAnswer = new UserAnswer();
        userAnswer.setQuizResult(quizResult);
        userAnswer.setQuestion(question);
        userAnswer.setUserAnswerText(request.userAnswerText());
        userAnswer.setCorrect(Boolean.TRUE.equals(request.isCorrect())); // Giá trị từ client, backend có thể tự chấm lại
        userAnswer.setSubmittedAt(LocalDateTime.now());

        userAnswer = userAnswerRepository.save(userAnswer);
        return mapToUserAnswerResponse(userAnswer);
    }

    /**
     * Retrieves all user answers for a specific quiz result.
     * @param quizResultId The ID of the quiz result.
     * @return A list of UserAnswerResponse.
     * @throws IllegalArgumentException if quizResultId is null or quizResult not found.
     */
    public List<UserAnswerResponse> getUserAnswersByQuizResultId(Integer quizResultId) {
        if (quizResultId == null) {
            throw new IllegalArgumentException("QuizResult ID cannot be null.");
        }
        // Kiểm tra QuizResult có tồn tại không
        quizResultRepository.findById(quizResultId)
                .orElseThrow(() -> new IllegalArgumentException("QuizResult not found with ID: " + quizResultId));

        return userAnswerRepository.findByQuizResultResultId(quizResultId).stream()
                .map(this::mapToUserAnswerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Helper method to map UserAnswer entity to UserAnswerResponse DTO.
     */
    private UserAnswerResponse mapToUserAnswerResponse(UserAnswer userAnswer) {
        if (userAnswer == null) {
            return null;
        }
        return new UserAnswerResponse(
                userAnswer.getUserAnswerId(),
                userAnswer.getQuizResult().getResultId(),
                userAnswer.getQuestion().getQuestionId(),
                userAnswer.getUserAnswerText(),
                userAnswer.isCorrect(),
                userAnswer.getSubmittedAt()
        );
    }
}