package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.LessonRequest;
import org.example.projetc_backend.dto.LessonResponse;
import org.example.projetc_backend.service.LessonService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = "*")
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    @PostMapping
    public ResponseEntity<LessonResponse> createLesson(@RequestBody LessonRequest request) {
        LessonResponse response = lessonService.createLesson(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable Integer lessonId) {
        LessonResponse response = lessonService.getLessonById(lessonId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<LessonResponse>> getAllLessons() {
        List<LessonResponse> responses = lessonService.getAllLessons();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> updateLesson(@PathVariable Integer lessonId,
                                                       @RequestBody LessonRequest request) {
        LessonResponse response = lessonService.updateLesson(lessonId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{lessonId}")
    public ResponseEntity<Void> deleteLesson(@PathVariable Integer lessonId) {
        lessonService.deleteLesson(lessonId);
        return ResponseEntity.noContent().build();
    }
}
