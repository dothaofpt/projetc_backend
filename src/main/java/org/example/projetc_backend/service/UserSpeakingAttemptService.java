package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserSpeakingAttemptRequest;
import org.example.projetc_backend.dto.UserSpeakingAttemptResponse;
import org.example.projetc_backend.entity.UserSpeakingAttempt;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Question; // Import Question entity
import org.example.projetc_backend.repository.UserSpeakingAttemptRepository;
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
public class UserSpeakingAttemptService {

    private final UserSpeakingAttemptRepository userSpeakingAttemptRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository; // Repository cho Question entity

    public UserSpeakingAttemptService(UserSpeakingAttemptRepository userSpeakingAttemptRepository,
                                      UserRepository userRepository,
                                      QuestionRepository questionRepository) {
        this.userSpeakingAttemptRepository = userSpeakingAttemptRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Lưu một lần thử nói của người dùng.
     * @param request Dữ liệu yêu cầu cho lần thử nói.
     * @return UserSpeakingAttemptResponse của lần thử nói đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public UserSpeakingAttemptResponse saveSpeakingAttempt(UserSpeakingAttemptRequest request) {
        if (request == null || request.userId() == null || request.questionId() == null ||
                request.originalPromptText() == null || request.originalPromptText().trim().isEmpty() ||
                request.userAudioUrl() == null || request.userAudioUrl().trim().isEmpty() ||
                request.userTranscribedBySTT() == null || request.userTranscribedBySTT().trim().isEmpty() ||
                request.pronunciationScore() == null || request.overallScore() == null) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, questionId, originalPromptText, userAudioUrl, userTranscribedBySTT, pronunciationScore, overallScore) không được để trống.");
        }
        if (request.pronunciationScore() < 0 || request.pronunciationScore() > 100 ||
                request.overallScore() < 0 || request.overallScore() > 100) {
            throw new IllegalArgumentException("Điểm phát âm và tổng thể phải nằm trong khoảng từ 0 đến 100.");
        }
        if (request.fluencyScore() != null && (request.fluencyScore() < 0 || request.fluencyScore() > 100)) {
            throw new IllegalArgumentException("Điểm lưu loát phải nằm trong khoảng từ 0 đến 100.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + request.questionId()));

        UserSpeakingAttempt attempt = new UserSpeakingAttempt();
        attempt.setUser(user);
        attempt.setQuestion(question);
        attempt.setOriginalPromptText(request.originalPromptText().trim());
        attempt.setUserAudioUrl(request.userAudioUrl().trim());
        attempt.setUserTranscribedBySTT(request.userTranscribedBySTT().trim());
        attempt.setPronunciationScore(request.pronunciationScore());
        attempt.setFluencyScore(request.fluencyScore()); // Có thể null
        attempt.setOverallScore(request.overallScore());
        attempt.setAttemptDate(LocalDateTime.now()); // Đặt thời gian tạo

        attempt = userSpeakingAttemptRepository.save(attempt);
        return mapToUserSpeakingAttemptResponse(attempt);
    }

    /**
     * Lấy lần thử nói của người dùng theo ID.
     * @param attemptId ID của lần thử nói.
     * @return UserSpeakingAttemptResponse của lần thử nói.
     * @throws IllegalArgumentException nếu attemptId trống hoặc không tìm thấy.
     */
    @Transactional(readOnly = true)
    public UserSpeakingAttemptResponse getSpeakingAttemptById(Integer attemptId) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        UserSpeakingAttempt attempt = userSpeakingAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử nói với ID: " + attemptId));
        return mapToUserSpeakingAttemptResponse(attempt);
    }

    /**
     * Lấy tất cả các lần thử nói của một người dùng.
     * @param userId ID của người dùng.
     * @return Danh sách UserSpeakingAttemptResponse.
     * @throws IllegalArgumentException nếu userId không hợp lệ.
     */
    @Transactional(readOnly = true)
    public List<UserSpeakingAttemptResponse> getSpeakingAttemptsByUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
        }

        return userSpeakingAttemptRepository.findByUserUserId(userId).stream()
                .map(this::mapToUserSpeakingAttemptResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả các lần thử nói cho một câu hỏi cụ thể.
     * @param questionId ID của câu hỏi.
     * @return Danh sách UserSpeakingAttemptResponse.
     * @throws IllegalArgumentException nếu questionId không hợp lệ.
     */
    @Transactional(readOnly = true)
    public List<UserSpeakingAttemptResponse> getSpeakingAttemptsByQuestion(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống.");
        }
        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId);
        }

        return userSpeakingAttemptRepository.findByQuestionQuestionId(questionId).stream()
                .map(this::mapToUserSpeakingAttemptResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xóa một lần thử nói của người dùng.
     * @param attemptId ID của lần thử nói cần xóa.
     * @throws IllegalArgumentException nếu attemptId trống hoặc không tìm thấy.
     */
    public void deleteSpeakingAttempt(Integer attemptId) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        if (!userSpeakingAttemptRepository.existsById(attemptId)) {
            throw new IllegalArgumentException("Không tìm thấy lần thử nói với ID: " + attemptId);
        }
        userSpeakingAttemptRepository.deleteById(attemptId);
    }


    /**
     * Phương thức trợ giúp để ánh xạ UserSpeakingAttempt entity sang UserSpeakingAttemptResponse DTO.
     * @param attempt Entity UserSpeakingAttempt.
     * @return UserSpeakingAttemptResponse DTO.
     */
    private UserSpeakingAttemptResponse mapToUserSpeakingAttemptResponse(UserSpeakingAttempt attempt) {
        return new UserSpeakingAttemptResponse(
                attempt.getAttemptId(),
                attempt.getUser().getUserId(),
                attempt.getQuestion().getQuestionId(),
                attempt.getOriginalPromptText(),
                attempt.getUserAudioUrl(),
                attempt.getUserTranscribedBySTT(),
                attempt.getPronunciationScore(),
                attempt.getFluencyScore(),
                attempt.getOverallScore(),
                attempt.getAttemptDate()
        );
    }
}