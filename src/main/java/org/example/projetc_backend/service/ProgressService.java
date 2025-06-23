package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.ProgressRequest;
import org.example.projetc_backend.dto.ProgressResponse;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.Progress;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.ProgressRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class ProgressService {

    private final ProgressRepository progressRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;

    public ProgressService(ProgressRepository progressRepository, UserRepository userRepository, LessonRepository lessonRepository) {
        this.progressRepository = progressRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
    }

    /**
     * Cập nhật hoặc tạo mới một bản ghi tiến độ cho một người dùng, bài học và loại hoạt động cụ thể.
     * @param request Dữ liệu yêu cầu cập nhật tiến độ.
     * @return ProgressResponse của bản ghi tiến độ đã được cập nhật/tạo mới.
     * @throws IllegalArgumentException nếu dữ liệu yêu cầu không hợp lệ hoặc không tìm thấy người dùng/bài học.
     */
    public ProgressResponse updateProgress(ProgressRequest request) {
        // Kiểm tra các trường bắt buộc
        if (request == null || request.userId() == null || request.lessonId() == null || request.activityType() == null || request.status() == null) {
            throw new IllegalArgumentException("Request, userId, lessonId, activityType, hoặc status không được để trống.");
        }
        // Kiểm tra phạm vi phần trăm hoàn thành
        if (request.completionPercentage() < 0 || request.completionPercentage() > 100) {
            throw new IllegalArgumentException("Tỷ lệ hoàn thành phải từ 0 đến 100.");
        }

        // Tìm kiếm người dùng và bài học theo ID
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        // Chuyển đổi chuỗi activityType sang enum
        Progress.ActivityType activityTypeEnum;
        try {
            activityTypeEnum = Progress.ActivityType.valueOf(request.activityType());
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Loại hoạt động không hợp lệ: " + request.activityType());
        }

        // Tìm kiếm bản ghi tiến độ hiện có cho người dùng, bài học và LOẠI HOẠT ĐỘNG cụ thể.
        // Đây là điểm khác biệt chính so với logic cũ.
        Optional<Progress> existingProgress = progressRepository.findByUserUserIdAndLessonLessonIdAndActivityType(
                request.userId(),
                request.lessonId(),
                activityTypeEnum
        );

        Progress progress = existingProgress.orElse(new Progress()); // Nếu không tìm thấy, tạo mới
        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setActivityType(activityTypeEnum); // Set loại hoạt động
        progress.setStatus(Progress.Status.valueOf(request.status()));
        progress.setCompletionPercentage(request.completionPercentage());
        // lastUpdated sẽ tự động được cập nhật bởi @PrePersist/@PreUpdate trong entity Progress

        progress = progressRepository.save(progress); // Lưu hoặc cập nhật bản ghi
        return mapToProgressResponse(progress); // Ánh xạ sang DTO trả về
    }

    /**
     * Lấy tiến độ của một loại hoạt động cụ thể trong một bài học của người dùng.
     * @param userId ID của người dùng.
     * @param lessonId ID của bài học.
     * @param activityType Loại hoạt động (ví dụ: "READING_MATERIAL").
     * @return ProgressResponse của tiến độ hoạt động.
     * @throws IllegalArgumentException nếu các ID hoặc loại hoạt động không hợp lệ, hoặc không tìm thấy tiến độ.
     */
    public ProgressResponse getProgressByActivity(Integer userId, Integer lessonId, String activityType) {
        if (userId == null || lessonId == null || activityType == null) {
            throw new IllegalArgumentException("User ID, Lesson ID và Activity Type không được để trống.");
        }

        Progress.ActivityType activityTypeEnum;
        try {
            activityTypeEnum = Progress.ActivityType.valueOf(activityType);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Loại hoạt động không hợp lệ: " + activityType);
        }

        Progress progress = progressRepository.findByUserUserIdAndLessonLessonIdAndActivityType(
                userId,
                lessonId,
                activityTypeEnum
        ).orElseThrow(() -> new IllegalArgumentException(
                "Không tìm thấy tiến độ cho user ID: " + userId + ", lesson ID: " + lessonId + " và activity type: " + activityType));

        return mapToProgressResponse(progress);
    }

    /**
     * Tính toán và trả về tổng phần trăm hoàn thành của một bài học cho một người dùng.
     * Tổng phần trăm được tính dựa trên trung bình phần trăm hoàn thành của TẤT CẢ các hoạt động
     * đã được ghi nhận trong bài học đó.
     * @param userId ID của người dùng.
     * @param lessonId ID của bài học.
     * @return ProgressResponse đại diện cho tổng tiến độ của bài học.
     * @throws IllegalArgumentException nếu User ID hoặc Lesson ID không được cung cấp.
     */
    public ProgressResponse getOverallLessonProgress(Integer userId, Integer lessonId) {
        if (userId == null || lessonId == null) {
            throw new IllegalArgumentException("User ID và Lesson ID không được để trống.");
        }

        // Lấy tất cả tiến độ của các hoạt động trong bài học đó cho người dùng này
        // (Sẽ cần phương thức findByUserUserIdAndLessonLessonId trong ProgressRepository)
        List<Progress> activitiesProgress = progressRepository.findByUserUserIdAndLessonLessonId(userId, lessonId);

        if (activitiesProgress.isEmpty()) {
            // Nếu không có hoạt động nào được ghi lại, coi như 0% hoàn thành và trạng thái NOT_STARTED
            return new ProgressResponse(null, userId, lessonId, "OVERALL_LESSON", Progress.Status.NOT_STARTED.toString(), 0, null);
        }

        // Tính tổng phần trăm hoàn thành từ tất cả các hoạt động
        int totalCompletion = 0;
        for (Progress progress : activitiesProgress) {
            totalCompletion += progress.getCompletionPercentage();
        }

        // Tính phần trăm trung bình (làm tròn xuống)
        int overallPercentage = totalCompletion / activitiesProgress.size();

        // Xác định trạng thái tổng thể của bài học
        Progress.Status overallStatus = determineOverallStatus(activitiesProgress);

        return new ProgressResponse(
                null, // progressId là null vì đây là bản ghi tổng hợp
                userId,
                lessonId,
                "OVERALL_LESSON", // Một loại hoạt động đặc biệt để biểu thị tổng thể bài học
                overallStatus.toString(),
                overallPercentage,
                null // lastUpdated có thể là null hoặc lấy từ bản ghi gần nhất nếu cần
        );
    }

    /**
     * Hàm trợ giúp để xác định trạng thái tổng thể của bài học dựa trên tiến độ của các hoạt động con.
     * - Nếu tất cả hoạt động đã hoàn thành -> COMPLETED.
     * - Nếu có ít nhất một hoạt động đang IN_PROGRESS -> IN_PROGRESS.
     * - Ngược lại (có bản ghi nhưng không có IN_PROGRESS và không phải tất cả COMPLETED) -> NOT_STARTED.
     * @param activitiesProgress Danh sách các bản ghi tiến độ hoạt động.
     * @return Trạng thái tổng thể của bài học.
     */
    private Progress.Status determineOverallStatus(List<Progress> activitiesProgress) {
        boolean anyInProgress = false;
        boolean allCompleted = true; // Ban đầu giả định tất cả đã hoàn thành

        if (activitiesProgress.isEmpty()) {
            return Progress.Status.NOT_STARTED; // Nếu không có hoạt động nào, coi như chưa bắt đầu
        }

        for (Progress progress : activitiesProgress) {
            if (progress.getStatus() == Progress.Status.IN_PROGRESS) {
                anyInProgress = true;
            }
            if (progress.getStatus() != Progress.Status.COMPLETED) {
                allCompleted = false; // Nếu có bất kỳ hoạt động nào chưa COMPLETED, thì allCompleted là false
            }
        }

        if (allCompleted) {
            return Progress.Status.COMPLETED;
        } else if (anyInProgress) {
            return Progress.Status.IN_PROGRESS;
        } else {
            // Nếu không có IN_PROGRESS và không phải tất cả COMPLETED,
            // có nghĩa là có một số hoạt động NOT_STARTED hoặc đã hoàn thành một phần nhưng chưa có ai IN_PROGRESS
            // Ví dụ: 1 hoạt động COMPLETED, 1 hoạt động NOT_STARTED => Overall là NOT_STARTED (hoặc IN_PROGRESS nếu bạn muốn)
            // Trong trường hợp này, ta sẽ trả về NOT_STARTED nếu không có bất kỳ IN_PROGRESS nào.
            return Progress.Status.NOT_STARTED;
        }
    }

    /**
     * Lấy tất cả các bản ghi tiến độ của một người dùng.
     * @param userId ID của người dùng.
     * @return Danh sách ProgressResponse của người dùng.
     * @throws IllegalArgumentException nếu User ID không được cung cấp.
     */
    public List<ProgressResponse> getProgressByUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        return progressRepository.findByUserUserId(userId).stream()
                .map(this::mapToProgressResponse)
                .collect(Collectors.toList());
    }

    /**
     * Ánh xạ một Progress entity sang ProgressResponse DTO.
     * @param progress Entity Progress.
     * @return ProgressResponse DTO.
     */
    private ProgressResponse mapToProgressResponse(Progress progress) {
        return new ProgressResponse(
                progress.getProgressId(),
                progress.getUser().getUserId(),
                progress.getLesson().getLessonId(),
                progress.getActivityType().toString(), // Sử dụng getActivityType()
                progress.getStatus().toString(),
                progress.getCompletionPercentage(),
                progress.getLastUpdated()
        );
    }
}
