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
        Lesson lesson = new Lesson();
        lesson.setTitle(request.title());
        lesson.setDescription(request.description());
        lesson.setLevel(Lesson.Level.valueOf(request.level()));
        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    public LessonResponse getLessonById(Integer lessonId) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        return mapToLessonResponse(lesson);
    }

    public List<LessonResponse> getAllLessons() {
        return lessonRepository.findAll().stream()
                .map(this::mapToLessonResponse)
                .collect(Collectors.toList());
    }

    public LessonResponse updateLesson(Integer lessonId, LessonRequest request) {
        Lesson lesson = lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        lesson.setTitle(request.title());
        lesson.setDescription(request.description());
        lesson.setLevel(Lesson.Level.valueOf(request.level()));
        lesson = lessonRepository.save(lesson);
        return mapToLessonResponse(lesson);
    }

    public void deleteLesson(Integer lessonId) {
        lessonRepository.deleteById(lessonId);
    }

    private LessonResponse mapToLessonResponse(Lesson lesson) {
        return new LessonResponse(
                lesson.getLessonId(),
                lesson.getTitle(),
                lesson.getDescription(),
                lesson.getLevel().toString(),
                lesson.getCreatedAt().toString()
        );
    }
}