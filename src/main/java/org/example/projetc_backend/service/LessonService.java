package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.LessonRequest;
import org.example.projetc_backend.dto.LessonResponse;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.repository.LessonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonService {

    private final LessonRepository lessonRepository;

    public LessonService(LessonRepository lessonRepository) {
        this.lessonRepository = lessonRepository;
    }

    public LessonResponse createLesson(LessonRequest request) {
        if (request == null || request.title() == null || request.level() == null || request.skill() == null) {
            throw new IllegalArgumentException("Request, title, level, hoặc skill không được để trống");
        }
        lessonRepository.findByTitle(request.title())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài học đã tồn tại: " + request.title());
                });
        Lesson lesson = new Lesson(
                request.title(),
                request.description(),
                request.level(),
                request.skill()
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
        return lessonRepository.findAll().stream()
                .map(this::mapToLessonResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse updateLesson(Integer lessonId, LessonRequest request) {
        if (lessonId == null || request == null || request.title() == null || request.level() == null || request.skill() == null) {
            throw new IllegalArgumentException("Lesson ID, request, title, level, hoặc skill không được để trống");
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
        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    public void deleteLesson(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống");
        }
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId);
        }
        lessonRepository.deleteById(lessonId);
    }

    private LessonResponse mapToLessonResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getLessonId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getLevel().toString(),
                lesson.getSkill().toString(),
                lesson.getCreatedAt()
        );
    }
}