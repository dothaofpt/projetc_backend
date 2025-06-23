package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.QuestionRequest;
import org.example.projetc_backend.dto.QuestionResponse;
import org.example.projetc_backend.service.QuestionService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = "*")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    public ResponseEntity<QuestionResponse> createQuestion(@RequestBody QuestionRequest request) {
        QuestionResponse response = questionService.createQuestion(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Integer questionId) {
        QuestionResponse response = questionService.getQuestionById(questionId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByQuizId(@PathVariable Integer quizId) {
        List<QuestionResponse> responses = questionService.getQuestionsByQuizId(quizId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint tìm kiếm câu hỏi.
     * Các tham số quizId, skill, questionText đều là tùy chọn.
     * Ví dụ:
     * - GET /api/questions/search?questionText=hello
     * - GET /api/questions/search?quizId=1&skill=READING
     * - GET /api/questions/search?questionText=grammar&skill=GRAMMAR&quizId=2
     */
    @GetMapping("/search")
    public ResponseEntity<List<QuestionResponse>> searchQuestions(
            @RequestParam(required = false) Integer quizId,
            @RequestParam(required = false) String skill,
            @RequestParam(required = false) String questionText) {
        List<QuestionResponse> responses = questionService.searchQuestions(quizId, skill, questionText);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Integer questionId,
                                                           @RequestBody QuestionRequest request) {
        QuestionResponse response = questionService.updateQuestion(questionId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{questionId}")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Integer questionId) {
        questionService.deleteQuestion(questionId);
        return ResponseEntity.noContent().build();
    }
}