package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.QuizRequest;
import org.example.projetc_backend.dto.QuizResponse;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {
    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);
    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;

    public QuizService(QuizRepository quizRepository, LessonRepository lessonRepository) {
        this.quizRepository = quizRepository;
        this.lessonRepository = lessonRepository;
    }

    public QuizResponse createQuiz(QuizRequest request) {
        if (request == null || request.lessonId() == null || request.title() == null || request.skill() == null) {
            throw new IllegalArgumentException("Request, lessonId, title, hoặc skill không được để trống");
        }

        logger.info("Processing QuizRequest with skill: {}", request.skill());
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));
        quizRepository.findByTitle(request.title())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài kiểm tra đã tồn tại: " + request.title());
                });

        Quiz quiz = new Quiz();
        quiz.setLesson(lesson);
        quiz.setTitle(request.title());
        try {
            quiz.setSkill(Quiz.Skill.valueOf(request.skill()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Skill không hợp lệ: " + request.skill());
        }
        quiz = quizRepository.save(quiz);
        return mapToQuizResponse(quiz);
    }

    public QuizResponse getQuizById(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống");
        }
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId));
        return mapToQuizResponse(quiz);
    }

    public List<QuizResponse> getQuizzesByLessonId(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống");
        }
        return quizRepository.findByLessonLessonId(lessonId).stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    public QuizResponse updateQuiz(Integer quizId, QuizRequest request) {
        if (quizId == null || request == null || request.lessonId() == null || request.title() == null || request.skill() == null) {
            throw new IllegalArgumentException("Quiz ID, request, lessonId, title, hoặc skill không được để trống");
        }

        logger.info("Updating Quiz with ID: {}, skill: {}", quizId, request.skill());
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId));
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));
        quizRepository.findByTitle(request.title())
                .filter(existing -> !existing.getQuizId().equals(quizId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài kiểm tra đã tồn tại: " + request.title());
                });

        quiz.setLesson(lesson);
        quiz.setTitle(request.title());
        try {
            quiz.setSkill(Quiz.Skill.valueOf(request.skill()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Skill không hợp lệ: " + request.skill());
        }
        quiz = quizRepository.save(quiz);
        return mapToQuizResponse(quiz);
    }

    public void deleteQuiz(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống");
        }
        if (!quizRepository.existsById(quizId)) {
            throw new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId);
        }
        quizRepository.deleteById(quizId);
    }

    private QuizResponse mapToQuizResponse(Quiz quiz) {
        return new QuizResponse(
                quiz.getQuizId(),
                quiz.getLesson().getLessonId(),
                quiz.getTitle(),
                quiz.getSkill().toString(),
                quiz.getCreatedAt()
        );
    }
}