package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserSpeakingAttemptRequest;
import org.example.projetc_backend.dto.UserSpeakingAttemptResponse;
import org.example.projetc_backend.dto.UserSpeakingAttemptSearchRequest;
import org.example.projetc_backend.entity.UserSpeakingAttempt;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.PracticeActivity;
import org.example.projetc_backend.repository.UserSpeakingAttemptRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.PracticeActivityRepository;
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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class UserSpeakingAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(UserSpeakingAttemptService.class);

    private final UserSpeakingAttemptRepository userSpeakingAttemptRepository;
    private final UserRepository userRepository;
    private final PracticeActivityRepository practiceActivityRepository;

    public UserSpeakingAttemptService(UserSpeakingAttemptRepository userSpeakingAttemptRepository,
                                      UserRepository userRepository,
                                      PracticeActivityRepository practiceActivityRepository) {
        this.userSpeakingAttemptRepository = userSpeakingAttemptRepository;
        this.userRepository = userRepository;
        this.practiceActivityRepository = practiceActivityRepository;
    }

    /**
     * Lưu một lần thử nói của người dùng.
     * Thực hiện chấm điểm phát âm/lưu loát tại backend bằng logic đơn giản.
     * @param request Dữ liệu yêu cầu cho lần thử nói (có thể bao gồm userTranscribedBySTT từ frontend).
     * @return UserSpeakingAttemptResponse của lần thử nói đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public UserSpeakingAttemptResponse saveSpeakingAttempt(UserSpeakingAttemptRequest request) {
        if (request == null || request.userId() == null || request.practiceActivityId() == null ||
                request.userAudioUrl() == null || request.userAudioUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userAudioUrl) không được để trống.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        // --- MỚI: Logic chấm điểm đơn giản hơn (Không AI/ML phức tạp) ---
        // Lấy văn bản gốc để so sánh (từ promptText hoặc transcriptText của PracticeActivity)
        String originalTextToCompare = practiceActivity.getPromptText(); // Giả sử promptText là nội dung cần nói
        if (originalTextToCompare == null || originalTextToCompare.trim().isEmpty()) {
            originalTextToCompare = practiceActivity.getTranscriptText(); // Fallback nếu promptText rỗng
        }

        // Lấy văn bản chuyển đổi từ người dùng (do frontend gửi hoặc là null)
        String userTranscribedBySTT = request.userTranscribedBySTT() != null ? request.userTranscribedBySTT().trim() : "";

        // Tính toán các điểm số (Đơn giản hóa đáng kể)
        Integer pronunciationScore = calculateSimplePronunciationScore(request.userAudioUrl()); // Dựa trên sự tồn tại của audio
        Integer fluencyScore = calculateSimpleFluencyScore(request.userAudioUrl()); // Dựa trên sự tồn tại của audio
        Integer overallScore = calculateSimpleSpeakingOverallScore(userTranscribedBySTT, originalTextToCompare); // Dựa trên so sánh văn bản

        if (originalTextToCompare == null || originalTextToCompare.trim().isEmpty()) {
            logger.warn("PracticeActivity (ID: {}) for speaking attempt has no original prompt/text to grade against. Overall score might be 0.", request.practiceActivityId());
            overallScore = 0; // Nếu không có bản gốc, điểm tổng thể là 0
        }
        // --- END Logic chấm điểm đơn giản hơn ---

        UserSpeakingAttempt attempt = new UserSpeakingAttempt();
        attempt.setUser(user);
        attempt.setPracticeActivity(practiceActivity);
        attempt.setUserAudioUrl(request.userAudioUrl().trim());
        attempt.setUserTranscribedBySTT(userTranscribedBySTT); // Lưu lại kết quả STT (dù từ frontend hay backend)
        attempt.setPronunciationScore(pronunciationScore);
        attempt.setFluencyScore(fluencyScore);
        attempt.setOverallScore(overallScore);
        attempt.setAttemptDate(LocalDateTime.now());

        attempt = userSpeakingAttemptRepository.save(attempt);
        return mapToUserSpeakingAttemptResponse(attempt);
    }

    /**
     * Cập nhật một lần thử nói hiện có của người dùng.
     * @param attemptId ID của lần thử nói cần cập nhật.
     * @param request Dữ liệu yêu cầu cập nhật.
     * @return UserSpeakingAttemptResponse của lần thử nói đã cập nhật.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc không tìm thấy lần thử.
     */
    public UserSpeakingAttemptResponse updateSpeakingAttempt(Integer attemptId, UserSpeakingAttemptRequest request) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        if (request == null || request.userId() == null || request.practiceActivityId() == null ||
                request.userAudioUrl() == null || request.userAudioUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userAudioUrl) không được để trống.");
        }

        UserSpeakingAttempt existingAttempt = userSpeakingAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử nói với ID: " + attemptId));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        // MỚI: Tính lại điểm và STT khi cập nhật (Logic đơn giản)
        String originalTextToCompare = practiceActivity.getPromptText();
        if (originalTextToCompare == null || originalTextToCompare.trim().isEmpty()) {
            originalTextToCompare = practiceActivity.getTranscriptText();
        }
        String userTranscribedBySTT = request.userTranscribedBySTT() != null ? request.userTranscribedBySTT().trim() : "";

        Integer pronunciationScore = calculateSimplePronunciationScore(request.userAudioUrl());
        Integer fluencyScore = calculateSimpleFluencyScore(request.userAudioUrl());
        Integer overallScore = calculateSimpleSpeakingOverallScore(userTranscribedBySTT, originalTextToCompare);

        if (originalTextToCompare == null || originalTextToCompare.trim().isEmpty()) {
            overallScore = 0;
        }

        existingAttempt.setUser(user);
        existingAttempt.setPracticeActivity(practiceActivity);
        existingAttempt.setUserAudioUrl(request.userAudioUrl().trim());
        existingAttempt.setUserTranscribedBySTT(userTranscribedBySTT);
        existingAttempt.setPronunciationScore(pronunciationScore);
        existingAttempt.setFluencyScore(fluencyScore);
        existingAttempt.setOverallScore(overallScore);

        existingAttempt = userSpeakingAttemptRepository.save(existingAttempt);
        return mapToUserSpeakingAttemptResponse(existingAttempt);
    }

    public UserSpeakingAttemptResponse getSpeakingAttemptById(Integer attemptId) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        UserSpeakingAttempt attempt = userSpeakingAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử nói với ID: " + attemptId));
        return mapToUserSpeakingAttemptResponse(attempt);
    }

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

    public List<UserSpeakingAttemptResponse> getSpeakingAttemptsByPracticeActivity(Integer practiceActivityId) {
        if (practiceActivityId == null) {
            throw new IllegalArgumentException("Practice Activity ID không được để trống.");
        }
        if (!practiceActivityRepository.existsById(practiceActivityId)) {
            throw new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + practiceActivityId);
        }

        return userSpeakingAttemptRepository.findByPracticeActivityActivityId(practiceActivityId).stream()
                .map(this::mapToUserSpeakingAttemptResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserSpeakingAttemptResponse> searchAndPaginateSpeakingAttempts(UserSpeakingAttemptSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
                searchRequest.page(),
                searchRequest.size(),
                Sort.by("attemptDate").descending()
        );

        Page<UserSpeakingAttempt> attemptsPage = userSpeakingAttemptRepository.searchSpeakingAttempts(
                searchRequest.userId(),
                searchRequest.practiceActivityId(),
                searchRequest.minOverallScore(),
                searchRequest.maxOverallScore(),
                pageable
        );

        return attemptsPage.map(this::mapToUserSpeakingAttemptResponse);
    }

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
        String practiceActivityTitle = null;
        String originalPromptText = null;
        String expectedOutputText = null;

        if (attempt.getPracticeActivity() != null) {
            practiceActivityTitle = attempt.getPracticeActivity().getTitle();
            originalPromptText = attempt.getPracticeActivity().getPromptText();
            expectedOutputText = attempt.getPracticeActivity().getExpectedOutputText();
        }

        return new UserSpeakingAttemptResponse(
                attempt.getAttemptId(),
                attempt.getUser().getUserId(),
                attempt.getPracticeActivity() != null ? attempt.getPracticeActivity().getActivityId() : null,
                attempt.getUserAudioUrl(),
                attempt.getUserTranscribedBySTT(),
                attempt.getPronunciationScore(),
                attempt.getFluencyScore(),
                attempt.getOverallScore(),
                attempt.getAttemptDate(),
                practiceActivityTitle,
                originalPromptText,
                expectedOutputText
        );
    }

    // MỚI: Hàm chấm điểm phát âm đơn giản
    private Integer calculateSimplePronunciationScore(String audioUrl) {
        // Đây là một placeholder đơn giản.
        // Trong thực tế, việc chấm điểm phát âm đòi hỏi phân tích audio (AI/ML).
        // Ví dụ: chỉ cho điểm nếu audio được cung cấp.
        return audioUrl != null && !audioUrl.trim().isEmpty() ? 50 : 0; // Giả sử 50 điểm nếu có audio
    }

    // MỚI: Hàm chấm điểm lưu loát đơn giản
    private Integer calculateSimpleFluencyScore(String audioUrl) {
        // Tương tự, đây là một placeholder.
        // Lưu loát thường được đánh giá qua tốc độ nói, khoảng dừng, v.v. (AI/ML).
        return audioUrl != null && !audioUrl.trim().isEmpty() ? 50 : 0; // Giả sử 50 điểm nếu có audio
    }

    // Đã đổi tên hàm từ calculateOverallSpeakingScore để tránh nhầm lẫn nếu có chấm điểm phát âm/lưu loát riêng
    private int calculateSimpleSpeakingOverallScore(String userTranscribedText, String originalText) {
        if (originalText == null || originalText.trim().isEmpty()) {
            return 0; // Không có bản gốc để chấm điểm
        }
        if (userTranscribedText == null || userTranscribedText.trim().isEmpty()) {
            return 0; // Người dùng không nói gì
        }

        String normalizedUserText = userTranscribedText.trim().toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        String normalizedOriginalText = originalText.trim().toLowerCase().replaceAll("[^a-z0-9\\s]", "");

        String[] userWords = normalizedUserText.split("\\s+");
        String[] originalWords = normalizedOriginalText.split("\\s+");

        if (originalWords.length == 0) {
            return userWords.length == 0 ? 100 : 0;
        }

        int correctWords = 0;
        for (int i = 0; i < Math.min(userWords.length, originalWords.length); i++) {
            if (userWords[i].equals(originalWords[i])) {
                correctWords++;
            }
        }

        double accuracy = (double) correctWords / originalWords.length;
        return (int) Math.round(accuracy * 100);
    }
}