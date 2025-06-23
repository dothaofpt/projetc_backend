package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.AnswerRequest;
import org.example.projetc_backend.dto.AnswerResponse;
import org.example.projetc_backend.dto.AnswerSearchRequest;
import org.example.projetc_backend.service.AnswerService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@CrossOrigin(origins = "*")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping
    public	ResponseEntity<AnswerResponse> createAnswer(@RequestBody AnswerRequest request) {
        AnswerResponse response = answerService.createAnswer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{answerId}")
    public ResponseEntity<AnswerResponse> getAnswerById(@PathVariable Integer answerId) {
        AnswerResponse response = answerService.getAnswerById(answerId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerResponse>> getAnswersByQuestionId(@PathVariable Integer questionId) {
        List<AnswerResponse> responses = answerService.getAnswersByQuestionId(questionId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/question/{questionId}/all")
    public ResponseEntity<List<AnswerResponse>> getAllAnswersForAdminByQuestionId(@PathVariable Integer questionId) {
        List<AnswerResponse> responses = answerService.getAllAnswersForAdminByQuestionId(questionId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{answerId}")
    public ResponseEntity<AnswerResponse> updateAnswer(@PathVariable Integer answerId, @RequestBody AnswerRequest request) {
        AnswerResponse response = answerService.updateAnswer(answerId, request);
        return ResponseEntity.ok(response);
    }

    @PatchMapping("/{answerId}/status")
    public ResponseEntity<AnswerResponse> toggleAnswerStatus(@PathVariable Integer answerId, @RequestParam boolean newStatus) {
        AnswerResponse response = answerService.toggleAnswerStatus(answerId, newStatus);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{answerId}")
    public ResponseEntity<Void> softDeleteAnswer(@PathVariable Integer answerId) {
        answerService.softDeleteAnswer(answerId);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/search")
    public ResponseEntity<Page<AnswerResponse>> searchAnswers(@RequestBody AnswerSearchRequest request) {
        Page<AnswerResponse> responses = answerService.searchAnswers(request);
        return ResponseEntity.ok(responses);
    }
}