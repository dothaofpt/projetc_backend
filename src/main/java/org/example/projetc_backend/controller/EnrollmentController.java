package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.EnrollmentRequest;
import org.example.projetc_backend.dto.EnrollmentResponse;
import org.example.projetc_backend.dto.EnrollmentSearchRequest;
import org.example.projetc_backend.service.EnrollmentService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.Collections;
import java.util.List;

@RestController
@RequestMapping("/api/enrollments")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class EnrollmentController {

    private final EnrollmentService enrollmentService;

    public EnrollmentController(EnrollmentService enrollmentService) {
        this.enrollmentService = enrollmentService;
    }

    /**
     * Đăng ký người dùng vào một bài học.
     * Cả USER và ADMIN đều có quyền.
     * @param request DTO chứa userId và lessonId.
     * @return ResponseEntity với EnrollmentResponse của đăng ký đã tạo.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<EnrollmentResponse> enrollUserInLesson(@Valid @RequestBody EnrollmentRequest request) {
        try {
            EnrollmentResponse response = enrollmentService.enrollUserInLesson(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Trả về BAD_REQUEST với thông báo lỗi rõ ràng từ Service
            // Sử dụng EnrollmentResponse để chứa thông báo lỗi, các trường khác là null
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    new EnrollmentResponse(null, null, null, null, null, null, e.getMessage()) // 'e.getMessage()' đi vào trường 'status'
            );
        } catch (Exception e) {
            // Xử lý các lỗi không mong muốn khác
            // Nên log exception ở đây: logger.error("Error creating enrollment", e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new EnrollmentResponse(null, null, null, null, null, null, "Lỗi hệ thống khi đăng ký bài học.")
            );
        }
    }

    /**
     * Lấy thông tin một đăng ký theo ID.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param enrollmentId ID của đăng ký.
     * @return ResponseEntity với EnrollmentResponse.
     */
    @GetMapping("/{enrollmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<EnrollmentResponse> getEnrollmentById(@PathVariable Integer enrollmentId) {
        try {
            EnrollmentResponse response = enrollmentService.getEnrollmentById(enrollmentId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Trả về NOT_FOUND với thông báo lỗi rõ ràng
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(
                    new EnrollmentResponse(null, null, null, null, null, null, e.getMessage())
            );
        } catch (Exception e) {
            // Log exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(
                    new EnrollmentResponse(null, null, null, null, null, null, "Lỗi hệ thống khi lấy thông tin đăng ký.")
            );
        }
    }

    /**
     * Lấy tất cả các đăng ký khóa học.
     * Chỉ ADMIN mới có quyền truy cập.
     * @return ResponseEntity với danh sách EnrollmentResponse.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getAllEnrollments() {
        try {
            List<EnrollmentResponse> responses = enrollmentService.getAllEnrollments();
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (Exception e) {
            // Log exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    /**
     * Lấy danh sách đăng ký của một người dùng cụ thể.
     * Cả USER và ADMIN đều có thể truy cập (ADMIN xem được của bất kỳ ai).
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách EnrollmentResponse.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getUserEnrollments(@PathVariable Integer userId) {
        // Có thể thêm logic kiểm tra userId == authenticated_user_id cho USER role ở đây hoặc trong service
        try {
            List<EnrollmentResponse> responses = enrollmentService.getEnrollmentsByUserId(userId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Trả về BAD_REQUEST nếu userId không hợp lệ (ví dụ: không tìm thấy user)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        } catch (Exception e) {
            // Log exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    /**
     * Lấy danh sách đăng ký cho một bài học cụ thể.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param lessonId ID của bài học.
     * @return ResponseEntity với danh sách EnrollmentResponse.
     */
    @GetMapping("/lesson/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<EnrollmentResponse>> getEnrollmentsByLessonId(@PathVariable Integer lessonId) {
        try {
            List<EnrollmentResponse> responses = enrollmentService.getEnrollmentsByLessonId(lessonId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Trả về BAD_REQUEST nếu lessonId không hợp lệ (ví dụ: không tìm thấy lesson)
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Collections.emptyList());
        } catch (Exception e) {
            // Log exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Collections.emptyList());
        }
    }

    /**
     * Tìm kiếm đăng ký với các tiêu chí và phân trang.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param request DTO chứa tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return ResponseEntity với Page của EnrollmentResponse.
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<EnrollmentResponse>> searchEnrollments(@RequestBody EnrollmentSearchRequest request) {
        try {
            Page<EnrollmentResponse> responses = enrollmentService.searchEnrollments(request);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Trả về BAD_REQUEST với Page rỗng nếu lỗi
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Page.empty());
        } catch (Exception e) {
            // Log exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }

    /**
     * Xóa một đăng ký khóa học.
     * Chỉ ADMIN mới có quyền.
     * @param enrollmentId ID của đăng ký.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{enrollmentId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteEnrollment(@PathVariable Integer enrollmentId) {
        try {
            enrollmentService.deleteEnrollment(enrollmentId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            // Phân biệt NOT_FOUND và BAD_REQUEST (nếu service có ném lỗi đã bị xóa...)
            // Hiện tại service chỉ ném NOT_FOUND nếu ID không tồn tại
            return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
        } catch (Exception e) {
            // Log exception
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }
}