package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.LessonVocabularyRequest;
import org.example.projetc_backend.dto.LessonVocabularyResponse;
import org.example.projetc_backend.service.LessonVocabularyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/lesson-vocabulary")
@CrossOrigin(origins = "*")
public class LessonVocabularyController {

    private final LessonVocabularyService lessonVocabularyService;

    public LessonVocabularyController(LessonVocabularyService lessonVocabularyService) {
        this.lessonVocabularyService = lessonVocabularyService;
    }

    @PostMapping
    public ResponseEntity<LessonVocabularyResponse> createLessonVocabulary(@RequestBody LessonVocabularyRequest request) {
        LessonVocabularyResponse response = lessonVocabularyService.createLessonVocabulary(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<LessonVocabularyResponse>> getLessonVocabulariesByLessonId(@PathVariable Integer lessonId) {
        List<LessonVocabularyResponse> responses = lessonVocabularyService.getLessonVocabulariesByLessonId(lessonId);
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/lesson/{lessonId}/word/{wordId}")
    public ResponseEntity<Void> deleteLessonVocabulary(@PathVariable Integer lessonId,
                                                       @PathVariable Integer wordId) {
        lessonVocabularyService.deleteLessonVocabulary(lessonId, wordId);
        return ResponseEntity.noContent().build();
    }
}