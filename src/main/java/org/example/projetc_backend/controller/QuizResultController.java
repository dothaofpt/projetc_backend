package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.QuizResultRequest;
import org.example.projetc_backend.dto.QuizResultResponse;
import org.example.projetc_backend.dto.QuizResultSearchRequest; // Import DTO tìm kiếm
import org.example.projetc_backend.dto.QuizResultPageResponse; // Import DTO phân trang
import org.example.projetc_backend.service.QuizResultService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/quiz-results")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class QuizResultController {

    private final QuizResultService quizResultService;

    public QuizResultController(QuizResultService quizResultService) {
        this.quizResultService = quizResultService;
    }

    /**
     * Lưu kết quả làm bài quiz của người dùng.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể lưu kết quả của chính mình.
     * @param request DTO chứa userId, quizId và score.
     * @return ResponseEntity với QuizResultResponse của kết quả đã lưu.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ví dụ: @PreAuthorize("hasRole('ADMIN') or (#request.userId() == authentication.principal.id)")
    public ResponseEntity<QuizResultResponse> saveQuizResult(@Valid @RequestBody QuizResultRequest request) {
        try {
            QuizResultResponse response = quizResultService.saveQuizResult(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Cập nhật kết quả bài kiểm tra hiện có.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param resultId ID của kết quả bài kiểm tra cần cập nhật.
     * @param request DTO chứa thông tin cập nhật (score, durationSeconds).
     * @return ResponseEntity với QuizResultResponse của kết quả đã cập nhật.
     */
    @PutMapping("/{resultId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResultResponse> updateQuizResult(@PathVariable Integer resultId,
                                                               @Valid @RequestBody QuizResultRequest request) {
        try {
            QuizResultResponse response = quizResultService.updateQuizResult(resultId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Lấy kết quả quiz cho một người dùng và một bài quiz cụ thể.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể xem của chính mình.
     * @param userId ID của người dùng.
     * @param quizId ID của bài quiz.
     * @return ResponseEntity với QuizResultResponse.
     */
    @GetMapping("/user/{userId}/quiz/{quizId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ví dụ: @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public ResponseEntity<QuizResultResponse> getQuizResultByUserAndQuiz(@PathVariable Integer userId,
                                                                         @PathVariable Integer quizId) {
        try {
            QuizResultResponse response = quizResultService.getQuizResultByUserAndQuiz(userId, quizId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy tất cả các kết quả quiz của một người dùng.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể xem của chính mình.
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách QuizResultResponse.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ví dụ: @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public ResponseEntity<List<QuizResultResponse>> getQuizResultsByUser(@PathVariable Integer userId) {
        try {
            List<QuizResultResponse> responses = quizResultService.getQuizResultsByUser(userId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy tất cả các kết quả quiz cho một bài quiz cụ thể.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param quizId ID của bài quiz.
     * @return ResponseEntity với danh sách QuizResultResponse.
     */
    @GetMapping("/quiz/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<QuizResultResponse>> getQuizResultsByQuiz(@PathVariable Integer quizId) {
        try {
            List<QuizResultResponse> responses = quizResultService.findQuizResultsByQuiz(quizId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Tìm kiếm và phân trang kết quả bài kiểm tra dựa trên các tiêu chí tùy chọn.
     * Sử dụng @ModelAttribute để ánh xạ các RequestParam vào DTO.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param searchRequest DTO chứa các tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return ResponseEntity với một trang (Page) các QuizResultResponse phù hợp với tiêu chí tìm kiếm.
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResultPageResponse> searchQuizResults(@ModelAttribute QuizResultSearchRequest searchRequest) {
        try {
            QuizResultPageResponse response = quizResultService.searchQuizResults(searchRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Xóa một kết quả quiz.
     * Chỉ ADMIN mới có quyền.
     * @param resultId ID của kết quả quiz cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{resultId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuizResult(@PathVariable Integer resultId) {
        try {
            quizResultService.deleteQuizResult(resultId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}