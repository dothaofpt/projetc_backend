package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.AnswerRequest;
import org.example.projetc_backend.dto.AnswerResponse;
import org.example.projetc_backend.service.AnswerService;
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
    public ResponseEntity<AnswerResponse> createAnswer(@RequestBody AnswerRequest request) {
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

    @PutMapping("/{answerId}")
    public ResponseEntity<AnswerResponse> updateAnswer(@PathVariable Integer answerId, @RequestBody AnswerRequest request) {
        AnswerResponse response = answerService.updateAnswer(answerId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{answerId}")
    public ResponseEntity<Void> deleteAnswer(@PathVariable Integer answerId) {
        answerService.deleteAnswer(answerId);
        return ResponseEntity.noContent().build();
    }
}