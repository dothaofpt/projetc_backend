package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.QuizResultRequest;
import org.example.projetc_backend.dto.QuizResultResponse;
import org.example.projetc_backend.service.QuizResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-results")
@CrossOrigin(origins = "*")
public class QuizResultController {

    private final QuizResultService quizResultService;

    public QuizResultController(QuizResultService quizResultService) {
        this.quizResultService = quizResultService;
    }

    @PostMapping
    public ResponseEntity<QuizResultResponse> saveQuizResult(@RequestBody QuizResultRequest request) {
        QuizResultResponse response = quizResultService.saveQuizResult(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/quiz/{quizId}")
    public ResponseEntity<QuizResultResponse> getQuizResultByUserAndQuiz(@PathVariable Integer userId,
                                                                         @PathVariable Integer quizId) {
        QuizResultResponse response = quizResultService.getQuizResultByUserAndQuiz(userId, quizId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<QuizResultResponse>> getQuizResultsByUser(@PathVariable Integer userId) {
        List<QuizResultResponse> responses = quizResultService.getQuizResultsByUser(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuizResultResponse>> getQuizResultsByQuiz(@PathVariable Integer quizId) {
        List<QuizResultResponse> responses = quizResultService.findQuizResultsByQuiz(quizId);
        return ResponseEntity.ok(responses);
    }
}
