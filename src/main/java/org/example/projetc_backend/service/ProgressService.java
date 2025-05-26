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
        if (request == null || request.userId() == null || request.lessonId() == null || request.skill() == null || request.status() == null) {
            throw new IllegalArgumentException("Request, userId, lessonId, skill, hoặc status không được để trống");
        }
        if (request.completionPercentage() < 0 || request.completionPercentage() > 100) {
            throw new IllegalArgumentException("Tỷ lệ hoàn thành phải từ 0 đến 100");
        }
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));
        Progress progress = progressRepository.findByUserUserIdAndLessonLessonId(request.userId(), request.lessonId())
                .orElse(new Progress());
        progress.setUser(user);
        progress.setLesson(lesson);
        progress.setSkill(Progress.Skill.valueOf(request.skill()));
        progress.setStatus(Progress.Status.valueOf(request.status()));
        progress.setCompletionPercentage(request.completionPercentage());
        progress = progressRepository.save(progress);
        return mapToProgressResponse(progress);
    }

    public ProgressResponse getProgressByUserAndLesson(Integer userId, Integer lessonId) {
        if (userId == null || lessonId == null) {
            throw new IllegalArgumentException("User ID và Lesson ID không được để trống");
        }
        Progress progress = progressRepository.findByUserUserIdAndLessonLessonId(userId, lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tiến độ cho user ID: " + userId + " và lesson ID: " + lessonId));
        return mapToProgressResponse(progress);
    }

    public List<ProgressResponse> getProgressByUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống");
        }
        return progressRepository.findByUserUserId(userId).stream()
                .map(this::mapToProgressResponse)
                .collect(Collectors.toList());
    }

    private ProgressResponse mapToProgressResponse(Progress progress) {
        return new ProgressResponse(
                progress.getProgressId(),
                progress.getUser().getUserId(),
                progress.getLesson().getLessonId(),
                progress.getSkill().toString(),
                progress.getStatus().toString(),
                progress.getCompletionPercentage(),
                progress.getLastUpdated()
        );
    }
}