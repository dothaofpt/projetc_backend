package org.example.projetc_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.example.projetc_backend.dto.ErrorResponse;
import org.example.projetc_backend.dto.LessonRequest;
import org.example.projetc_backend.dto.LessonResponse;
import org.example.projetc_backend.dto.MessageResponse;
import org.example.projetc_backend.service.LessonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@Tag(name = "Lessons", description = "APIs for managing lessons")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping
    @Operation(summary = "Create lesson", description = "Create a new lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Lesson created successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> createLesson(@Valid @RequestBody LessonRequest request) {
        try {
            LessonResponse response = lessonService.createLesson(request);
            return ResponseEntity.status(HttpStatus.CREATED).body(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{lessonId}")
    @Operation(summary = "Get lesson by ID", description = "Retrieve a lesson by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Lesson not found")
    })
    public ResponseEntity<?> getLessonById(@PathVariable Integer lessonId) {
        try {
            LessonResponse response = lessonService.getLessonById(lessonId);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping
    @Operation(summary = "Get all lessons", description = "Retrieve all lessons")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lessons retrieved successfully")
    })
    public ResponseEntity<?> getAllLessons() {
        try {
            List<LessonResponse> responses = lessonService.getAllLessons();
            return ResponseEntity.ok(responses);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(new ErrorResponse("Error retrieving lessons"));
        }
    }

    @PutMapping("/{lessonId}")
    @Operation(summary = "Update lesson", description = "Update an existing lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson updated successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid request data")
    })
    public ResponseEntity<?> updateLesson(@PathVariable Integer lessonId, @Valid @RequestBody LessonRequest request) {
        try {
            LessonResponse response = lessonService.updateLesson(lessonId, request);
            return ResponseEntity.ok(response);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{lessonId}")
    @Operation(summary = "Delete lesson", description = "Delete a lesson by its ID")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Lesson deleted successfully"),
            @ApiResponse(responseCode = "400", description = "Lesson not found")
    })
    public ResponseEntity<?> deleteLesson(@PathVariable Integer lessonId) {
        try {
            lessonService.deleteLesson(lessonId);
            return ResponseEntity.ok(new MessageResponse("Lesson deleted successfully"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}