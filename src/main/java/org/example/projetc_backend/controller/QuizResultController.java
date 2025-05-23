package org.example.projetc_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.projetc_backend.dto.ErrorResponse;
import org.example.projetc_backend.dto.QuizResultRequest;
import org.example.projetc_backend.dto.QuizResultResponse;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.service.QuizResultService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-results")
@Tag(name = "Quiz Results", description = "APIs for managing quiz results")
public class QuizResultController {

    private final QuizResultService quizResultService;

    public QuizResultController(QuizResultService quizResultService) {
        this.quizResultService = quizResultService;
    }

    @PostMapping
    @Operation(summary = "Save quiz result", description = "Save a quiz result for a user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz result saved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> saveQuizResult(@Valid @RequestBody QuizResultRequest request, @AuthenticationPrincipal User user) {
        try {
            if (!user.getUserId().equals(request.getUserId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized to save quiz result for this user"));
            }
            QuizResultResponse response = quizResultService.saveQuizResult(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{userId}/{quizId}")
    @Operation(summary = "Get quiz result by user and quiz", description = "Retrieve quiz result for a specific user and quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz result retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Quiz result not found")
    })
    public ResponseEntity<?> getQuizResultByUserAndQuiz(@PathVariable Integer userId, @PathVariable Integer quizId, @AuthenticationPrincipal User user) {
        try {
            if (!user.getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized to view quiz result for this user"));
            }
            QuizResultResponse response = quizResultService.getQuizResultByUserAndQuiz(userId, quizId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get quiz results by user", description = "Retrieve all quiz results for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz results retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "User not found")
    })
    public ResponseEntity<?> getQuizResultsByUser(@PathVariable Integer userId, @AuthenticationPrincipal User user) {
        try {
            if (!user.getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized to view quiz results for this user"));
            }
            List<QuizResultResponse> responses = quizResultService.getQuizResultsByUser(userId);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/quiz/{quizId}")
    @Operation(summary = "Get quiz results by quiz", description = "Retrieve all quiz results for a specific quiz")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Quiz results retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Quiz not found")
    })
    public ResponseEntity<?> getQuizResultsByQuiz(@PathVariable Integer quizId) {
        try {
            List<QuizResultResponse> responses = quizResultService.getQuizResultsByQuiz(quizId);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}