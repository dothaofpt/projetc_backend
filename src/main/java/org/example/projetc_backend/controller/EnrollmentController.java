package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.EnrollmentRequest;
import org.example.projetc_backend.dto.EnrollmentResponse;
import org.example.projetc_backend.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments") // Base URL cho các API liên quan đến đăng ký
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * Endpoint để đăng ký một người dùng vào một khóa học.
     * User hoặc Admin có thể thực hiện.
     * @param request EnrollmentRequest chứa userId và lessonId.
     * @return ResponseEntity với EnrollmentResponse của đăng ký vừa tạo.
     */
    @PostMapping("/enroll")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Cả user và admin đều có thể đăng ký
    public ResponseEntity<EnrollmentResponse> enrollUserInLesson(@RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enrollUserInLesson(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để lấy tất cả các đăng ký khóa học.
     * Chỉ Admin mới có quyền truy cập.
     * @return ResponseEntity với danh sách EnrollmentResponse.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        List<EnrollmentResponse> responses = enrollmentService.getAllEnrollments();
        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint để lấy danh sách các đăng ký khóa học sắp hết hạn hoặc đã hết hạn.
     * Chỉ Admin mới có quyền truy cập.
     * @return ResponseEntity với danh sách EnrollmentResponse.
     */
    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getExpiringEnrollments() {
        List<EnrollmentResponse> responses = enrollmentService.getExpiringOrExpiredEnrollments();
        return ResponseEntity.ok(responses);
    }

    /**
     * Endpoint để xóa một đăng ký khóa học.
     * Chỉ Admin mới có quyền truy cập.
     * @param enrollmentId ID của đăng ký cần xóa.
     * @return ResponseEntity không nội dung (204 No Content) nếu xóa thành công.
     */
    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Integer enrollmentId) {
        enrollmentService.deleteEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }
}