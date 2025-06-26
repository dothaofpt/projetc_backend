package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserWritingAttemptRequest;
import org.example.projetc_backend.dto.UserWritingAttemptResponse;
import org.example.projetc_backend.entity.UserWritingAttempt;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Question; // Import Question entity
import org.example.projetc_backend.repository.UserWritingAttemptRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.QuestionRepository; // Import QuestionRepository

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
@Transactional
public class UserWritingAttemptService {

    private final UserWritingAttemptRepository userWritingAttemptRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository; // Repository cho Question entity

    public UserWritingAttemptService(UserWritingAttemptRepository userWritingAttemptRepository,
                                     UserRepository userRepository,
                                     QuestionRepository questionRepository) {
        this.userWritingAttemptRepository = userWritingAttemptRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Lưu một lần thử viết của người dùng.
     * @param request Dữ liệu yêu cầu cho lần thử viết.
     * @return UserWritingAttemptResponse của lần thử viết đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public UserWritingAttemptResponse saveWritingAttempt(UserWritingAttemptRequest request) {
        if (request == null || request.userId() == null || request.questionId() == null ||
                request.originalPromptText() == null || request.originalPromptText().trim().isEmpty() ||
                request.userWrittenText() == null || request.userWrittenText().trim().isEmpty()) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, questionId, originalPromptText, userWrittenText) không được để trống.");
        }
        if (request.overallScore() != null && (request.overallScore() < 0 || request.overallScore() > 100)) {
            throw new IllegalArgumentException("Điểm tổng thể phải nằm trong khoảng từ 0 đến 100.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        // questionId trong UserWritingAttempt có thể null, kiểm tra nếu nó không null thì tìm question
        Question question = null;
        if (request.questionId() != null) {
            question = questionRepository.findById(request.questionId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + request.questionId()));
        }


        UserWritingAttempt attempt = new UserWritingAttempt();
        attempt.setUser(user);
        attempt.setQuestion(question); // Có thể là null
        attempt.setOriginalPromptText(request.originalPromptText().trim());
        attempt.setUserWrittenText(request.userWrittenText().trim());
        attempt.setGrammarFeedback(request.grammarFeedback() != null ? request.grammarFeedback().trim() : null);
        attempt.setSpellingFeedback(request.spellingFeedback() != null ? request.spellingFeedback().trim() : null);
        attempt.setCohesionFeedback(request.cohesionFeedback() != null ? request.cohesionFeedback().trim() : null);
        attempt.setOverallScore(request.overallScore());
        attempt.setAttemptDate(LocalDateTime.now()); // Đặt thời gian tạo

        attempt = userWritingAttemptRepository.save(attempt);
        return mapToUserWritingAttemptResponse(attempt);
    }

    /**
     * Lấy lần thử viết của người dùng theo ID.
     * @param attemptId ID của lần thử viết.
     * @return UserWritingAttemptResponse của lần thử viết.
     * @throws IllegalArgumentException nếu attemptId trống hoặc không tìm thấy.
     */
    @Transactional(readOnly = true)
    public UserWritingAttemptResponse getWritingAttemptById(Integer attemptId) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        UserWritingAttempt attempt = userWritingAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử viết với ID: " + attemptId));
        return mapToUserWritingAttemptResponse(attempt);
    }

    /**
     * Lấy tất cả các lần thử viết của một người dùng.
     * @param userId ID của người dùng.
     * @return Danh sách UserWritingAttemptResponse.
     * @throws IllegalArgumentException nếu userId không hợp lệ.
     */
    @Transactional(readOnly = true)
    public List<UserWritingAttemptResponse> getWritingAttemptsByUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
        }

        return userWritingAttemptRepository.findByUserUserId(userId).stream()
                .map(this::mapToUserWritingAttemptResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả các lần thử viết cho một câu hỏi cụ thể.
     * @param questionId ID của câu hỏi.
     * @return Danh sách UserWritingAttemptResponse.
     * @throws IllegalArgumentException nếu questionId không hợp lệ.
     */
    @Transactional(readOnly = true)
    public List<UserWritingAttemptResponse> getWritingAttemptsByQuestion(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống.");
        }
        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId);
        }

        return userWritingAttemptRepository.findByQuestionQuestionId(questionId).stream()
                .map(this::mapToUserWritingAttemptResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xóa một lần thử viết của người dùng.
     * @param attemptId ID của lần thử viết cần xóa.
     * @throws IllegalArgumentException nếu attemptId trống hoặc không tìm thấy.
     */
    public void deleteWritingAttempt(Integer attemptId) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        if (!userWritingAttemptRepository.existsById(attemptId)) {
            throw new IllegalArgumentException("Không tìm thấy lần thử viết với ID: " + attemptId);
        }
        userWritingAttemptRepository.deleteById(attemptId);
    }

    /**
     * Phương thức trợ giúp để ánh xạ UserWritingAttempt entity sang UserWritingAttemptResponse DTO.
     * @param attempt Entity UserWritingAttempt.
     * @return UserWritingAttemptResponse DTO.
     */
    private UserWritingAttemptResponse mapToUserWritingAttemptResponse(UserWritingAttempt attempt) {
        return new UserWritingAttemptResponse(
                attempt.getAttemptId(),
                attempt.getUser().getUserId(),
                (attempt.getQuestion() != null) ? attempt.getQuestion().getQuestionId() : null, // Question có thể null
                attempt.getOriginalPromptText(),
                attempt.getUserWrittenText(),
                attempt.getGrammarFeedback(),
                attempt.getSpellingFeedback(),
                attempt.getCohesionFeedback(),
                attempt.getOverallScore(),
                attempt.getAttemptDate()
        );
    }
}