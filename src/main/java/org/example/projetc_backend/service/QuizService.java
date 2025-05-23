package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.QuizRequest;
import org.example.projetc_backend.dto.QuizResponse;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.QuizRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizService {

    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;

    public QuizService(QuizRepository quizRepository, LessonRepository lessonRepository) {
        this.quizRepository = quizRepository;
        this.lessonRepository = lessonRepository;
    }

    public QuizResponse createQuiz(QuizRequest request) {
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        Quiz quiz = new Quiz();
        quiz.setLesson(lesson);
        quiz.setTitle(request.title());
        quiz = quizRepository.save(quiz);
        return mapToQuizResponse(quiz);
    }

    public QuizResponse getQuizById(Integer quizId) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        return mapToQuizResponse(quiz);
    }

    public List<QuizResponse> getQuizzesByLessonId(Integer lessonId) {
        return quizRepository.findByLessonLessonId(lessonId).stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    public QuizResponse updateQuiz(Integer quizId, QuizRequest request) {
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found"));
        quiz.setLesson(lesson);
        quiz.setTitle(request.title());
        quiz = quizRepository.save(quiz);
        return mapToQuizResponse(quiz);
    }

    public void deleteQuiz(Integer quizId) {
        quizRepository.deleteById(quizId);
    }

    private QuizResponse mapToQuizResponse(Quiz quiz) {
        return new QuizResponse(
                quiz.getQuizId(),
                quiz.getLesson().getLessonId(),
                quiz.getTitle(),
                quiz.getCreatedAt().toString()
        );
    }
}