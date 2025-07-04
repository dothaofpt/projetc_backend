package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.ProgressRequest;
import org.example.projetc_backend.dto.ProgressResponse;
import org.example.projetc_backend.dto.ProgressSearchRequest;
import org.example.projetc_backend.dto.ProgressPageResponse;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.Progress;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.ProgressRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
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
     * Tạo mới một bản ghi tiến độ.
     * @param request DTO chứa thông tin để tạo tiến độ mới.
     * @return ProgressResponse của bản ghi đã tạo.
     * @throws IllegalArgumentException Nếu dữ liệu không hợp lệ hoặc tiến độ đã tồn tại cho cặp user-lesson-activity.
     */
    @Transactional
    public ProgressResponse createProgress(ProgressRequest request) {
        // Validation cơ bản
        if (request == null || request.userId() == null || request.lessonId() == null || request.activityType() == null || request.status() == null) {
            throw new IllegalArgumentException("Request, userId, lessonId, activityType, hoặc status không được để trống.");
        }
        if (request.completionPercentage() < 0 || request.completionPercentage() > 100) {
            throw new IllegalArgumentException("Tỷ lệ hoàn thành phải từ 0 đến 100.");
        }

        // Kiểm tra xem đã tồn tại bản ghi cho cặp user-lesson-activity này chưa
        Optional<Progress> existingProgress = progressRepository.findByUserUserIdAndLessonLessonIdAndActivityType(
                request.userId(),
                request.lessonId(),
                request.activityType()
        );

        if (existingProgress.isPresent()) {
            throw new IllegalArgumentException("Tiến độ cho người dùng này, bài học này và loại hoạt động này đã tồn tại. Vui lòng sử dụng cập nhật.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        Progress progress = new Progress(); // Tạo một đối tượng Progress MỚI
        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setActivityType(request.activityType());
        progress.setStatus(request.status());
        progress.setCompletionPercentage(request.completionPercentage());
        progress.setLastUpdated(LocalDateTime.now()); // Hoặc để JPA tự động xử lý (nếu bạn có @PreUpdate)

        progress = progressRepository.save(progress);
        return mapToProgressResponse(progress);
    }

    /**
     * Cập nhật một bản ghi tiến độ hiện có.
     * @param progressId ID của tiến độ cần cập nhật.
     * @param request DTO chứa thông tin để cập nhật tiến độ.
     * @return ProgressResponse của bản ghi đã cập nhật.
     * @throws IllegalArgumentException Nếu ID không tồn tại, dữ liệu không hợp lệ, hoặc cố gắng thay đổi các khóa chính.
     */
    @Transactional
    public ProgressResponse updateProgress(Integer progressId, ProgressRequest request) {
        if (progressId == null) {
            throw new IllegalArgumentException("Progress ID không được để trống để cập nhật.");
        }
        if (request == null || request.userId() == null || request.lessonId() == null || request.activityType() == null || request.status() == null) {
            throw new IllegalArgumentException("Request, userId, lessonId, activityType, hoặc status không được để trống.");
        }
        if (request.completionPercentage() < 0 || request.completionPercentage() > 100) {
            throw new IllegalArgumentException("Tỷ lệ hoàn thành phải từ 0 đến 100.");
        }

        Progress progress = progressRepository.findById(progressId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tiến độ với ID: " + progressId));

        // Tùy chọn: Kiểm tra xem userId, lessonId, activityType có bị thay đổi không.
        // Thường thì các trường này không nên thay đổi khi cập nhật.
        if (!progress.getUser().getUserId().equals(request.userId()) ||
                !progress.getLesson().getLessonId().equals(request.lessonId()) ||
                !progress.getActivityType().equals(request.activityType())) {
            throw new IllegalArgumentException("Không thể thay đổi userId, lessonId hoặc activityType khi cập nhật tiến độ.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setActivityType(request.activityType()); // Vẫn cập nhật để đảm bảo tính đồng bộ, mặc dù đã kiểm tra không đổi
        progress.setStatus(request.status());
        progress.setCompletionPercentage(request.completionPercentage());
        progress.setLastUpdated(LocalDateTime.now()); // Cập nhật thời gian

        progress = progressRepository.save(progress);
        return mapToProgressResponse(progress);
    }

    @Transactional(readOnly = true)
    public ProgressResponse getProgressByActivity(Integer userId, Integer lessonId, String activityType) {
        if (userId == null || lessonId == null || activityType == null || activityType.trim().isEmpty()) {
            throw new IllegalArgumentException("User ID, Lesson ID và Activity Type không được để trống.");
        }

        Progress.ActivityType activityTypeEnum;
        try {
            activityTypeEnum = Progress.ActivityType.valueOf(activityType.toUpperCase());
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

    @Transactional(readOnly = true)
    public ProgressResponse getOverallLessonProgress(Integer userId, Integer lessonId) {
        if (userId == null || lessonId == null) {
            throw new IllegalArgumentException("User ID và Lesson ID không được để trống.");
        }

        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
        }
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId);
        }

        List<Progress> activitiesProgress = progressRepository.findByUserUserIdAndLessonLessonId(userId, lessonId);

        if (activitiesProgress.isEmpty()) {
            return new ProgressResponse(null, userId, lessonId, Progress.ActivityType.READING_MATERIAL, Progress.Status.NOT_STARTED, 0, null);
        }

        double totalCompletion = activitiesProgress.stream()
                .mapToInt(Progress::getCompletionPercentage)
                .average()
                .orElse(0.0);

        int overallPercentage = (int) Math.round(totalCompletion);

        Progress.Status overallStatus = determineOverallStatus(activitiesProgress);

        return new ProgressResponse(
                null,
                userId,
                lessonId,
                Progress.ActivityType.READING_MATERIAL,
                overallStatus,
                overallPercentage,
                null
        );
    }

    private Progress.Status determineOverallStatus(List<Progress> activitiesProgress) {
        if (activitiesProgress == null || activitiesProgress.isEmpty()) {
            return Progress.Status.NOT_STARTED;
        }

        boolean allCompleted = activitiesProgress.stream()
                .allMatch(p -> p.getStatus() == Progress.Status.COMPLETED);
        if (allCompleted) {
            return Progress.Status.COMPLETED;
        }

        boolean anyInProgress = activitiesProgress.stream()
                .anyMatch(p -> p.getStatus() == Progress.Status.IN_PROGRESS);
        if (anyInProgress) {
            return Progress.Status.IN_PROGRESS;
        }

        boolean anyActivityStarted = activitiesProgress.stream()
                .anyMatch(p -> p.getStatus() != Progress.Status.NOT_STARTED);
        if (anyActivityStarted) {
            return Progress.Status.IN_PROGRESS;
        }

        return Progress.Status.NOT_STARTED;
    }

    @Transactional(readOnly = true)
    public List<ProgressResponse> getProgressByUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
        }

        return progressRepository.findByUserUserId(userId).stream()
                .map(this::mapToProgressResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deleteProgress(Integer progressId) {
        if (progressId == null) {
            throw new IllegalArgumentException("Progress ID không được để trống.");
        }
        if (!progressRepository.existsById(progressId)) {
            throw new IllegalArgumentException("Không tìm thấy tiến độ với ID: " + progressId);
        }
        progressRepository.deleteById(progressId);
    }

    /**
     * Tìm kiếm và phân trang các bản ghi tiến độ dựa trên các tiêu chí tùy chọn.
     * @param request DTO chứa các tiêu chí tìm kiếm (userId, lessonId, activityType, status, minCompletionPercentage, maxCompletionPercentage) và thông tin phân trang/sắp xếp.
     * @return Trang các ProgressResponse phù hợp với tiêu chí tìm kiếm.
     * @throws IllegalArgumentException Nếu Search request trống.
     */
    @Transactional(readOnly = true)
    public ProgressPageResponse searchProgress(ProgressSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, request.sortBy());
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<Progress> progressPage = progressRepository.searchProgress(
                request.userId(),
                request.lessonId(),
                request.activityType(),
                request.status(),
                request.minCompletionPercentage(),
                request.maxCompletionPercentage(),
                pageable
        );

        List<ProgressResponse> content = progressPage.getContent().stream()
                .map(this::mapToProgressResponse)
                .collect(Collectors.toList());

        return new ProgressPageResponse(
                content,
                progressPage.getTotalElements(),
                progressPage.getTotalPages(),
                progressPage.getNumber(),
                progressPage.getSize()
        );
    }

    private ProgressResponse mapToProgressResponse(Progress progress) {
        return new ProgressResponse(
                progress.getProgressId(),
                progress.getUser() != null ? progress.getUser().getUserId() : null,
                progress.getLesson() != null ? progress.getLesson().getLessonId() : null,
                progress.getActivityType(),
                progress.getStatus(),
                progress.getCompletionPercentage(),
                progress.getLastUpdated()
        );
    }
}