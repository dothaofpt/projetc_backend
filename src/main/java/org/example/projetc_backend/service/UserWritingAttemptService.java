package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserWritingAttemptRequest;
import org.example.projetc_backend.dto.UserWritingAttemptResponse;
import org.example.projetc_backend.dto.UserWritingAttemptSearchRequest; // Import DTO mới
import org.example.projetc_backend.entity.UserWritingAttempt;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.PracticeActivity;
import org.example.projetc_backend.repository.UserWritingAttemptRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.PracticeActivityRepository;

import org.springframework.data.domain.Page; // Import Page
import org.springframework.data.domain.PageRequest; // Import PageRequest
import org.springframework.data.domain.Pageable; // Import Pageable
import org.springframework.data.domain.Sort; // Import Sort
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
    private final PracticeActivityRepository practiceActivityRepository;

    public UserWritingAttemptService(UserWritingAttemptRepository userWritingAttemptRepository,
                                     UserRepository userRepository,
                                     PracticeActivityRepository practiceActivityRepository) {
        this.userWritingAttemptRepository = userWritingAttemptRepository;
        this.userRepository = userRepository;
        this.practiceActivityRepository = practiceActivityRepository;
    }

    /**
     * Lưu một lần thử viết của người dùng.
     * @param request Dữ liệu yêu cầu cho lần thử viết.
     * @return UserWritingAttemptResponse của lần thử viết đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public UserWritingAttemptResponse saveWritingAttempt(UserWritingAttemptRequest request) {
        if (request == null || request.userId() == null || request.practiceActivityId() == null ||
                request.userWrittenText() == null || request.userWrittenText().trim().isEmpty()) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userWrittenText) không được để trống.");
        }
        if (request.overallScore() != null && (request.overallScore() < 0 || request.overallScore() > 100)) {
            throw new IllegalArgumentException("Điểm tổng thể phải nằm trong khoảng từ 0 đến 100.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        UserWritingAttempt attempt = new UserWritingAttempt();
        attempt.setUser(user);
        attempt.setPracticeActivity(practiceActivity);
        attempt.setUserWrittenText(request.userWrittenText().trim());
        attempt.setGrammarFeedback(request.grammarFeedback() != null ? request.grammarFeedback().trim() : null);
        attempt.setSpellingFeedback(request.spellingFeedback() != null ? request.spellingFeedback().trim() : null);
        attempt.setCohesionFeedback(request.cohesionFeedback() != null ? request.cohesionFeedback().trim() : null);
        attempt.setOverallScore(request.overallScore());
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
        if (request.overallScore() != null && (request.overallScore() < 0 || request.overallScore() > 100)) {
            throw new IllegalArgumentException("Điểm tổng thể phải nằm trong khoảng từ 0 đến 100.");
        }

        UserWritingAttempt existingAttempt = userWritingAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử viết với ID: " + attemptId));

        // Kiểm tra user và practiceActivity có tồn tại không
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        // Cập nhật các trường
        existingAttempt.setUser(user);
        existingAttempt.setPracticeActivity(practiceActivity);
        existingAttempt.setUserWrittenText(request.userWrittenText().trim());
        existingAttempt.setGrammarFeedback(request.grammarFeedback() != null ? request.grammarFeedback().trim() : null);
        existingAttempt.setSpellingFeedback(request.spellingFeedback() != null ? request.spellingFeedback().trim() : null);
        existingAttempt.setCohesionFeedback(request.cohesionFeedback() != null ? request.cohesionFeedback().trim() : null);
        existingAttempt.setOverallScore(request.overallScore());
        // Không cập nhật attemptDate ở đây vì nó là thời gian tạo ban đầu

        existingAttempt = userWritingAttemptRepository.save(existingAttempt); // Lưu lại bản cập nhật
        return mapToUserWritingAttemptResponse(existingAttempt);
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
     * Lấy tất cả các lần thử viết cho một hoạt động luyện tập cụ thể.
     * @param practiceActivityId ID của hoạt động luyện tập.
     * @return Danh sách UserWritingAttemptResponse.
     * @throws IllegalArgumentException nếu practiceActivityId không hợp lệ.
     */
    @Transactional(readOnly = true)
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



    /**
     * Tìm kiếm và phân trang các lần thử viết của người dùng.
     * @param searchRequest DTO chứa các tiêu chí tìm kiếm và thông tin phân trang.
     * @return Trang chứa UserWritingAttemptResponse.
     */
    @Transactional(readOnly = true)
    public Page<UserWritingAttemptResponse> searchAndPaginateWritingAttempts(UserWritingAttemptSearchRequest searchRequest) {
        // Tạo đối tượng Pageable từ DTO tìm kiếm
        Pageable pageable = PageRequest.of(
                searchRequest.page(),
                searchRequest.size(),
                Sort.by("attemptDate").descending() // Sắp xếp theo ngày thử giảm dần
        );

        // Gọi phương thức tìm kiếm từ Repository
        Page<UserWritingAttempt> attemptsPage = userWritingAttemptRepository.searchWritingAttempts(
                searchRequest.userId(),
                searchRequest.practiceActivityId(),
                searchRequest.minOverallScore(),
                searchRequest.maxOverallScore(),
                pageable
        );

        // Ánh xạ Page của Entity sang Page của DTO Response
        return attemptsPage.map(this::mapToUserWritingAttemptResponse);
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
                attempt.getPracticeActivity() != null ? attempt.getPracticeActivity().getActivityId() : null,
                attempt.getUserWrittenText(),
                attempt.getGrammarFeedback(),
                attempt.getSpellingFeedback(),
                attempt.getCohesionFeedback(),
                attempt.getOverallScore(),
                attempt.getAttemptDate()
        );
    }
}