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

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    private static final Map<Lesson.Level, Integer> LEVEL_DURATIONS = new HashMap<>();
    static {
        LEVEL_DURATIONS.put(Lesson.Level.BEGINNER, 6);
        LEVEL_DURATIONS.put(Lesson.Level.INTERMEDIATE, 8);
        LEVEL_DURATIONS.put(Lesson.Level.ADVANCED, 12);
    }

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public LessonResponse createLesson(LessonRequest request) {
        if (request == null || request.title() == null || request.level() == null || request.skill() == null || request.price() == null) {
            throw new IllegalArgumentException("Request, title, level, skill, hoặc price không được để trống");
        }
        lessonRepository.findByTitle(request.title())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài học đã tồn tại: " + request.title());
                });

        Lesson lesson = new Lesson(
                request.title(),
                request.description(),
                request.level(),
                request.skill(),
                request.price()
        );
        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    public LessonResponse getLessonById(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));
        return mapToLessonResponse(lesson);
    }

    public List<LessonResponse> getAllLessons() {
        return lessonRepository.findAllActive().stream()
                .map(this::mapToLessonResponse)
                .filter(response -> response != null)
                .collect(Collectors.toList());
    }

    public LessonResponse updateLesson(Integer lessonId, LessonRequest request) {
        if (lessonId == null || request == null || request.title() == null || request.level() == null || request.skill() == null || request.price() == null) {
            throw new IllegalArgumentException("Lesson ID, request, title, level, skill, hoặc price không được để trống");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));

        lessonRepository.findByTitle(request.title())
                .filter(existing -> !existing.getLessonId().equals(lessonId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài học đã tồn tại: " + request.title());
                });

        lesson.setTitle(request.title());
        lesson.setDescription(request.description() != null ? request.description() : lesson.getDescription());
        lesson.setLevel(request.level());
        lesson.setSkill(request.skill());
        lesson.setPrice(request.price());
        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    public void deleteLesson(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống");
        }
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));
        lesson.setDeleted(true);
        lessonRepository.save(lesson);
    }

    public LessonPageResponse searchLessons(LessonSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống");
        }

        String sortBy = request.sortBy();
        if (!List.of("lessonId", "title", "price").contains(sortBy)) {
            sortBy = "lessonId";
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Lesson.Level level = request.level() != null ? Lesson.Level.valueOf(request.level().toUpperCase()) : null;
        Lesson.Skill skill = request.skill() != null ? Lesson.Skill.valueOf(request.skill().toUpperCase()) : null;

        Page<Lesson> lessonPage = lessonRepository.searchLessons(
                request.title(),
                level,
                skill,
                request.minPrice(),
                request.maxPrice(),
                pageable
        );

        List<LessonResponse> content = lessonPage.getContent().stream()
                .map(this::mapToLessonResponse)
                .filter(response -> response != null)
                .collect(Collectors.toList());

        return new LessonPageResponse(
                content,
                lessonPage.getTotalElements(),
                lessonPage.getTotalPages(),
                lessonPage.getNumber(),
                lessonPage.getSize()
        );
    }

    public LessonResponse mapToLessonResponse(Lesson lesson) {
        if (lesson.isDeleted()) return null; // Bỏ qua bài học đã xóa
        Integer durationMonths = LEVEL_DURATIONS.getOrDefault(lesson.getLevel(), null);

        return new LessonResponse(
                lesson.getLessonId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getLevel().toString(),
                lesson.getSkill().toString(),
                lesson.getPrice(),
                lesson.getCreatedAt(),
                durationMonths
        );
    }
}