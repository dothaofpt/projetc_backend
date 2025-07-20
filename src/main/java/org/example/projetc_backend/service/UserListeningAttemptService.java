package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserListeningAttemptRequest;
import org.example.projetc_backend.dto.UserListeningAttemptResponse;
import org.example.projetc_backend.dto.UserListeningAttemptSearchRequest;
import org.example.projetc_backend.entity.UserListeningAttempt;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.PracticeActivity;
import org.example.projetc_backend.repository.UserListeningAttemptRepository;
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

// MỚI: Thêm import cho Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Service
@Transactional
public class UserListeningAttemptService {

    // MỚI: Khai báo và khởi tạo Logger
    private static final Logger logger = LoggerFactory.getLogger(UserListeningAttemptService.class);

    private final UserListeningAttemptRepository userListeningAttemptRepository;
    private final UserRepository userRepository;
    private final PracticeActivityRepository practiceActivityRepository;

    public UserListeningAttemptService(UserListeningAttemptRepository userListeningAttemptRepository,
                                       UserRepository userRepository,
                                       PracticeActivityRepository practiceActivityRepository) {
        this.userListeningAttemptRepository = userListeningAttemptRepository;
        this.userRepository = userRepository;
        this.practiceActivityRepository = practiceActivityRepository;
    }

    /**
     * Lưu một lần thử nghe của người dùng và tính điểm chính xác tại backend.
     * @param request Dữ liệu yêu cầu cho lần thử nghe (không có accuracyScore).
     * @return UserListeningAttemptResponse của lần thử nghe đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public UserListeningAttemptResponse saveListeningAttempt(UserListeningAttemptRequest request) {
        if (request == null || request.userId() == null || request.practiceActivityId() == null ||
                request.userTranscribedText() == null || request.userTranscribedText().trim().isEmpty()) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userTranscribedText) không được để trống.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        String actualTranscript = practiceActivity.getTranscriptText();
        if (actualTranscript == null || actualTranscript.trim().isEmpty()) {
            logger.warn("PracticeActivity (ID: {}) for listening attempt has no actual transcript text. Accuracy score will be 0.", request.practiceActivityId());
        }

        int accuracyScore = calculateListeningAccuracy(request.userTranscribedText(), actualTranscript);

        UserListeningAttempt attempt = new UserListeningAttempt();
        attempt.setUser(user);
        attempt.setPracticeActivity(practiceActivity);
        attempt.setUserTranscribedText(request.userTranscribedText().trim());
        attempt.setAccuracyScore(accuracyScore);
        attempt.setAttemptDate(LocalDateTime.now());

        attempt = userListeningAttemptRepository.save(attempt);
        return mapToUserListeningAttemptResponse(attempt);
    }

    /**
     * Cập nhật một lần thử nghe hiện có của người dùng.
     * @param attemptId ID của lần thử nghe cần cập nhật.
     * @param request Dữ liệu yêu cầu cập nhật.
     * @return UserListeningAttemptResponse của lần thử nghe đã cập nhật.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc không tìm thấy lần thử.
     */
    public UserListeningAttemptResponse updateListeningAttempt(Integer attemptId, UserListeningAttemptRequest request) {
        if (attemptId == null) {
            throw new IllegalArgumentException("Attempt ID không được để trống.");
        }
        if (request == null || request.userId() == null || request.practiceActivityId() == null ||
                request.userTranscribedText() == null || request.userTranscribedText().trim().isEmpty()) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userTranscribedText) không được để trống.");
        }

        UserListeningAttempt existingAttempt = userListeningAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử nghe với ID: " + attemptId));

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        String actualTranscript = practiceActivity.getTranscriptText();
        int accuracyScore = calculateListeningAccuracy(request.userTranscribedText(), actualTranscript);

        existingAttempt.setUser(user);
        existingAttempt.setPracticeActivity(practiceActivity);
        existingAttempt.setUserTranscribedText(request.userTranscribedText().trim());
        existingAttempt.setAccuracyScore(accuracyScore);

        existingAttempt = userListeningAttemptRepository.save(existingAttempt);
        return mapToUserListeningAttemptResponse(existingAttempt);
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
     * Lấy tất cả các lần thử nghe cho một hoạt động luyện tập cụ thể.
     * @param practiceActivityId ID của hoạt động luyện tập.
     * @return Danh sách UserListeningAttemptResponse.
     * @throws IllegalArgumentException nếu practiceActivityId không hợp lệ.
     */
    @Transactional(readOnly = true)
    public List<UserListeningAttemptResponse> getListeningAttemptsByPracticeActivity(Integer practiceActivityId) {
        if (practiceActivityId == null) {
            throw new IllegalArgumentException("Practice Activity ID không được để trống.");
        }
        if (!practiceActivityRepository.existsById(practiceActivityId)) {
            throw new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + practiceActivityId);
        }

        return userListeningAttemptRepository.findByPracticeActivityActivityId(practiceActivityId).stream()
                .map(this::mapToUserListeningAttemptResponse)
                .collect(Collectors.toList());
    }


    /**
     * Tìm kiếm và phân trang các lần thử nghe của người dùng.
     * @param searchRequest DTO chứa các tiêu chí tìm kiếm và thông tin phân trang.
     * @return Trang chứa UserListeningAttemptResponse.
     */
    @Transactional(readOnly = true)
    public Page<UserListeningAttemptResponse> searchAndPaginateListeningAttempts(UserListeningAttemptSearchRequest searchRequest) {
        Pageable pageable = PageRequest.of(
                searchRequest.page(),
                searchRequest.size(),
                Sort.by("attemptDate").descending()
        );

        Page<UserListeningAttempt> attemptsPage = userListeningAttemptRepository.searchListeningAttempts(
                searchRequest.userId(),
                searchRequest.practiceActivityId(),
                searchRequest.minAccuracyScore(),
                searchRequest.maxAccuracyScore(),
                pageable
        );

        return attemptsPage.map(this::mapToUserListeningAttemptResponse);
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
        String practiceActivityTitle = null;
        String audioMaterialUrl = null;
        String actualTranscriptText = null;

        if (attempt.getPracticeActivity() != null) {
            practiceActivityTitle = attempt.getPracticeActivity().getTitle();
            audioMaterialUrl = attempt.getPracticeActivity().getMaterialUrl();
            actualTranscriptText = attempt.getPracticeActivity().getTranscriptText();
        }

        return new UserListeningAttemptResponse(
                attempt.getAttemptId(),
                attempt.getUser().getUserId(),
                attempt.getPracticeActivity() != null ? attempt.getPracticeActivity().getActivityId() : null,
                attempt.getUserTranscribedText(),
                attempt.getAccuracyScore(),
                attempt.getAttemptDate(),
                practiceActivityTitle,
                audioMaterialUrl,
                actualTranscriptText
        );
    }

    /**
     * Logic tính toán điểm chính xác cho bài nghe.
     * @param userText Văn bản người dùng đã gõ.
     * @param actualText Văn bản gốc (đáp án đúng).
     * @return Điểm chính xác từ 0 đến 100.
     */
    private int calculateListeningAccuracy(String userText, String actualText) {
        if (actualText == null || actualText.trim().isEmpty()) {
            return 0;
        }

        // Chuẩn hóa văn bản: chuyển về chữ thường, bỏ dấu câu (tùy ý)
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
}