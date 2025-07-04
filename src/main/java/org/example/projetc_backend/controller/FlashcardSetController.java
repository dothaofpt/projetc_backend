package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.FlashcardSetRequest;
import org.example.projetc_backend.dto.FlashcardSetResponse;
import org.example.projetc_backend.dto.FlashcardSetSearchRequest;
import org.example.projetc_backend.service.FlashcardSetService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/flashcard-sets")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class FlashcardSetController {

    private final FlashcardSetService flashcardSetService;

    public FlashcardSetController(FlashcardSetService flashcardSetService) {
        this.flashcardSetService = flashcardSetService;
    }

    @PostMapping
    public ResponseEntity<FlashcardSetResponse> createFlashcardSet(@Valid @RequestBody FlashcardSetRequest request) {
        try {
            FlashcardSetResponse response = flashcardSetService.createFlashcardSet(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @GetMapping("/{setId}")
    public ResponseEntity<FlashcardSetResponse> getFlashcardSetById(@PathVariable Integer setId) {
        try {
            FlashcardSetResponse response = flashcardSetService.getFlashcardSetById(setId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    // --- ĐÂY LÀ PHẦN CẦN SỬA ---
    @PostMapping("/search")
    public ResponseEntity<Page<FlashcardSetResponse>> searchFlashcardSets(@RequestBody FlashcardSetSearchRequest request) {
        try {
            Page<FlashcardSetResponse> page = flashcardSetService.searchFlashcardSets(request);
            // Trả về toàn bộ đối tượng Page, không chỉ lấy phần tử đầu tiên
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Có thể trả về Page rỗng hoặc một ResponseEntity.badRequest() với thông báo lỗi rõ ràng hơn
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
    // ----------------------------

    @PutMapping("/{setId}")
    public ResponseEntity<FlashcardSetResponse> updateFlashcardSet(@PathVariable Integer setId, @Valid @RequestBody FlashcardSetRequest request) {
        try {
            FlashcardSetResponse response = flashcardSetService.updateFlashcardSet(setId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{setId}")
    public ResponseEntity<Void> deleteFlashcardSet(@PathVariable Integer setId) {
        try {
            flashcardSetService.deleteFlashcardSet(setId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/{setId}/vocabulary/{wordId}")
    public ResponseEntity<Void> addVocabularyToSet(@PathVariable Integer setId, @PathVariable Integer wordId) {
        try {
            flashcardSetService.addVocabularyToSet(setId, wordId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        }
    }

    @DeleteMapping("/{setId}/vocabulary/{wordId}")
    public ResponseEntity<Void> removeVocabularyFromSet(@PathVariable Integer setId, @PathVariable Integer wordId) {
        try {
            flashcardSetService.removeVocabularyFromSet(setId, wordId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}