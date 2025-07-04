package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserListeningAttemptRequest;
import org.example.projetc_backend.dto.UserListeningAttemptResponse;
import org.example.projetc_backend.dto.UserListeningAttemptSearchRequest; // Import DTO mới
import org.example.projetc_backend.entity.UserListeningAttempt;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.PracticeActivity;
import org.example.projetc_backend.repository.UserListeningAttemptRepository;
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
public class UserListeningAttemptService {

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
     * Lưu một lần thử nghe của người dùng.
     * @param request Dữ liệu yêu cầu cho lần thử nghe.
     * @return UserListeningAttemptResponse của lần thử nghe đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ.
     */
    public UserListeningAttemptResponse saveListeningAttempt(UserListeningAttemptRequest request) {
        if (request == null || request.userId() == null || request.practiceActivityId() == null ||
                request.userTranscribedText() == null || request.userTranscribedText().trim().isEmpty() ||
                request.accuracyScore() == null) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userTranscribedText, accuracyScore) không được để trống.");
        }
        if (request.accuracyScore() < 0 || request.accuracyScore() > 100) {
            throw new IllegalArgumentException("Điểm chính xác phải nằm trong khoảng từ 0 đến 100.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        UserListeningAttempt attempt = new UserListeningAttempt();
        attempt.setUser(user);
        attempt.setPracticeActivity(practiceActivity);
        attempt.setUserTranscribedText(request.userTranscribedText().trim());
        attempt.setAccuracyScore(request.accuracyScore());
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
                request.userTranscribedText() == null || request.userTranscribedText().trim().isEmpty() ||
                request.accuracyScore() == null) {
            throw new IllegalArgumentException("Các trường bắt buộc (userId, practiceActivityId, userTranscribedText, accuracyScore) không được để trống.");
        }
        if (request.accuracyScore() < 0 || request.accuracyScore() > 100) {
            throw new IllegalArgumentException("Điểm chính xác phải nằm trong khoảng từ 0 đến 100.");
        }

        UserListeningAttempt existingAttempt = userListeningAttemptRepository.findById(attemptId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy lần thử nghe với ID: " + attemptId));

        // Kiểm tra user và practiceActivity có tồn tại không
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        PracticeActivity practiceActivity = practiceActivityRepository.findById(request.practiceActivityId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + request.practiceActivityId()));

        // Cập nhật các trường
        existingAttempt.setUser(user); // Có thể thay đổi người dùng (nếu logic cho phép)
        existingAttempt.setPracticeActivity(practiceActivity); // Có thể thay đổi hoạt động (nếu logic cho phép)
        existingAttempt.setUserTranscribedText(request.userTranscribedText().trim());
        existingAttempt.setAccuracyScore(request.accuracyScore());
        // Không cập nhật attemptDate ở đây vì nó là thời gian tạo ban đầu, trừ khi có yêu cầu cụ thể
        // existingAttempt.setAttemptDate(LocalDateTime.now()); // Nếu bạn muốn cập nhật thời gian sửa đổi

        existingAttempt = userListeningAttemptRepository.save(existingAttempt); // Lưu lại bản cập nhật
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
        // Tạo đối tượng Pageable từ DTO tìm kiếm
        Pageable pageable = PageRequest.of(
                searchRequest.page(),
                searchRequest.size(),
                Sort.by("attemptDate").descending() // Sắp xếp theo ngày thử giảm dần
        );

        // Gọi phương thức tìm kiếm từ Repository
        Page<UserListeningAttempt> attemptsPage = userListeningAttemptRepository.searchListeningAttempts(
                searchRequest.userId(),
                searchRequest.practiceActivityId(),
                searchRequest.minAccuracyScore(),
                searchRequest.maxAccuracyScore(),
                pageable
        );

        // Ánh xạ Page của Entity sang Page của DTO Response
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
        return new UserListeningAttemptResponse(
                attempt.getAttemptId(),
                attempt.getUser().getUserId(),
                attempt.getPracticeActivity() != null ? attempt.getPracticeActivity().getActivityId() : null,
                attempt.getUserTranscribedText(),
                attempt.getAccuracyScore(),
                attempt.getAttemptDate()
        );
    }
}