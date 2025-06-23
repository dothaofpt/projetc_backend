package org.example.projetc_backend.controller;// package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.EnrollmentRequest;
import org.example.projetc_backend.dto.EnrollmentResponse;
import org.example.projetc_backend.service.EnrollmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    @PostMapping("/enroll")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EnrollmentResponse> enrollUserInLesson(@RequestBody EnrollmentRequest request) {
        EnrollmentResponse response = enrollmentService.enrollUserInLesson(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        List<EnrollmentResponse> responses = enrollmentService.getAllEnrollments();
        return ResponseEntity.ok(responses);
    }

    // THÊM ENDPOINT MỚI NÀY: Lấy danh sách đăng ký của một người dùng cụ thể
    /**
     * Endpoint để lấy danh sách các đăng ký khóa học của một người dùng cụ thể.
     * Cả User và Admin đều có thể truy cập.
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách EnrollmentResponse.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Cả user và admin đều có thể xem đăng ký của user
    public ResponseEntity<List<EnrollmentResponse>> getUserEnrollments(@PathVariable Integer userId) {
        List<EnrollmentResponse> responses = enrollmentService.getEnrollmentsByUserId(userId);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/expiring")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getExpiringEnrollments() {
        List<EnrollmentResponse> responses = enrollmentService.getExpiringOrExpiredEnrollments();
        return ResponseEntity.ok(responses);
    }

    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Integer enrollmentId) {
        enrollmentService.deleteEnrollment(enrollmentId);
        return ResponseEntity.noContent().build();
    }
}