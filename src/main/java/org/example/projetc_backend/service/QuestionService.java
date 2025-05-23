package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.QuestionRequest;
import org.example.projetc_backend.dto.QuestionResponse;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.repository.QuizRepository;
import org.example.projetc_backend.repository.QuestionRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {

    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public QuestionResponse createQuestion(QuestionRequest request) {
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        Question question = new Question();
        question.setQuiz(quiz);
        question.setQuestionText(request.questionText());
        question.setType(Question.QuestionType.valueOf(request.type()));
        question = questionRepository.save(question);
        return mapToQuestionResponse(question);
    }

    public QuestionResponse getQuestionById(Integer questionId) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        return mapToQuestionResponse(question);
    }

    public List<QuestionResponse> getQuestionsByQuizId(Integer quizId) {
        return questionRepository.findByQuizQuizId(quizId).stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    public QuestionResponse updateQuestion(Integer questionId, QuestionRequest request) {
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Quiz not found"));
        question.setQuiz(quiz);
        question.setQuestionText(request.questionText());
        question.setType(Question.QuestionType.valueOf(request.type()));
        question = questionRepository.save(question);
        return mapToQuestionResponse(question);
    }

    public void deleteQuestion(Integer questionId) {
        questionRepository.deleteById(questionId);
    }

    private QuestionResponse mapToQuestionResponse(Question question) {
        return new QuestionResponse(
                question.getQuestionId(),
                question.getQuiz().getQuizId(),
                question.getQuestionText(),
                question.getType().toString()
        );
    }
}