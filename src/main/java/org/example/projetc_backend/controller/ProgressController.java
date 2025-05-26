package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.ProgressRequest;
import org.example.projetc_backend.dto.ProgressResponse;
import org.example.projetc_backend.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    @PostMapping
    public ResponseEntity<ProgressResponse> updateProgress(@RequestBody ProgressRequest request) {
        ProgressResponse response = progressService.updateProgress(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}/lesson/{lessonId}")
    public ResponseEntity<ProgressResponse> getProgressByUserAndLesson(@PathVariable Integer userId,
                                                                       @PathVariable Integer lessonId) {
        ProgressResponse response = progressService.getProgressByUserAndLesson(userId, lessonId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProgressResponse>> getProgressByUser(@PathVariable Integer userId) {
        List<ProgressResponse> responses = progressService.getProgressByUser(userId);
        return ResponseEntity.ok(responses);
    }
}