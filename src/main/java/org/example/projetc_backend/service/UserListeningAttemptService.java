package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserListeningAttemptRequest;
import org.example.projetc_backend.dto.UserListeningAttemptResponse;
import org.example.projetc_backend.entity.UserListeningAttempt;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Question; // Import Question entity
import org.example.projetc_backend.repository.UserListeningAttemptRepository;
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
@Transactional // Đặt Transactional ở cấp độ class nếu hầu hết các phương thức là transactional
public class UserListeningAttemptService {

    private final UserListeningAttemptRepository userListeningAttemptRepository;
    private final UserRepository userRepository;
    private final QuestionRepository questionRepository; // Repository cho Question entity

    public UserListeningAttemptService(UserListeningAttemptRepository userListeningAttemptRepository,
                                       UserRepository userRepository,
                                       QuestionRepository questionRepository) {
        this.userListeningAttemptRepository = userListeningAttemptRepository;
        this.userRepository = userRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Lưu một lần thử nghe của người dùng.
     * @param request Dữ liệu yêu cầu cho lần thử nghe.
     * @return UserListeningAttemptResponse của lần thử nghe đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public UserListeningAttemptResponse saveListeningAttempt(UserListeningAttemptRequest request) {
        if (request == null || request.userId() == null || request.questionId() == null ||
                request.audioMaterialUrl() == null || request.audioMaterialUrl().trim().isEmpty() ||
                request.userTranscribedText() == null || request.userTranscribedText().trim().isEmpty() ||
                request.actualTranscriptText() == null || request.actualTranscriptText().trim().isEmpty() ||
                request.accuracyScore() == null) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, questionId, audioMaterialUrl, userTranscribedText, actualTranscriptText, accuracyScore) không được để trống.");
        }
        if (request.accuracyScore() < 0 || request.accuracyScore() > 100) {
            throw new IllegalArgumentException("Điểm chính xác phải nằm trong khoảng từ 0 đến 100.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + request.questionId()));

        UserListeningAttempt attempt = new UserListeningAttempt();
        attempt.setUser(user);
        attempt.setQuestion(question);
        attempt.setAudioMaterialUrl(request.audioMaterialUrl().trim());
        attempt.setUserTranscribedText(request.userTranscribedText().trim());
        attempt.setActualTranscriptText(request.actualTranscriptText().trim());
        attempt.setAccuracyScore(request.accuracyScore());
        attempt.setAttemptDate(LocalDateTime.now()); // Đặt thời gian tạo

        attempt = userListeningAttemptRepository.save(attempt);
        return mapToUserListeningAttemptResponse(attempt);
    }

    /**
     * Lấy một lần thử nghe của người dùng theo ID.
     * @param attemptId ID của lần thử nghe.
     * @return UserListeningAttemptResponse của lần thử nghe.
     * @throws IllegalArgumentException nếu attemptId không hợp lệ.
     */
    @Transactional(readOnly = true)
    public UserListeningAttemptResponse getListeningAttemptById(Integer attemptId) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        UserListeningAttempt attempt = userListeningAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử nghe với ID: " + attemptId));
        return mapToUserListeningAttemptResponse(attempt);
    }

    /**
     * Lấy tất cả các lần thử nghe của một người dùng.
     * @param userId ID của người dùng.
     * @return Danh sách UserListeningAttemptResponse.
     * @throws IllegalArgumentException nếu userId không hợp lệ.
     */
    @Transactional(readOnly = true)
    public List<UserListeningAttemptResponse> getListeningAttemptsByUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
        }

        return userListeningAttemptRepository.findByUserUserId(userId).stream()
                .map(this::mapToUserListeningAttemptResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xóa một lần thử nghe của người dùng.
     * @param attemptId ID của lần thử nghe cần xóa.
     * @throws IllegalArgumentException nếu attemptId không hợp lệ.
     */
    public void deleteListeningAttempt(Integer attemptId) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        if (!userListeningAttemptRepository.existsById(attemptId)) {
            throw new IllegalArgumentException("Không tìm thấy lần thử nghe với ID: " + attemptId);
        }
        userListeningAttemptRepository.deleteById(attemptId);
    }

    /**
     * Phương thức trợ giúp để ánh xạ UserListeningAttempt entity sang UserListeningAttemptResponse DTO.
     * @param attempt Entity UserListeningAttempt.
     * @return UserListeningAttemptResponse DTO.
     */
    private UserListeningAttemptResponse mapToUserListeningAttemptResponse(UserListeningAttempt attempt) {
        return new UserListeningAttemptResponse(
                attempt.getAttemptId(),
                attempt.getUser().getUserId(),
                attempt.getQuestion().getQuestionId(), // Lấy ID của Question
                attempt.getAudioMaterialUrl(),
                attempt.getUserTranscribedText(),
                attempt.getActualTranscriptText(),
                attempt.getAccuracyScore(),
                attempt.getAttemptDate()
        );
    }
}