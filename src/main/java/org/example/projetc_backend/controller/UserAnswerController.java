package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.UserAnswerRequest;
import org.example.projetc_backend.dto.UserAnswerResponse;
import org.example.projetc_backend.service.UserAnswerService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/user-answers")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class UserAnswerController {

    private final UserAnswerService userAnswerService;

    public UserAnswerController(UserAnswerService userAnswerService) {
        this.userAnswerService = userAnswerService;
    }

    /**
     * Endpoint để lưu câu trả lời của người dùng cho một câu hỏi cụ thể trong một phiên làm bài quiz.
     * Cho phép cả USER và ADMIN.
     * @param request DTO chứa thông tin câu trả lời của người dùng.
     * @return ResponseEntity với UserAnswerResponse của câu trả lời đã lưu.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserAnswerResponse> submitUserAnswer(@Valid @RequestBody UserAnswerRequest request) {
        try {
            UserAnswerResponse response = userAnswerService.submitUserAnswer(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new UserAnswerResponse(null, null, null, null, null, null));
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new UserAnswerResponse(null, null, null, null, null, null));
        }
    }

    /**
     * Endpoint để lấy tất cả câu trả lời của người dùng cho một kết quả quiz cụ thể.
     * Cho phép cả USER (cho kết quả của chính mình) và ADMIN.
     * @param quizResultId ID của kết quả bài kiểm tra.
     * @return ResponseEntity với danh sách UserAnswerResponse.
     */
    @GetMapping("/quiz-result/{quizResultId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Cần thêm logic kiểm tra quyền sở hữu cho USER
    public ResponseEntity<List<UserAnswerResponse>> getUserAnswersByQuizResultId(@PathVariable Integer quizResultId) {
        try {
            // TODO: Đối với USER, cần kiểm tra xem quizResultId này có thuộc về userId của người dùng hiện tại không.
            // Ví dụ: Lấy userId từ Authentication.principal.id, sau đó kiểm tra quizResult.getUser().getUserId() == currentUserId.
            List<UserAnswerResponse> responses = userAnswerService.getUserAnswersByQuizResultId(quizResultId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(List.of());
        } catch (Exception e) {
            // Log the exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }
}