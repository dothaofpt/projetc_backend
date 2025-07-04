package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.PracticeActivityRequest;
import org.example.projetc_backend.dto.PracticeActivityResponse;
import org.example.projetc_backend.dto.PracticeActivityPageResponse;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.PracticeActivity;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.PracticeActivityRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional // Đặt Transactional ở cấp độ class
public class PracticeActivityService {
    private static final Logger logger = LoggerFactory.getLogger(PracticeActivityService.class);
    private final PracticeActivityRepository practiceActivityRepository;
    private final LessonRepository lessonRepository;

    public PracticeActivityService(PracticeActivityRepository practiceActivityRepository, LessonRepository lessonRepository) {
        this.practiceActivityRepository = practiceActivityRepository;
        this.lessonRepository = lessonRepository;
    }

    /**
     * Tạo một hoạt động luyện tập mới.
     * @param request Dữ liệu yêu cầu để tạo hoạt động luyện tập.
     * @return PracticeActivityResponse của hoạt động đã tạo.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc không tìm thấy Lesson.
     */
    public PracticeActivityResponse createPracticeActivity(PracticeActivityRequest request) {
        if (request == null || request.lessonId() == null || request.title() == null ||
                request.skill() == null || request.activityType() == null) {
            throw new IllegalArgumentException("Lesson ID, tiêu đề, kỹ năng và loại hoạt động là bắt buộc.");
        }

        logger.info("Đang xử lý yêu cầu tạo PracticeActivity với tiêu đề: {}, kỹ năng: {}, loại: {}",
                request.title(), request.skill(), request.activityType());

        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        practiceActivityRepository.findByTitle(request.title().trim())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề hoạt động luyện tập '" + request.title() + "' đã tồn tại.");
                });

        PracticeActivity activity = new PracticeActivity();
        activity.setLesson(lesson);
        activity.setTitle(request.title().trim());
        activity.setDescription(request.description() != null ? request.description().trim() : null);
        activity.setSkill(request.skill());
        activity.setActivityType(request.activityType());
        activity.setMaterialUrl(request.materialUrl() != null ? request.materialUrl().trim() : null);
        activity.setTranscriptText(request.transcriptText() != null ? request.transcriptText().trim() : null);
        activity.setPromptText(request.promptText() != null ? request.promptText().trim() : null);
        activity.setExpectedOutputText(request.expectedOutputText() != null ? request.expectedOutputText().trim() : null);
        activity.setCreatedAt(LocalDateTime.now());

        activity = practiceActivityRepository.save(activity);
        return mapToPracticeActivityResponse(activity);
    }

    /**
     * Lấy thông tin hoạt động luyện tập theo ID.
     * @param activityId ID của hoạt động luyện tập.
     * @return PracticeActivityResponse chứa thông tin hoạt động.
     * @throws IllegalArgumentException nếu activityId trống hoặc không tìm thấy.
     */
    @Transactional(readOnly = true)
    public PracticeActivityResponse getPracticeActivityById(Integer activityId) {
        if (activityId == null) {
            throw new IllegalArgumentException("Practice Activity ID không được để trống.");
        }
        PracticeActivity activity = practiceActivityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + activityId));
        return mapToPracticeActivityResponse(activity);
    }

    /**
     * Lấy danh sách các hoạt động luyện tập theo ID bài học (Lesson ID).
     * @param lessonId ID của bài học.
     * @return Danh sách PracticeActivityResponse của các hoạt động thuộc bài học đó.
     * @throws IllegalArgumentException nếu lessonId trống hoặc không tìm thấy Lesson.
     */
    @Transactional(readOnly = true)
    public List<PracticeActivityResponse> getPracticeActivitiesByLessonId(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId);
        }
        return practiceActivityRepository.findByLessonLessonId(lessonId).stream()
                .map(this::mapToPracticeActivityResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả các hoạt động luyện tập hiện có trong hệ thống.
     * @return Danh sách PracticeActivityResponse của tất cả các hoạt động.
     */
    @Transactional(readOnly = true)
    public List<PracticeActivityResponse> getAllPracticeActivities() {
        logger.info("Đang lấy tất cả PracticeActivities.");
        return practiceActivityRepository.findAll().stream()
                .map(this::mapToPracticeActivityResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm và phân trang các hoạt động luyện tập.
     * @param lessonId ID bài học (tùy chọn).
     * @param title Tiêu đề (tùy chọn).
     * @param skill Kỹ năng (tùy chọn).
     * @param activityType Loại hoạt động (tùy chọn).
     * @param page Số trang.
     * @param size Kích thước trang.
     * @param sortBy Trường để sắp xếp.
     * @param sortDir Hướng sắp xếp (ASC/DESC).
     * @return Trang các PracticeActivityResponse.
     */
    @Transactional(readOnly = true)
    public PracticeActivityPageResponse searchPracticeActivities(
            Integer lessonId, String title, PracticeActivity.ActivitySkill skill,
            PracticeActivity.ActivityType activityType,
            int page, int size, String sortBy, String sortDir) {

        Sort sort = Sort.by(sortDir.equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(page, size, sort);

        Page<PracticeActivity> activityPage = practiceActivityRepository.searchPracticeActivities(
                lessonId, title, skill, activityType, pageable
        );

        List<PracticeActivityResponse> content = activityPage.getContent().stream()
                .map(this::mapToPracticeActivityResponse)
                .collect(Collectors.toList());

        return new PracticeActivityPageResponse(
                content,
                activityPage.getTotalElements(),
                activityPage.getTotalPages(),
                activityPage.getNumber(),
                activityPage.getSize()
        );
    }

    /**
     * Cập nhật thông tin của một hoạt động luyện tập hiện có.
     * @param activityId ID của hoạt động luyện tập cần cập nhật.
     * @param request Dữ liệu mới để cập nhật.
     * @return PracticeActivityResponse chứa thông tin hoạt động đã cập nhật.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ, không tìm thấy Activity/Lesson, hoặc tiêu đề trùng lặp.
     */
    public PracticeActivityResponse updatePracticeActivity(Integer activityId, PracticeActivityRequest request) {
        if (activityId == null || request == null || request.lessonId() == null || request.title() == null ||
                request.skill() == null || request.activityType() == null) {
            throw new IllegalArgumentException("Practice Activity ID, Lesson ID, tiêu đề, kỹ năng và loại hoạt động là bắt buộc.");
        }

        logger.info("Đang cập nhật PracticeActivity với ID: {}", activityId);

        PracticeActivity activity = practiceActivityRepository.findById(activityId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + activityId));

        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        practiceActivityRepository.findByTitle(request.title().trim())
                .filter(existing -> !existing.getActivityId().equals(activityId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề hoạt động luyện tập '" + request.title() + "' đã tồn tại.");
                });

        activity.setLesson(lesson);
        activity.setTitle(request.title().trim());
        activity.setDescription(request.description() != null ? request.description().trim() : null);
        activity.setSkill(request.skill());
        activity.setActivityType(request.activityType());
        activity.setMaterialUrl(request.materialUrl() != null ? request.materialUrl().trim() : null);
        activity.setTranscriptText(request.transcriptText() != null ? request.transcriptText().trim() : null);
        activity.setPromptText(request.promptText() != null ? request.promptText().trim() : null);
        activity.setExpectedOutputText(request.expectedOutputText() != null ? request.expectedOutputText().trim() : null);
        // Không cập nhật createdAt khi update

        activity = practiceActivityRepository.save(activity);
        return mapToPracticeActivityResponse(activity);
    }

    /**
     * Xóa một hoạt động luyện tập khỏi cơ sở dữ liệu.
     * @param activityId ID của hoạt động luyện tập cần xóa.
     * @throws IllegalArgumentException nếu activityId trống hoặc không tìm thấy.
     */
    public void deletePracticeActivity(Integer activityId) {
        if (activityId == null) {
            throw new IllegalArgumentException("Practice Activity ID không được để trống.");
        }
        if (!practiceActivityRepository.existsById(activityId)) {
            throw new IllegalArgumentException("Không tìm thấy hoạt động luyện tập với ID: " + activityId);
        }
        practiceActivityRepository.deleteById(activityId);
    }

    /**
     * Phương thức trợ giúp để ánh xạ đối tượng PracticeActivity entity sang PracticeActivityResponse DTO.
     * @param activity Đối tượng PracticeActivity entity.
     * @return Đối tượng PracticeActivityResponse DTO tương ứng.
     */
    private PracticeActivityResponse mapToPracticeActivityResponse(PracticeActivity activity) {
        return new PracticeActivityResponse(
                activity.getActivityId(),
                activity.getLesson() != null ? activity.getLesson().getLessonId() : null,
                activity.getTitle(),
                activity.getDescription(),
                activity.getSkill(),
                activity.getActivityType(),
                activity.getMaterialUrl(),
                activity.getTranscriptText(),
                activity.getPromptText(),
                activity.getExpectedOutputText(),
                activity.getCreatedAt()
        );
    }
}