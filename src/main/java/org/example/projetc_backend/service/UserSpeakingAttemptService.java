package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserSpeakingAttemptRequest;
import org.example.projetc_backend.dto.UserSpeakingAttemptResponse;
import org.example.projetc_backend.dto.UserSpeakingAttemptSearchRequest; // Import DTO mới
import org.example.projetc_backend.entity.UserSpeakingAttempt;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.PracticeActivity;
import org.example.projetc_backend.repository.UserSpeakingAttemptRepository;
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
public class UserSpeakingAttemptService {

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
     * @param request Dữ liệu yêu cầu cho lần thử nói.
     * @return UserSpeakingAttemptResponse của lần thử nói đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public UserSpeakingAttemptResponse saveSpeakingAttempt(UserSpeakingAttemptRequest request) {
        // Đã thay đổi: questionId thành practiceActivityId, bỏ originalPromptText
        if (request == null || request.userId() == null || request.practiceActivityId() == null ||
                request.userAudioUrl() == null || request.userAudioUrl().trim().isEmpty() ||
                request.userTranscribedBySTT() == null || request.userTranscribedBySTT().trim().isEmpty() ||
                request.pronunciationScore() == null || request.overallScore() == null) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userAudioUrl, userTranscribedBySTT, pronunciationScore, overallScore) không được để trống.");
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

        // Đã thay đổi: Tìm PracticeActivity thay vì Question
        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        UserSpeakingAttempt attempt = new UserSpeakingAttempt();
        attempt.setUser(user);
        attempt.setPracticeActivity(practiceActivity); // Đã thay đổi: setPracticeActivity
        // Đã bỏ: setOriginalPromptText vì đã có trong PracticeActivity
        attempt.setUserAudioUrl(request.userAudioUrl().trim());
        attempt.setUserTranscribedBySTT(request.userTranscribedBySTT().trim());
        attempt.setPronunciationScore(request.pronunciationScore());
        attempt.setFluencyScore(request.fluencyScore());
        attempt.setOverallScore(request.overallScore());
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
                request.userAudioUrl() == null || request.userAudioUrl().trim().isEmpty() ||
                request.userTranscribedBySTT() == null || request.userTranscribedBySTT().trim().isEmpty() ||
                request.pronunciationScore() == null || request.overallScore() == null) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userAudioUrl, userTranscribedBySTT, pronunciationScore, overallScore) không được để trống.");
        }
        if (request.pronunciationScore() < 0 || request.pronunciationScore() > 100 ||
                request.overallScore() < 0 || request.overallScore() > 100) {
            throw new IllegalArgumentException("Điểm phát âm và tổng thể phải nằm trong khoảng từ 0 đến 100.");
        }
        if (request.fluencyScore() != null && (request.fluencyScore() < 0 || request.fluencyScore() > 100)) {
            throw new IllegalArgumentException("Điểm lưu loát phải nằm trong khoảng từ 0 đến 100.");
        }

        UserSpeakingAttempt existingAttempt = userSpeakingAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử nói với ID: " + attemptId));

        // Kiểm tra user và practiceActivity có tồn tại không
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        // Cập nhật các trường
        existingAttempt.setUser(user);
        existingAttempt.setPracticeActivity(practiceActivity);
        existingAttempt.setUserAudioUrl(request.userAudioUrl().trim());
        existingAttempt.setUserTranscribedBySTT(request.userTranscribedBySTT().trim());
        existingAttempt.setPronunciationScore(request.pronunciationScore());
        existingAttempt.setFluencyScore(request.fluencyScore());
        existingAttempt.setOverallScore(request.overallScore());
        // Không cập nhật attemptDate ở đây vì nó là thời gian tạo ban đầu

        existingAttempt = userSpeakingAttemptRepository.save(existingAttempt); // Lưu lại bản cập nhật
        return mapToUserSpeakingAttemptResponse(existingAttempt);
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
     * Lấy tất cả các lần thử nói cho một hoạt động luyện tập cụ thể.
     * @param practiceActivityId ID của hoạt động luyện tập.
     * @return Danh sách UserSpeakingAttemptResponse.
     * @throws IllegalArgumentException nếu practiceActivityId không hợp lệ.
     */
    @Transactional(readOnly = true)
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



    /**
     * Tìm kiếm và phân trang các lần thử nói của người dùng.
     * @param searchRequest DTO chứa các tiêu chí tìm kiếm và thông tin phân trang.
     * @return Trang chứa UserSpeakingAttemptResponse.
     */
    @Transactional(readOnly = true)
    public Page<UserSpeakingAttemptResponse> searchAndPaginateSpeakingAttempts(UserSpeakingAttemptSearchRequest searchRequest) {
        // Tạo đối tượng Pageable từ DTO tìm kiếm
        Pageable pageable = PageRequest.of(
                searchRequest.page(),
                searchRequest.size(),
                Sort.by("attemptDate").descending() // Sắp xếp theo ngày thử giảm dần
        );

        // Gọi phương thức tìm kiếm từ Repository
        Page<UserSpeakingAttempt> attemptsPage = userSpeakingAttemptRepository.searchSpeakingAttempts(
                searchRequest.userId(),
                searchRequest.practiceActivityId(),
                searchRequest.minOverallScore(),
                searchRequest.maxOverallScore(),
                pageable
        );

        // Ánh xạ Page của Entity sang Page của DTO Response
        return attemptsPage.map(this::mapToUserSpeakingAttemptResponse);
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
                // Đã thay đổi: Lấy ID của PracticeActivity
                attempt.getPracticeActivity() != null ? attempt.getPracticeActivity().getActivityId() : null,
                // Đã bỏ trường này khỏi DTO Response
                // attempt.getOriginalPromptText(),
                attempt.getUserAudioUrl(),
                attempt.getUserTranscribedBySTT(),
                attempt.getPronunciationScore(),
                attempt.getFluencyScore(),
                attempt.getOverallScore(),
                attempt.getAttemptDate()
        );
    }
}