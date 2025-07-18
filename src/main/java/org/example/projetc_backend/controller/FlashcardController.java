package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.FlashcardPageResponse;
import org.example.projetc_backend.dto.FlashcardResponse;
import org.example.projetc_backend.dto.FlashcardSearchRequest;
import org.example.projetc_backend.dto.UserFlashcardRequest;
import org.example.projetc_backend.service.FlashcardService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/flashcards")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class FlashcardController {

    private final FlashcardService flashcardService;
    private static final Logger log = LoggerFactory.getLogger(FlashcardController.class);

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FlashcardResponse> createUserFlashcard(@Valid @RequestBody UserFlashcardRequest request) {
        try {
            FlashcardResponse response = flashcardService.createUserFlashcard(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Bad Request for creating user flashcard: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Internal server error while creating user flashcard", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FlashcardPageResponse> searchUserFlashcards(@RequestBody FlashcardSearchRequest request) {
        try {
            FlashcardPageResponse response = flashcardService.searchUserFlashcards(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Bad Request for searching user flashcards: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Internal server error while searching user flashcards", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{userFlashcardId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FlashcardResponse> getUserFlashcardById(@PathVariable Integer userFlashcardId) {
        try {
            FlashcardResponse response = flashcardService.getUserFlashcardById(userFlashcardId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("User flashcard not found with id {}: {}", userFlashcardId, e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Internal server error while getting user flashcard with id {}", userFlashcardId, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{userFlashcardId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteUserFlashcard(@PathVariable Integer userFlashcardId) {
        try {
            flashcardService.deleteUserFlashcard(userFlashcardId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("User flashcard not found for deletion with id {}: {}", userFlashcardId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Internal server error while deleting user flashcard with id {}", userFlashcardId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}