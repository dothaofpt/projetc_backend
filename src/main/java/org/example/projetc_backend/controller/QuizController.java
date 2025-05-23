package org.example.projetc_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.projetc_backend.dto.ErrorResponse;
import org.example.projetc_backend.dto.MessageResponse;
import org.example.projetc_backend.dto.QuizRequest;
import org.example.projetc_backend.dto.QuizResponse;
import org.example.projetc_backend.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@Tag(name = "Quizzes", description = "APIs for managing quizzes")
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    @PostMapping
    @Operation(summary = "Create quiz", description = "Create a new quiz for a lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Quiz created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> createQuiz(@Valid @RequestBody QuizRequest request) {
        try {
            QuizResponse response = quizService.createQuiz(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{quizId}")
    @Operation(summary = "Get quiz by ID", description = "Retrieve a quiz by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Quiz not found")
    })
    public ResponseEntity<?> getQuizById(@PathVariable Integer quizId) {
        try {
            QuizResponse response = quizService.getQuizById(quizId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/lesson/{lessonId}")
    @Operation(summary = "Get quizzes by lesson", description = "Retrieve all quizzes for a specific lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quizzes retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Lesson not found")
    })
    public ResponseEntity<?> getQuizzesByLessonId(@PathVariable Integer lessonId) {
        try {
            List<QuizResponse> responses = quizService.getQuizzesByLessonId(lessonId);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PutMapping("/{quizId}")
    @Operation(summary = "Update quiz", description = "Update an existing quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> updateQuiz(@PathVariable Integer quizId, @Valid @RequestBody QuizRequest request) {
        try {
            QuizResponse response = quizService.updateQuiz(quizId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{quizId}")
    @Operation(summary = "Delete quiz", description = "Delete a quiz by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Quiz not found")
    })
    public ResponseEntity<?> deleteQuiz(@PathVariable Integer quizId) {
        try {
            quizService.deleteQuiz(quizId);
            return ResponseEntity.ok(new MessageResponse("Quiz deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}