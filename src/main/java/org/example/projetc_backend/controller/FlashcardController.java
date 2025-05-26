package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.FlashcardResponse;
import org.example.projetc_backend.dto.UserFlashcardRequest;
import org.example.projetc_backend.dto.UserFlashcardResponse;
import org.example.projetc_backend.service.FlashcardService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@CrossOrigin(origins = "*")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @GetMapping("/lesson/{lessonId}/user/{userId}")
    public ResponseEntity<List<FlashcardResponse>> getFlashcardsByLesson(@PathVariable Integer lessonId,
                                                                         @PathVariable Integer userId) {
        List<FlashcardResponse> responses = flashcardService.getFlashcardsByLesson(lessonId, userId);
        return ResponseEntity.ok(responses);
    }

    @PostMapping("/mark")
    public ResponseEntity<UserFlashcardResponse> markFlashcard(@RequestBody UserFlashcardRequest request) {
        UserFlashcardResponse response = flashcardService.markFlashcard(request);
        return ResponseEntity.ok(response);
    }
}
