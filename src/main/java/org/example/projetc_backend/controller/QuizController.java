package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.QuizRequest;
import org.example.projetc_backend.dto.QuizResponse;
import org.example.projetc_backend.service.QuizService;
import org.springframework.http.HttpStatus; // Import HttpStatus
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/quizzes")
@CrossOrigin(origins = "*") // Hoặc cấu hình CORS cụ thể hơn nếu cần
public class QuizController {

    private final QuizService quizService;

    public QuizController(QuizService quizService) {
        this.quizService = quizService;
    }

    // THÊM PHƯƠNG THỨC NÀY ĐỂ XỬ LÝ GET /api/quizzes (LẤY TẤT CẢ QUIZZES)
    @GetMapping
    public ResponseEntity<List<QuizResponse>> getAllQuizzes() {
        List<QuizResponse> responses = quizService.getAllQuizzes(); // Gọi phương thức trong service
        return ResponseEntity.ok(responses);
    }

    @PostMapping
    public ResponseEntity<QuizResponse> createQuiz(@RequestBody QuizRequest request) {
        QuizResponse response = quizService.createQuiz(request);
        // Thay vì ResponseEntity.ok, nên trả về HttpStatus.CREATED (201) cho POST thành công
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @GetMapping("/{quizId}")
    public ResponseEntity<QuizResponse> getQuizById(@PathVariable Integer quizId) {
        QuizResponse response = quizService.getQuizById(quizId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<QuizResponse>> getQuizzesByLessonId(@PathVariable Integer lessonId) {
        List<QuizResponse> responses = quizService.getQuizzesByLessonId(lessonId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{quizId}")
    public ResponseEntity<QuizResponse> updateQuiz(@PathVariable Integer quizId,
                                                   @RequestBody QuizRequest request) {
        QuizResponse response = quizService.updateQuiz(quizId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{quizId}")
    public ResponseEntity<Void> deleteQuiz(@PathVariable Integer quizId) {
        quizService.deleteQuiz(quizId);
        return ResponseEntity.noContent().build(); // 204 No Content
    }
}