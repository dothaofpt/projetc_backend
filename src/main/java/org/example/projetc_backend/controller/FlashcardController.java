package org.example.projetc_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.projetc_backend.dto.ErrorResponse;
import org.example.projetc_backend.dto.FlashcardResponse;
import org.example.projetc_backend.dto.MessageResponse;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.service.FlashcardService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/flashcards")
@Tag(name = "Flashcards", description = "APIs for managing flashcards")
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    @GetMapping("/lesson/{lessonId}")
    @Operation(summary = "Get flashcards by lesson", description = "Retrieve flashcards for a specific lesson")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flashcards retrieved successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid lesson ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> getFlashcardsByLesson(
            @PathVariable Integer lessonId,
            @AuthenticationPrincipal User user) {
        try {
            List<FlashcardResponse> flashcards = flashcardService.getFlashcardsByLesson(lessonId, user.getUserId());
            return ResponseEntity.ok(flashcards);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Đã xảy ra lỗi khi lấy danh sách flashcard"));
        }
    }

    @PostMapping("/mark/{wordId}")
    @Operation(summary = "Mark flashcard", description = "Mark a flashcard as known or unknown")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "Flashcard marked successfully"),
            @ApiResponse(responseCode = "400", description = "Invalid word ID"),
            @ApiResponse(responseCode = "500", description = "Internal server error")
    })
    public ResponseEntity<?> markFlashcard(
            @PathVariable Integer wordId,
            @RequestParam boolean isKnown,
            @AuthenticationPrincipal User user) {
        try {
            flashcardService.markFlashcard(user.getUserId(), wordId, isKnown);
            return ResponseEntity.ok(new MessageResponse("Cập nhật flashcard thành công"));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest()
                    .body(new ErrorResponse(e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(500)
                    .body(new ErrorResponse("Đã xảy ra lỗi khi cập nhật flashcard"));
        }
    }
}