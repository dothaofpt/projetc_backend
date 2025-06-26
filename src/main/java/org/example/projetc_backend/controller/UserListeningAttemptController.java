// src/main/java/org/example/projetc_backend/controller/UserListeningAttemptController.java
package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.UserListeningAttemptRequest;
import org.example.projetc_backend.dto.UserListeningAttemptResponse;
import org.example.projetc_backend.service.UserListeningAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/listening-attempts")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class UserListeningAttemptController {

    private final UserListeningAttemptService userListeningAttemptService;

    public UserListeningAttemptController(UserListeningAttemptService userListeningAttemptService) {
        this.userListeningAttemptService = userListeningAttemptService;
    }

    /**
     * Lưu một lần thử nghe của người dùng.
     * Người dùng có thể tự gửi kết quả của mình.
     * @param request Dữ liệu lần thử nghe.
     * @return ResponseEntity với UserListeningAttemptResponse đã lưu.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Cả người dùng và admin đều có thể lưu
    public ResponseEntity<UserListeningAttemptResponse> saveListeningAttempt(@Valid @RequestBody UserListeningAttemptRequest request) {
        try {
            UserListeningAttemptResponse response = userListeningAttemptService.saveListeningAttempt(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Nên trả về lỗi chi tiết hơn
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy thông tin một lần thử nghe bằng ID.
     * ADMIN có thể xem bất kỳ lần thử nào. USER chỉ có thể xem lần thử của chính mình.
     * @param id ID của lần thử nghe.
     * @return ResponseEntity với UserListeningAttemptResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwnerOfListeningAttempt(#id)")
    // Giả định có một @Service tên là userSecurity với phương thức isOwnerOfListeningAttempt
    public ResponseEntity<UserListeningAttemptResponse> getListeningAttemptById(@PathVariable Integer id) {
        try {
            UserListeningAttemptResponse response = userListeningAttemptService.getListeningAttemptById(id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Lấy tất cả các lần thử nghe của một người dùng cụ thể.
     * ADMIN có thể xem của bất kỳ ai. USER chỉ có thể xem của chính mình.
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách UserListeningAttemptResponse.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)") // Giả định ID của user principal được lưu trong authentication.principal.id
    public ResponseEntity<List<UserListeningAttemptResponse>> getListeningAttemptsByUser(@PathVariable Integer userId) {
        try {
            List<UserListeningAttemptResponse> responses = userListeningAttemptService.getListeningAttemptsByUser(userId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Xóa một lần thử nghe.
     * Chỉ ADMIN mới có quyền xóa.
     * @param id ID của lần thử nghe cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteListeningAttempt(@PathVariable Integer id) {
        try {
            userListeningAttemptService.deleteListeningAttempt(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}