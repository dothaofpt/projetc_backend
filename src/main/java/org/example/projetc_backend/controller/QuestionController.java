package org.example.projetc_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.projetc_backend.dto.ErrorResponse;
import org.example.projetc_backend.dto.MessageResponse;
import org.example.projetc_backend.dto.QuestionRequest;
import org.example.projetc_backend.dto.QuestionResponse;
import org.example.projetc_backend.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@Tag(name = "Questions", description = "APIs for managing questions")
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    @PostMapping
    @Operation(summary = "Create question", description = "Create a new question for a quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Question created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> createQuestion(@Valid @RequestBody QuestionRequest request) {
        try {
            QuestionResponse response = questionService.createQuestion(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{questionId}")
    @Operation(summary = "Get question by ID", description = "Retrieve a question by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Question not found")
    })
    public ResponseEntity<?> getQuestionById(@PathVariable Integer questionId) {
        try {
            QuestionResponse response = questionService.getQuestionById(questionId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get questions by quiz", description = "Retrieve all questions for a specific quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Questions retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Quiz not found")
    })
    public ResponseEntity<?> getQuestionsByQuizId(@PathVariable Integer quizId) {
        try {
            List<QuestionResponse> responses = questionService.getQuestionsByQuizId(quizId);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{questionId}")
    @Operation(summary = "Update question", description = "Update an existing question")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> updateQuestion(@PathVariable Integer questionId, @Valid @RequestBody QuestionRequest request) {
        try {
            QuestionResponse response = questionService.updateQuestion(questionId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{questionId}")
    @Operation(summary = "Delete question", description = "Delete a question by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Question deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Question not found")
    })
    public ResponseEntity<?> deleteQuestion(@PathVariable Integer questionId) {
        try {
            questionService.deleteQuestion(questionId);
            return ResponseEntity.ok(new MessageResponse("Question deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}