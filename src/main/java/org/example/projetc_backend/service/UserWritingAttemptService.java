package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserWritingAttemptRequest;
import org.example.projetc_backend.dto.UserWritingAttemptResponse;
import org.example.projetc_backend.dto.UserWritingAttemptSearchRequest;
import org.example.projetc_backend.entity.UserWritingAttempt;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.PracticeActivity;
import org.example.projetc_backend.repository.UserWritingAttemptRepository;
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
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class UserWritingAttemptService {

    private static final Logger logger = LoggerFactory.getLogger(UserWritingAttemptService.class);

    private final UserWritingAttemptRepository userWritingAttemptRepository;
    private final UserRepository userRepository;
    private final PracticeActivityRepository practiceActivityRepository;

    public UserWritingAttemptService(UserWritingAttemptRepository userWritingAttemptRepository,
                                     UserRepository userRepository,
                                     PracticeActivityRepository practiceActivityRepository) {
        this.userWritingAttemptRepository = userWritingAttemptRepository;
        this.userRepository = userRepository;
        this.practiceActivityRepository = practiceActivityRepository;
    }

    /**
     * Lưu một lần thử viết của người dùng và chấm điểm tại backend.
     * @param request Dữ liệu yêu cầu cho lần thử viết (chỉ userWrittenText).
     * @return UserWritingAttemptResponse của lần thử viết đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public UserWritingAttemptResponse saveWritingAttempt(UserWritingAttemptRequest request) {
        if (request == null || request.userId() == null || request.practiceActivityId() == null ||
                request.userWrittenText() == null || request.userWrittenText().trim().isEmpty()) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userWrittenText) không được để trống.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        String originalTextOrPrompt = practiceActivity.getExpectedOutputText(); // Hoặc promptText/transcriptText tùy bài tập
        if (originalTextOrPrompt == null || originalTextOrPrompt.trim().isEmpty()) {
            logger.warn("PracticeActivity (ID: {}) for writing attempt has no expected output/prompt to grade against. Overall score will be 0.", request.practiceActivityId());
        }

        String grammarFeedback = "Phản hồi ngữ pháp đơn giản: Cấu trúc câu ổn định.";
        String spellingFeedback = "Phản hồi chính tả đơn giản: Kiểm tra lỗi chính tả cơ bản.";
        String cohesionFeedback = "Phản hồi mạch lạc đơn giản: Các ý tưởng có vẻ liên kết.";
        Integer overallScore = calculateWritingOverallScore(request.userWrittenText(), originalTextOrPrompt);

        UserWritingAttempt attempt = new UserWritingAttempt();
        attempt.setUser(user);
        attempt.setPracticeActivity(practiceActivity);
        attempt.setUserWrittenText(request.userWrittenText().trim());
        attempt.setGrammarFeedback(grammarFeedback);
        attempt.setSpellingFeedback(spellingFeedback);
        attempt.setCohesionFeedback(cohesionFeedback);
        attempt.setOverallScore(overallScore);
        attempt.setAttemptDate(LocalDateTime.now());

        attempt = userWritingAttemptRepository.save(attempt);
        return mapToUserWritingAttemptResponse(attempt);
    }

    /**
     * Cập nhật một lần thử viết hiện có của người dùng.
     * @param attemptId ID của lần thử viết cần cập nhật.
     * @param request Dữ liệu yêu cầu cập nhật.
     * @return UserWritingAttemptResponse của lần thử viết đã cập nhật.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc không tìm thấy lần thử.
     */
    public UserWritingAttemptResponse updateWritingAttempt(Integer attemptId, UserWritingAttemptRequest request) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        if (request == null || request.userId() == null || request.practiceActivityId() == null ||
                request.userWrittenText() == null || request.userWrittenText().trim().isEmpty()) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userWrittenText) không được để trống.");
        }

        UserWritingAttempt existingAttempt = userWritingAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử viết với ID: " + attemptId));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        String originalTextOrPrompt = practiceActivity.getExpectedOutputText();
        String grammarFeedback = "Phản hồi ngữ pháp đơn giản (cập nhật).";
        String spellingFeedback = "Spelling feedback placeholder (updated).";
        String cohesionFeedback = "Cohesion feedback placeholder (updated).";
        Integer overallScore = calculateWritingOverallScore(request.userWrittenText(), originalTextOrPrompt);

        existingAttempt.setUser(user);
        existingAttempt.setPracticeActivity(practiceActivity);
        existingAttempt.setUserWrittenText(request.userWrittenText().trim());
        existingAttempt.setGrammarFeedback(grammarFeedback);
        existingAttempt.setSpellingFeedback(spellingFeedback);
        existingAttempt.setCohesionFeedback(cohesionFeedback);
        existingAttempt.setOverallScore(overallScore);

        existingAttempt = userWritingAttemptRepository.save(existingAttempt);
        return mapToUserWritingAttemptResponse(existingAttempt);
    }

    /**
     * Các phương thức GET, DELETE và SEARCH vẫn giữ nguyên như bạn đã cung cấp.
     */
    public UserWritingAttemptResponse getWritingAttemptById(Integer attemptId) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        UserWritingAttempt attempt = userWritingAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử viết với ID: " + attemptId));
        return mapToUserWritingAttemptResponse(attempt);
    }

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

    public List<UserWritingAttemptResponse> getWritingAttemptsByPracticeActivity(Integer practiceActivityId) {
        if (practiceActivityId == null) {
            throw new IllegalArgumentException("Practice Activity ID không được để trống.");
        }
        if (!practiceActivityRepository.existsById(practiceActivityId)) {
            throw new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + practiceActivityId);
        }

        return userWritingAttemptRepository.findByPracticeActivityActivityId(practiceActivityId).stream()
                .map(this::mapToUserWritingAttemptResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Page<UserWritingAttemptResponse> searchAndPaginateWritingAttempts(UserWritingAttemptSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
                searchRequest.page(),
                searchRequest.size(),
                Sort.by("attemptDate").descending()
        );

        Page<UserWritingAttempt> attemptsPage = userWritingAttemptRepository.searchWritingAttempts(
                searchRequest.userId(),
                searchRequest.practiceActivityId(),
                searchRequest.minOverallScore(),
                searchRequest.maxOverallScore(),
                pageable
        );

        return attemptsPage.map(this::mapToUserWritingAttemptResponse);
    }

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
        String practiceActivityTitle = null;
        String originalPromptText = null;
        String expectedOutputText = null;

        if (attempt.getPracticeActivity() != null) {
            practiceActivityTitle = attempt.getPracticeActivity().getTitle();
            originalPromptText = attempt.getPracticeActivity().getPromptText();
            expectedOutputText = attempt.getPracticeActivity().getExpectedOutputText();
        }

        return new UserWritingAttemptResponse(
                attempt.getAttemptId(),
                attempt.getUser().getUserId(),
                attempt.getPracticeActivity() != null ? attempt.getPracticeActivity().getActivityId() : null,
                attempt.getUserWrittenText(),
                attempt.getGrammarFeedback(),
                attempt.getSpellingFeedback(),
                attempt.getCohesionFeedback(),
                attempt.getOverallScore(),
                attempt.getAttemptDate(),
                practiceActivityTitle,
                originalPromptText,
                expectedOutputText
        );
    }

    /**
     * Logic tính toán điểm tổng thể cho bài viết.
     * @param userWrittenText Văn bản người dùng đã viết.
     * @param expectedOutputText Văn bản mẫu/đoạn văn gốc (dùng để chấm điểm).
     * @return Điểm tổng thể từ 0 đến 100.
     */
    private int calculateWritingOverallScore(String userWrittenText, String expectedOutputText) {
        if (expectedOutputText == null || expectedOutputText.trim().isEmpty()) {
            return 0; // Không có bản gốc để chấm điểm
        }
        // SỬA LỖI: Sử dụng tham số expectedOutputText thay vì expectedText
        String normalizedExpectedText = expectedOutputText.trim().toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        String normalizedUserText = userWrittenText.trim().toLowerCase().replaceAll("[^a-z0-9\\s]", "");


        // Ví dụ đơn giản: tính phần trăm từ đúng nếu là điền vào chỗ trống
        // Giả định dấu hiệu của chỗ trống có thể là "_" hoặc "["
        if (normalizedExpectedText.contains("_") || normalizedExpectedText.contains("[")) {
            // Đây là logic cho điền vào chỗ trống, so sánh từng từ
            return calculateListeningAccuracy(normalizedUserText, normalizedExpectedText);
        } else {
            // Đây là logic cho bài luận/đoạn văn, dùng độ tương đồng Jaccard
            double similarity = calculateJaccardSimilarity(normalizedUserText, normalizedExpectedText);
            return (int) Math.round(similarity * 100);
        }
    }

    // Helper cho calculateWritingOverallScore
    // Hàm này đã được dùng ở UserListeningAttemptService, giờ cần copy vào đây để tránh phụ thuộc
    private int calculateListeningAccuracy(String userText, String actualText) {
        if (actualText == null || actualText.trim().isEmpty()) {
            return 0;
        }

        String normalizedUserText = userText.trim().toLowerCase().replaceAll("[^a-z0-9\\s]", "");
        String normalizedActualText = actualText.trim().toLowerCase().replaceAll("[^a-z0-9\\s]", "");

        String[] userWords = normalizedUserText.split("\\s+");
        String[] actualWords = normalizedActualText.split("\\s+");

        if (actualWords.length == 0) {
            return userWords.length == 0 ? 100 : 0;
        }

        int correctWords = 0;
        for (int i = 0; i < Math.min(userWords.length, actualWords.length); i++) {
            if (userWords[i].equals(actualWords[i])) {
                correctWords++;
            }
        }
        double accuracy = (double) correctWords / actualWords.length;
        return (int) Math.round(accuracy * 100);
    }

    // Helper cho calculateWritingOverallScore
    // Hàm này cũng đã được dùng ở UserListeningAttemptService (nhưng không phải là logic chính ở đó),
    // giờ cần copy vào đây để tránh phụ thuộc nếu không có BaseService.
    private double calculateJaccardSimilarity(String s1, String s2) {
        if (s1.isEmpty() && s2.isEmpty()) return 1.0;
        if (s1.isEmpty() || s2.isEmpty()) return 0.0;

        String[] words1 = s1.split("\\s+");
        String[] words2 = s2.split("\\s+");

        java.util.Set<String> set1 = new java.util.HashSet<>(java.util.Arrays.asList(words1));
        java.util.Set<String> set2 = new java.util.HashSet<>(java.util.Arrays.asList(words2));

        java.util.Set<String> intersection = new java.util.HashSet<>(set1);
        intersection.retainAll(set2);

        java.util.Set<String> union = new java.util.HashSet<>(set1);
        union.addAll(set2);

        if (union.isEmpty()) return 0.0;
        return (double) intersection.size() / union.size();
    }
}