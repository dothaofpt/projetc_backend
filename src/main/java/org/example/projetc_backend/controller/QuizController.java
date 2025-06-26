package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.QuizRequest;
import org.example.projetc_backend.dto.QuizResponse;
import org.example.projetc_backend.dto.QuizSearchRequest; // Import DTO tìm kiếm
import org.example.projetc_backend.dto.QuizPageResponse; // Import DTO phân trang
import org.example.projetc_backend.service.QuizService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    /**
     * Lấy tất cả các bài kiểm tra hiện có trong hệ thống.
     * Có thể truy cập công khai.
     * @return ResponseEntity với danh sách QuizResponse.
     */
    @GetMapping
    public ResponseEntity<List<QuizResponse>> getAllQuizzes() {
        List<QuizResponse> responses = quizService.getAllQuizzes();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Tạo một bài kiểm tra mới.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa thông tin bài kiểm tra.
     * @return ResponseEntity với QuizResponse của bài kiểm tra đã tạo.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> createQuiz(@Valid @RequestBody QuizRequest request) {
        try {
            QuizResponse response = quizService.createQuiz(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Lấy thông tin bài kiểm tra theo ID.
     * Có thể truy cập công khai.
     * @param quizId ID của bài kiểm tra.
     * @return ResponseEntity với QuizResponse.
     */
    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable Integer quizId) {
        try {
            QuizResponse response = quizService.getQuizById(quizId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy danh sách các bài kiểm tra theo ID bài học.
     * Có thể truy cập công khai.
     * @param lessonId ID của bài học.
     * @return ResponseEntity với danh sách QuizResponse.
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<QuizResponse>> getQuizzesByLessonId(@PathVariable Integer lessonId) {
        try {
            List<QuizResponse> responses = quizService.getQuizzesByLessonId(lessonId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }


    /**
     * Tìm kiếm và phân trang các bài kiểm tra dựa trên các tiêu chí tùy chọn.
     * Sử dụng @ModelAttribute để ánh xạ các RequestParam vào DTO.
     * Có thể truy cập công khai.
     * @param searchRequest DTO chứa các tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return ResponseEntity với một trang (Page) các QuizResponse phù hợp với tiêu chí tìm kiếm.
     */
    @GetMapping("/search")
    public ResponseEntity<QuizPageResponse> searchQuizzes(@ModelAttribute QuizSearchRequest searchRequest) {
        try {
            QuizPageResponse response = quizService.searchQuizzes(searchRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Cập nhật thông tin của một bài kiểm tra.
     * Chỉ ADMIN mới có quyền.
     * @param quizId ID của bài kiểm tra.
     * @param request DTO chứa thông tin cập nhật.
     * @return ResponseEntity với QuizResponse đã cập nhật.
     */
    @PutMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuizResponse> updateQuiz(@PathVariable Integer quizId,
                                                   @Valid @RequestBody QuizRequest request) {
        try {
            QuizResponse response = quizService.updateQuiz(quizId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Xóa một bài kiểm tra.
     * Chỉ ADMIN mới có quyền.
     * @param quizId ID của bài kiểm tra.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{quizId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Integer quizId) {
        try {
            quizService.deleteQuiz(quizId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}