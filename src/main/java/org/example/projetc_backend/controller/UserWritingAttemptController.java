package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.UserWritingAttemptRequest;
import org.example.projetc_backend.dto.UserWritingAttemptResponse;
import org.example.projetc_backend.service.UserWritingAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/writing-attempts")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class UserWritingAttemptController {

    private final UserWritingAttemptService userWritingAttemptService;
    // Bổ sung UserSecurityService nếu bạn muốn triển khai kiểm tra quyền sở hữu chi tiết hơn
    // private final UserSecurityService userSecurityService;

    public UserWritingAttemptController(UserWritingAttemptService userWritingAttemptService /*, UserSecurityService userSecurityService*/) {
        this.userWritingAttemptService = userWritingAttemptService;
        // this.userSecurityService = userSecurityService;
    }

    /**
     * Lưu một lần thử viết mới của người dùng.
     * Người dùng có thể tự lưu lần thử của mình. ADMIN cũng có thể lưu.
     * @param request DTO chứa thông tin lần thử viết.
     * @return ResponseEntity với UserWritingAttemptResponse của lần thử đã lưu.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserWritingAttemptResponse> saveWritingAttempt(@Valid @RequestBody UserWritingAttemptRequest request) {
        try {
            UserWritingAttemptResponse response = userWritingAttemptService.saveWritingAttempt(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Lấy thông tin chi tiết một lần thử viết theo ID.
     * ADMIN có thể xem bất kỳ lần thử nào. USER chỉ có thể xem lần thử của chính mình.
     * (Cần triển khai logic kiểm tra quyền sở hữu trong SecurityConfig hoặc riêng biệt)
     * Ví dụ cho USER: @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isOwnerOfWritingAttempt(#attemptId)")
     * @param attemptId ID của lần thử viết.
     * @return ResponseEntity với UserWritingAttemptResponse.
     */
    @GetMapping("/{attemptId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Tạm thời cho phép USER, cần thêm logic kiểm tra sở hữu
    public ResponseEntity<UserWritingAttemptResponse> getWritingAttemptById(@PathVariable Integer attemptId) {
        try {
            UserWritingAttemptResponse response = userWritingAttemptService.getWritingAttemptById(attemptId);
            // Thêm kiểm tra quyền sở hữu nếu người dùng là USER và không phải ADMIN
            // if (userSecurityService.isUser() && !userSecurityService.isAdmin() && !userSecurityService.isOwnerOfWritingAttempt(attemptId)) {
            //     return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            // }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy tất cả các lần thử viết của một người dùng cụ thể.
     * ADMIN có thể xem của bất kỳ người dùng nào. USER chỉ có thể xem của chính mình.
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách UserWritingAttemptResponse.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #userId") // Giả định principal có trường 'id'
    public ResponseEntity<List<UserWritingAttemptResponse>> getWritingAttemptsByUser(@PathVariable Integer userId) {
        try {
            List<UserWritingAttemptResponse> responses = userWritingAttemptService.getWritingAttemptsByUser(userId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy tất cả các lần thử viết cho một câu hỏi cụ thể.
     * ADMIN có quyền xem. Có thể mở public nếu cần cho mục đích thống kê hoặc hiển thị.
     * @param questionId ID của câu hỏi.
     * @return ResponseEntity với danh sách UserWritingAttemptResponse.
     */
    @GetMapping("/question/{questionId}")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN hoặc có thể mở rộng quyền
    public ResponseEntity<List<UserWritingAttemptResponse>> getWritingAttemptsByQuestion(@PathVariable Integer questionId) {
        try {
            List<UserWritingAttemptResponse> responses = userWritingAttemptService.getWritingAttemptsByQuestion(questionId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Xóa một lần thử viết.
     * Chỉ ADMIN mới có quyền.
     * @param attemptId ID của lần thử viết cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{attemptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWritingAttempt(@PathVariable Integer attemptId) {
        try {
            userWritingAttemptService.deleteWritingAttempt(attemptId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}