package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.LessonRequest;
import org.example.projetc_backend.dto.LessonResponse;
import org.example.projetc_backend.dto.LessonSearchRequest;
import org.example.projetc_backend.dto.LessonPageResponse;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.repository.LessonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    // Map này không được sử dụng trong các phương thức hiện có, nhưng có thể hữu ích cho tương lai.
    private static final Map<Lesson.Level, Integer> LEVEL_DURATIONS = new HashMap<>();
    static {
        LEVEL_DURATIONS.put(Lesson.Level.BEGINNER, 6); // 6 tháng
        LEVEL_DURATIONS.put(Lesson.Level.INTERMEDIATE, 8); // 8 tháng
        LEVEL_DURATIONS.put(Lesson.Level.ADVANCED, 12); // 12 tháng
    }

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    /**
     * Tạo một bài học mới.
     *
     * @param request DTO chứa thông tin bài học.
     * @return LessonResponse của bài học đã tạo.
     * @throws IllegalArgumentException Nếu dữ liệu request không hợp lệ hoặc tiêu đề đã tồn tại.
     */
    @Transactional
    public LessonResponse createLesson(LessonRequest request) {
        if (request == null || request.title() == null || request.title().trim().isEmpty() ||
                request.level() == null || request.skill() == null || request.price() == null) {
            throw new IllegalArgumentException("Tiêu đề, cấp độ, kỹ năng và giá của bài học là bắt buộc.");
        }

        Optional<Lesson> existingLesson = lessonRepository.findByTitle(request.title().trim());
        if (existingLesson.isPresent()) {
            throw new IllegalArgumentException("Bài học với tiêu đề '" + request.title() + "' đã tồn tại.");
        }

        Lesson lesson = new Lesson();
        lesson.setTitle(request.title().trim());
        lesson.setDescription(request.description() != null ? request.description().trim() : null);
        lesson.setLevel(request.level());
        lesson.setSkill(request.skill());
        lesson.setPrice(request.price());
        lesson.setCreatedAt(LocalDateTime.now());
        lesson.setDeleted(false); // Mặc định không bị xóa

        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    /**
     * Lấy thông tin chi tiết của một bài học theo ID.
     *
     * @param lessonId ID của bài học.
     * @return LessonResponse của bài học.
     * @throws IllegalArgumentException Nếu Lesson ID trống hoặc không tìm thấy bài học.
     */
    @Transactional(readOnly = true)
    public LessonResponse getLessonById(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));
        return mapToLessonResponse(lesson);
    }

    /**
     * Lấy tất cả các bài học đang hoạt động (chưa bị xóa mềm).
     *
     * @return Danh sách LessonResponse của các bài học đang hoạt động.
     */
    @Transactional(readOnly = true)
    public List<LessonResponse> getAllActiveLessons() {
        return lessonRepository.findByIsDeletedFalse().stream()
                .map(this::mapToLessonResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin của một bài học hiện có.
     *
     * @param lessonId ID của bài học cần cập nhật.
     * @param request DTO chứa thông tin cập nhật.
     * @return LessonResponse của bài học đã cập nhật.
     * @throws IllegalArgumentException Nếu dữ liệu request không hợp lệ, bài học không tồn tại, hoặc tiêu đề đã trùng.
     */
    @Transactional
    public LessonResponse updateLesson(Integer lessonId, LessonRequest request) {
        if (lessonId == null || request == null || request.title() == null || request.title().trim().isEmpty() ||
                request.level() == null || request.skill() == null || request.price() == null) {
            throw new IllegalArgumentException("Lesson ID, tiêu đề, cấp độ, kỹ năng và giá của bài học là bắt buộc.");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));

        // Kiểm tra tiêu đề trùng lặp, nhưng loại trừ chính bài học đang cập nhật
        lessonRepository.findByTitle(request.title().trim())
                .filter(existing -> !existing.getLessonId().equals(lessonId)) // Loại trừ bài học hiện tại
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Bài học với tiêu đề '" + request.title() + "' đã tồn tại.");
                });

        lesson.setTitle(request.title().trim());
        lesson.setDescription(request.description() != null ? request.description().trim() : null);
        lesson.setLevel(request.level());
        lesson.setSkill(request.skill());
        lesson.setPrice(request.price());

        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    /**
     * Xóa mềm một bài học (đánh dấu là đã xóa).
     *
     * @param lessonId ID của bài học cần xóa mềm.
     * @throws IllegalArgumentException Nếu Lesson ID trống, không tìm thấy bài học, hoặc bài học đã bị xóa mềm.
     */
    @Transactional
    public void softDeleteLesson(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));

        if (lesson.isDeleted()) {
            throw new IllegalArgumentException("Bài học đã bị xóa mềm trước đó.");
        }

        lesson.setDeleted(true);
        lessonRepository.save(lesson);
    }

    /**
     * Khôi phục một bài học đã bị xóa mềm.
     *
     * @param lessonId ID của bài học cần khôi phục.
     * @return LessonResponse của bài học đã khôi phục.
     * @throws IllegalArgumentException Nếu Lesson ID trống, không tìm thấy bài học, hoặc bài học chưa bị xóa mềm.
     */
    @Transactional
    public LessonResponse restoreLesson(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));

        if (!lesson.isDeleted()) {
            throw new IllegalArgumentException("Bài học không bị xóa mềm, không cần khôi phục.");
        }

        lesson.setDeleted(false);
        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    /**
     * Tìm kiếm và phân trang các bài học.
     *
     * @param request DTO chứa các tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return Trang (Page) các LessonResponse.
     * @throws IllegalArgumentException Nếu Search request trống.
     */
    @Transactional(readOnly = true)
    public LessonPageResponse searchLessons(LessonSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        // Đặt giá trị mặc định cho sortBy và sortDir nếu không hợp lệ
        String sortBy = "lessonId"; // Mặc định sắp xếp theo lessonId
        if (request.sortBy() != null && List.of("lessonId", "title", "price", "level", "skill", "createdAt").contains(request.sortBy())) {
            sortBy = request.sortBy();
        }

        Sort.Direction sortDir = Sort.Direction.ASC; // Mặc định sắp xếp tăng dần
        if (request.sortDir() != null && request.sortDir().equalsIgnoreCase("DESC")) {
            sortDir = Sort.Direction.DESC;
        }
        Sort sort = Sort.by(sortDir, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Lesson.Level levelEnum = null;
        if (request.level() != null && !request.level().isBlank()) {
            try {
                levelEnum = Lesson.Level.valueOf(request.level().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Log lỗi nhưng không ném exception để cho phép tìm kiếm với các tiêu chí khác
                System.err.println("Giá trị Level không hợp lệ trong yêu cầu tìm kiếm Lesson: " + request.level() + " - " + e.getMessage());
            }
        }

        Lesson.Skill skillEnum = null;
        if (request.skill() != null && !request.skill().isBlank()) {
            try {
                skillEnum = Lesson.Skill.valueOf(request.skill().toUpperCase());
            } catch (IllegalArgumentException e) {
                // Log lỗi nhưng không ném exception
                System.err.println("Giá trị Skill không hợp lệ trong yêu cầu tìm kiếm Lesson: " + request.skill() + " - " + e.getMessage());
            }
        }

        Page<Lesson> lessonPage = lessonRepository.searchLessons(
                request.title(),
                levelEnum,
                skillEnum,
                request.minPrice(),
                request.maxPrice(),
                pageable
        );

        List<LessonResponse> content = lessonPage.getContent().stream()
                .map(this::mapToLessonResponse) // Sử dụng phương thức public
                .collect(Collectors.toList());

        return new LessonPageResponse(
                content,
                lessonPage.getTotalElements(),
                lessonPage.getTotalPages(),
                lessonPage.getNumber(),
                lessonPage.getSize()
        );
    }


    /**
     * Phương thức ánh xạ từ Lesson Entity sang LessonResponse DTO.
     * **QUAN TRỌNG: Đã thay đổi từ private thành public.**
     *
     * @param lesson Entity Lesson.
     * @return LessonResponse DTO.
     */
    public LessonResponse mapToLessonResponse(Lesson lesson) { // Đã thay đổi thành public
        if (lesson == null) {
            return null;
        }

        return new LessonResponse(
                lesson.getLessonId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getLevel(),
                lesson.getSkill(),
                lesson.getPrice(),
                lesson.getCreatedAt(),
                lesson.isDeleted()
        );
    }
}