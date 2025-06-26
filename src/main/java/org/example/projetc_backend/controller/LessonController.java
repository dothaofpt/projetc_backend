package org.example.projetc_backend.controller;

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

    private static final Map<Lesson.Level, Integer> LEVEL_DURATIONS = new HashMap<>();
    static {
        LEVEL_DURATIONS.put(Lesson.Level.BEGINNER, 6); // 6 tháng
        LEVEL_DURATIONS.put(Lesson.Level.INTERMEDIATE, 8); // 8 tháng
        LEVEL_DURATIONS.put(Lesson.Level.ADVANCED, 12); // 12 tháng
    }

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

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
        lesson.setDeleted(false);

        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    @Transactional(readOnly = true)
    public LessonResponse getLessonById(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));
        return mapToLessonResponse(lesson);
    }

    @Transactional(readOnly = true)
    public List<LessonResponse> getAllActiveLessons() {
        return lessonRepository.findByIsDeletedFalse().stream()
                .map(this::mapToLessonResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public LessonResponse updateLesson(Integer lessonId, LessonRequest request) {
        if (lessonId == null || request == null || request.title() == null || request.title().trim().isEmpty() ||
                request.level() == null || request.skill() == null || request.price() == null) {
            throw new IllegalArgumentException("Lesson ID, tiêu đề, cấp độ, kỹ năng và giá của bài học là bắt buộc.");
        }

        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));

        lessonRepository.findByTitle(request.title().trim())
                .filter(existing -> !existing.getLessonId().equals(lessonId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài học đã tồn tại: " + request.title());
                });

        lesson.setTitle(request.title().trim());
        lesson.setDescription(request.description() != null ? request.description().trim() : null);
        lesson.setLevel(request.level());
        lesson.setSkill(request.skill());
        lesson.setPrice(request.price());

        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

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

    // FIX: Change return type from void to LessonResponse
    @Transactional
    public LessonResponse restoreLesson(Integer lessonId) { // Changed return type
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));

        if (!lesson.isDeleted()) {
            throw new IllegalArgumentException("Bài học không bị xóa mềm, không cần khôi phục.");
        }

        lesson.setDeleted(false);
        lesson = lessonRepository.save(lesson); // Ensure the saved lesson is used
        return mapToLessonResponse(lesson); // Return the mapped response
    }

    @Transactional(readOnly = true)
    public LessonPageResponse searchLessons(LessonSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        String sortBy = request.sortBy();
        if (!List.of("lessonId", "title", "price", "level", "skill", "createdAt").contains(sortBy)) {
            sortBy = "lessonId";
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Lesson.Level levelEnum = null;
        if (request.level() != null && !request.level().isBlank()) {
            try {
                levelEnum = Lesson.Level.valueOf(request.level().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid Lesson Level in search request: " + request.level());
            }
        }

        Lesson.Skill skillEnum = null;
        if (request.skill() != null && !request.skill().isBlank()) {
            try {
                skillEnum = Lesson.Skill.valueOf(request.skill().toUpperCase());
            } catch (IllegalArgumentException e) {
                System.err.println("Invalid Lesson Skill in search request: " + request.skill());
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
                .map(this::mapToLessonResponse)
                .collect(Collectors.toList());

        return new LessonPageResponse(
                content,
                lessonPage.getTotalElements(),
                lessonPage.getTotalPages(),
                lessonPage.getNumber(),
                lessonPage.getSize()
        );
    }

    private LessonResponse mapToLessonResponse(Lesson lesson) {
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