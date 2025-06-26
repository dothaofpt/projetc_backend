package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.QuestionRequest;
import org.example.projetc_backend.dto.QuestionResponse;
import org.example.projetc_backend.dto.QuestionSearchRequest; // Import DTO tìm kiếm
import org.example.projetc_backend.dto.QuestionPageResponse; // Import DTO phân trang
import org.example.projetc_backend.service.QuestionService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/questions")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class QuestionController {

    private final QuestionService questionService;

    public QuestionController(QuestionService questionService) {
        this.questionService = questionService;
    }

    /**
     * Tạo một câu hỏi mới.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa thông tin câu hỏi.
     * @return ResponseEntity với QuestionResponse của câu hỏi đã tạo.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> createQuestion(@Valid @RequestBody QuestionRequest request) {
        try {
            QuestionResponse response = questionService.createQuestion(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Lấy thông tin một câu hỏi theo ID.
     * Có thể truy cập công khai (public) vì câu hỏi có thể cần cho các bài quiz/bài tập.
     * @param questionId ID của câu hỏi.
     * @return ResponseEntity với QuestionResponse.
     */
    @GetMapping("/{questionId}")
    public ResponseEntity<QuestionResponse> getQuestionById(@PathVariable Integer questionId) {
        try {
            QuestionResponse response = questionService.getQuestionById(questionId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy danh sách các câu hỏi thuộc một bài quiz cụ thể.
     * Có thể truy cập công khai.
     * @param quizId ID của bài quiz.
     * @return ResponseEntity với danh sách QuestionResponse.
     */
    @GetMapping("/quiz/{quizId}")
    public ResponseEntity<List<QuestionResponse>> getQuestionsByQuizId(@PathVariable Integer quizId) {
        try {
            List<QuestionResponse> responses = questionService.getQuestionsByQuizId(quizId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }


    /**
     * Tìm kiếm và phân trang câu hỏi dựa trên các tiêu chí tùy chọn.
     * Sử dụng @ModelAttribute để ánh xạ các RequestParam vào DTO.
     * Có thể truy cập công khai.
     * @param searchRequest DTO chứa các tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return ResponseEntity với một trang (Page) các QuestionResponse phù hợp với tiêu chí tìm kiếm.
     */
    @GetMapping("/search")
    public ResponseEntity<QuestionPageResponse> searchQuestions(@ModelAttribute QuestionSearchRequest searchRequest) {
        try {
            QuestionPageResponse response = questionService.searchQuestions(searchRequest);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }


    /**
     * Cập nhật thông tin một câu hỏi.
     * Chỉ ADMIN mới có quyền.
     * @param questionId ID của câu hỏi.
     * @param request DTO chứa thông tin cập nhật.
     * @return ResponseEntity với QuestionResponse đã cập nhật.
     */
    @PutMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<QuestionResponse> updateQuestion(@PathVariable Integer questionId,
                                                           @Valid @RequestBody QuestionRequest request) {
        try {
            QuestionResponse response = questionService.updateQuestion(questionId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Xóa một câu hỏi.
     * Chỉ ADMIN mới có quyền.
     * @param questionId ID của câu hỏi.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{questionId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteQuestion(@PathVariable Integer questionId) {
        try {
            questionService.deleteQuestion(questionId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}