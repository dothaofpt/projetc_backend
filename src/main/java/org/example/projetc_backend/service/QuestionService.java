package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.QuestionRequest;
import org.example.projetc_backend.dto.QuestionResponse;
import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.repository.QuestionRepository;
import org.example.projetc_backend.repository.QuizRepository;
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
        if (request == null || request.quizId() == null || request.questionText() == null || request.type() == null) {
            throw new IllegalArgumentException("Request, quizId, questionText, hoặc type không được để trống");
        }
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + request.quizId()));
        Question question = new Question();
        question.setQuiz(quiz);
        question.setQuestionText(request.questionText());
        question.setType(Question.QuestionType.valueOf(request.type()));
        question = questionRepository.save(question);
        return mapToQuestionResponse(question);
    }

    public QuestionResponse getQuestionById(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống");
        }
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId));
        return mapToQuestionResponse(question);
    }

    public List<QuestionResponse> getQuestionsByQuizId(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống");
        }
        return questionRepository.findByQuizQuizId(quizId).stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    public QuestionResponse updateQuestion(Integer questionId, QuestionRequest request) {
        if (questionId == null || request == null || request.quizId() == null || request.questionText() == null || request.type() == null) {
            throw new IllegalArgumentException("Question ID, request, quizId, questionText, hoặc type không được để trống");
        }
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId));
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + request.quizId()));
        question.setQuiz(quiz);
        question.setQuestionText(request.questionText());
        question.setType(Question.QuestionType.valueOf(request.type()));
        question = questionRepository.save(question);
        return mapToQuestionResponse(question);
    }

    public void deleteQuestion(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống");
        }
        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId);
        }
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