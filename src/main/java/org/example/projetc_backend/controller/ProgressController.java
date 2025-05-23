package org.example.projetc_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.projetc_backend.dto.ErrorResponse;
import org.example.projetc_backend.dto.ProgressRequest;
import org.example.projetc_backend.dto.ProgressResponse;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@Tag(name = "Progress", description = "APIs for managing user progress")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping
    @Operation(summary = "Update progress", description = "Update user progress for a lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> updateProgress(@Valid @RequestBody ProgressRequest request, @AuthenticationPrincipal User user) {
        try {
            if (!user.getUserId().equals(request.userId())) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized to update progress for this user"));
            }
            ProgressResponse response = progressService.updateProgress(request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{userId}/{lessonId}")
    @Operation(summary = "Get progress by user and lesson", description = "Retrieve progress for a specific user and lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Progress not found")
    })
    public ResponseEntity<?> getProgressByUserAndLesson(@PathVariable Integer userId, @PathVariable Integer lessonId, @AuthenticationPrincipal User user) {
        try {
            if (!user.getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized to view progress for this user"));
            }
            ProgressResponse response = progressService.getProgressByUserAndLesson(userId, lessonId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/user/{userId}")
    @Operation(summary = "Get progress by user", description = "Retrieve all progress for a specific user")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Progress retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "User not found")
    })
    public ResponseEntity<?> getProgressByUser(@PathVariable Integer userId, @AuthenticationPrincipal User user) {
        try {
            if (!user.getUserId().equals(userId)) {
                return ResponseEntity.badRequest().body(new ErrorResponse("Unauthorized to view progress for this user"));
            }
            List<ProgressResponse> responses = progressService.getProgressByUser(userId);
            return ResponseEntity.ok(responses);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}