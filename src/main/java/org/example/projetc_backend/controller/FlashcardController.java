package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.FlashcardResponse;
import org.example.projetc_backend.dto.UserFlashcardRequest;
// import org.example.projetc_backend.dto.UserFlashcardResponse; // FlashcardService trả về FlashcardResponse thay vì UserFlashcardResponse
import org.example.projetc_backend.dto.FlashcardSearchRequest;
import org.example.projetc_backend.dto.FlashcardPageResponse;
import org.example.projetc_backend.service.FlashcardService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

// import java.util.List; // Không còn cần thiết nếu không có getFlashcardsByLesson trả về List

@RestController
@RequestMapping("/api/flashcards")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class FlashcardController {

    private final FlashcardService flashcardService;

    public FlashcardController(FlashcardService flashcardService) {
        this.flashcardService = flashcardService;
    }

    // --- Cập nhật phương thức tạo/cập nhật UserFlashcard ---
    /**
     * Tạo hoặc cập nhật một UserFlashcard.
     * Sử dụng phương thức này để ghi lại trạng thái 'biết' hoặc 'chưa biết' của một từ vựng
     * đối với một người dùng cụ thể, đồng thời khởi tạo/cập nhật thông tin SRS.
     * Cả USER và ADMIN đều có quyền. USER nên chỉ có thể thao tác với flashcard của chính mình.
     *
     * @param request DTO chứa userId, wordId và trạng thái isKnown.
     * @return ResponseEntity với FlashcardResponse của flashcard đã tạo/cập nhật.
     */
    @PostMapping // Endpoint rõ ràng hơn cho việc tạo/cập nhật
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FlashcardResponse> createUserFlashcard(@Valid @RequestBody UserFlashcardRequest request) {
        try {
            // FlashcardService.createUserFlashcard trả về FlashcardResponse
            FlashcardResponse response = flashcardService.createUserFlashcard(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // --- Phương thức tìm kiếm và phân trang UserFlashcard ---
    /**
     * Tìm kiếm và phân trang các flashcard của người dùng với các tiêu chí.
     * Request DTO đã bao gồm userId.
     * Cả USER và ADMIN đều có quyền. USER nên chỉ có thể tìm kiếm của chính mình.
     *
     * @param request DTO chứa userId, tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return ResponseEntity với Page của FlashcardResponse.
     */
    @PostMapping("/search") // Endpoint không cần userId trên path vì nó có trong request body
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FlashcardPageResponse> searchUserFlashcards(@RequestBody FlashcardSearchRequest request) {
        try {
            // FlashcardService.searchUserFlashcards nhận trực tiếp FlashcardSearchRequest
            FlashcardPageResponse response = flashcardService.searchUserFlashcards(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    // --- Phương thức lấy UserFlashcard bằng ID ---
    /**
     * Lấy một UserFlashcard cụ thể bằng ID của nó.
     * Cả USER và ADMIN đều có quyền. USER nên chỉ có thể truy cập của chính mình.
     *
     * @param userFlashcardId ID của UserFlashcard.
     * @return ResponseEntity với FlashcardResponse của UserFlashcard.
     */
    @GetMapping("/{userFlashcardId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<FlashcardResponse> getUserFlashcardById(@PathVariable Integer userFlashcardId) {
        try {
            FlashcardResponse response = flashcardService.getUserFlashcardById(userFlashcardId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // Sử dụng NOT_FOUND cho trường hợp không tìm thấy
        }
    }

    // --- Phương thức xóa UserFlashcard ---
    /**
     * Xóa một UserFlashcard.
     * Cả USER và ADMIN đều có quyền. USER nên chỉ có thể xóa của chính mình.
     *
     * @param userFlashcardId ID của UserFlashcard cần xóa.
     * @return ResponseEntity với trạng thái NO_CONTENT nếu thành công.
     */
    @DeleteMapping("/{userFlashcardId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<Void> deleteUserFlashcard(@PathVariable Integer userFlashcardId) {
        try {
            flashcardService.deleteUserFlashcard(userFlashcardId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content cho xóa thành công
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}