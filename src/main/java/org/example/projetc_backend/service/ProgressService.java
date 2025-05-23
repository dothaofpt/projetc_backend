package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.ProgressRequest;
import org.example.projetc_backend.dto.ProgressResponse;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.Progress;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.ProgressRepository;
import org.springframework.stereotype.Service;

import java.util.List;
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

    public ProgressResponse updateProgress(ProgressRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        Progress progress = progressRepository.findByUserUserIdAndLessonLessonId(request.userId(), request.lessonId())
                .orElse(new Progress());
        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setStatus(Progress.Status.valueOf(request.status()));
        progress = progressRepository.save(progress);
        return mapToProgressResponse(progress);
    }

    public ProgressResponse getProgressByUserAndLesson(Integer userId, Integer lessonId) {
        Progress progress = progressRepository.findByUserUserIdAndLessonLessonId(userId, lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Progress not found"));
        return mapToProgressResponse(progress);
    }

    public List<ProgressResponse> getProgressByUser(Integer userId) {
        return progressRepository.findByUserUserId(userId).stream()
                .map(this::mapToProgressResponse)
                .collect(Collectors.toList());
    }

    private ProgressResponse mapToProgressResponse(Progress progress) {
        return new ProgressResponse(
                progress.getProgressId(),
                progress.getUser().getUserId(),
                progress.getLesson().getLessonId(),
                progress.getStatus().toString(),
                progress.getLastUpdated().toString()
        );
    }
}