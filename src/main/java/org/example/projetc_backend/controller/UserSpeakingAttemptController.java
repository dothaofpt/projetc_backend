package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.UserSpeakingAttemptRequest;
import org.example.projetc_backend.dto.UserSpeakingAttemptResponse;
import org.example.projetc_backend.service.UserSpeakingAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/speaking-attempts")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class UserSpeakingAttemptController {

    private final UserSpeakingAttemptService userSpeakingAttemptService;
    // Bổ sung UserSecurityService nếu bạn muốn triển khai kiểm tra quyền sở hữu chi tiết hơn
    // private final UserSecurityService userSecurityService;

    public UserSpeakingAttemptController(UserSpeakingAttemptService userSpeakingAttemptService /*, UserSecurityService userSecurityService*/) {
        this.userSpeakingAttemptService = userSpeakingAttemptService;
        // this.userSecurityService = userSecurityService;
    }

    /**
     * Lưu một lần thử nói mới của người dùng.
     * Người dùng có thể tự lưu lần thử của mình. ADMIN cũng có thể lưu.
     * @param request DTO chứa thông tin lần thử nói.
     * @return ResponseEntity với UserSpeakingAttemptResponse của lần thử đã lưu.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserSpeakingAttemptResponse> saveSpeakingAttempt(@Valid @RequestBody UserSpeakingAttemptRequest request) {
        try {
            UserSpeakingAttemptResponse response = userSpeakingAttemptService.saveSpeakingAttempt(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Lấy thông tin chi tiết một lần thử nói theo ID.
     * ADMIN có thể xem bất kỳ lần thử nào. USER chỉ có thể xem lần thử của chính mình.
     * (Cần triển khai logic kiểm tra quyền sở hữu trong SecurityConfig hoặc riêng biệt)
     * Ví dụ cho USER: @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isOwnerOfSpeakingAttempt(#attemptId)")
     * @param attemptId ID của lần thử nói.
     * @return ResponseEntity với UserSpeakingAttemptResponse.
     */
    @GetMapping("/{attemptId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Tạm thời cho phép USER, cần thêm logic kiểm tra sở hữu
    public ResponseEntity<UserSpeakingAttemptResponse> getSpeakingAttemptById(@PathVariable Integer attemptId) {
        try {
            UserSpeakingAttemptResponse response = userSpeakingAttemptService.getSpeakingAttemptById(attemptId);
            // Thêm kiểm tra quyền sở hữu nếu người dùng là USER và không phải ADMIN
            // if (userSecurityService.isUser() && !userSecurityService.isAdmin() && !userSecurityService.isOwnerOfSpeakingAttempt(attemptId)) {
            //     return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            // }
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy tất cả các lần thử nói của một người dùng cụ thể.
     * ADMIN có thể xem của bất kỳ người dùng nào. USER chỉ có thể xem của chính mình.
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách UserSpeakingAttemptResponse.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #userId") // Giả định principal có trường 'id'
    public ResponseEntity<List<UserSpeakingAttemptResponse>> getSpeakingAttemptsByUser(@PathVariable Integer userId) {
        try {
            List<UserSpeakingAttemptResponse> responses = userSpeakingAttemptService.getSpeakingAttemptsByUser(userId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy tất cả các lần thử nói cho một câu hỏi cụ thể.
     * ADMIN có quyền xem. Có thể mở public nếu cần cho mục đích thống kê hoặc hiển thị.
     * @param questionId ID của câu hỏi.
     * @return ResponseEntity với danh sách UserSpeakingAttemptResponse.
     */
    @GetMapping("/question/{questionId}")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN hoặc có thể mở rộng quyền
    public ResponseEntity<List<UserSpeakingAttemptResponse>> getSpeakingAttemptsByQuestion(@PathVariable Integer questionId) {
        try {
            List<UserSpeakingAttemptResponse> responses = userSpeakingAttemptService.getSpeakingAttemptsByQuestion(questionId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Xóa một lần thử nói.
     * Chỉ ADMIN mới có quyền.
     * @param attemptId ID của lần thử nói cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{attemptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSpeakingAttempt(@PathVariable Integer attemptId) {
        try {
            userSpeakingAttemptService.deleteSpeakingAttempt(attemptId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}