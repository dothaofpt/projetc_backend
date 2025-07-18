package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.FlashcardSetRequest;
import org.example.projetc_backend.dto.FlashcardSetResponse;
import org.example.projetc_backend.dto.FlashcardSetSearchRequest;
import org.example.projetc_backend.service.FlashcardSetService;
import org.example.projetc_backend.service.UserService; // THÊM IMPORT NÀY: Inject UserService
import org.example.projetc_backend.entity.User; // THÊM IMPORT NÀY: Để làm việc với User entity
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/api/flashcard-sets")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class FlashcardSetController {

    private final FlashcardSetService flashcardSetService;
    private final UserService userService; // THÊM: Inject UserService
    private static final Logger log = LoggerFactory.getLogger(FlashcardSetController.class);

    // Cập nhật constructor để nhận UserService
    public FlashcardSetController(FlashcardSetService flashcardSetService, UserService userService) {
        this.flashcardSetService = flashcardSetService;
        this.userService = userService; // Gán UserService
    }

    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FlashcardSetResponse> createFlashcardSet(@Valid @RequestBody FlashcardSetRequest request) {
        try {
            FlashcardSetResponse response = flashcardSetService.createFlashcardSet(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            log.warn("Bad Request for creating flashcard set: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Internal server error while creating flashcard set", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @GetMapping("/{setId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FlashcardSetResponse> getFlashcardSetById(
            @PathVariable Integer setId,
            @AuthenticationPrincipal UserDetails userDetails) {
        try {
            // Lấy username từ UserDetails
            String username = userDetails.getUsername();

            // SỬA LỖI CHÍNH Ở ĐÂY: Tìm User entity bằng username để lấy userId thực sự
            User user = userService.findByUsername(username)
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với username: " + username));

            Integer currentUserId = user.getUserId(); // Lấy userId từ đối tượng User đã tìm thấy

            FlashcardSetResponse response = flashcardSetService.getFlashcardSetById(setId, currentUserId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Flashcard set not found or user data issue for set id {}: {}", setId, e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Internal server error while getting flashcard set with id {} for user {}", setId, userDetails.getUsername(), e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/search")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Page<FlashcardSetResponse>> searchFlashcardSets(@RequestBody FlashcardSetSearchRequest request) {
        try {
            Page<FlashcardSetResponse> page = flashcardSetService.searchFlashcardSets(request);
            return new ResponseEntity<>(page, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Bad Request for searching flashcard sets: {}", e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Internal server error while searching flashcard sets", e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PutMapping("/{setId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FlashcardSetResponse> updateFlashcardSet(@PathVariable Integer setId, @Valid @RequestBody FlashcardSetRequest request) {
        try {
            FlashcardSetResponse response = flashcardSetService.updateFlashcardSet(setId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to update flashcard set with id {}: {}", setId, e.getMessage());
            if (e.getMessage().contains("Không tìm thấy")) {
                return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
            }
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Internal server error while updating flashcard set with id {}", setId, e);
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{setId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteFlashcardSet(@PathVariable Integer setId) {
        try {
            flashcardSetService.deleteFlashcardSet(setId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to delete flashcard set with id {}: {}", setId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Internal server error while deleting flashcard set with id {}", setId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @PostMapping("/{setId}/vocabulary/{wordId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> addVocabularyToSet(@PathVariable Integer setId, @PathVariable Integer wordId) {
        try {
            flashcardSetService.addVocabularyToSet(setId, wordId);
            return new ResponseEntity<>(HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to add vocabulary {} to set {}: {}", wordId, setId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            log.error("Internal server error while adding vocabulary {} to set {}", wordId, setId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    @DeleteMapping("/{setId}/vocabulary/{wordId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> removeVocabularyFromSet(@PathVariable Integer setId, @PathVariable Integer wordId) {
        try {
            flashcardSetService.removeVocabularyFromSet(setId, wordId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            log.warn("Failed to remove vocabulary {} from set {}: {}", wordId, setId, e.getMessage());
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            log.error("Internal server error while removing vocabulary {} from set {}", wordId, setId, e);
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}