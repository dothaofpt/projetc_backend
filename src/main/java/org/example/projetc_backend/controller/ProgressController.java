package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.ProgressRequest;
import org.example.projetc_backend.dto.ProgressResponse;
import org.example.projetc_backend.dto.ProgressSearchRequest;
import org.example.projetc_backend.dto.ProgressPageResponse;
import org.example.projetc_backend.service.ProgressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * Endpoint để tạo mới một bản ghi tiến độ.
     * Sử dụng POST request.
     * Cả USER và ADMIN đều có quyền.
     * @param request Dữ liệu yêu cầu tạo tiến độ (userId, lessonId, activityType, status, completionPercentage).
     * @return ResponseEntity chứa ProgressResponse của bản ghi đã được tạo.
     */
    @PostMapping // <-- Endpoint MỚI cho tạo mới
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ví dụ: @PreAuthorize("hasRole('ADMIN') or (#request.userId() == authentication.principal.id)")
    public ResponseEntity<ProgressResponse> createProgress(@Valid @RequestBody ProgressRequest request) {
        try {
            ProgressResponse response = progressService.createProgress(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED); // Trả về 201 Created
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint để cập nhật một bản ghi tiến độ hiện có.
     * Sử dụng PUT request và yêu cầu ID của tiến độ trên URL.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể cập nhật tiến độ của chính mình.
     * @param progressId ID của bản ghi tiến độ cần cập nhật.
     * @param request Dữ liệu yêu cầu cập nhật tiến độ.
     * @return ResponseEntity chứa ProgressResponse của bản ghi đã được cập nhật.
     */
    @PutMapping("/{progressId}") // <-- Endpoint MỚI cho cập nhật
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ví dụ: @PreAuthorize("hasRole('ADMIN') or (progressService.getProgressById(#progressId).userId() == authentication.principal.id)")
    public ResponseEntity<ProgressResponse> updateProgress(@PathVariable Integer progressId, @Valid @RequestBody ProgressRequest request) {
        try {
            // Lưu ý: Logic ủy quyền phức tạp hơn cho update (ví dụ: USER chỉ cập nhật của chính họ)
            // có thể cần được xử lý trong service hoặc sử dụng Spring Security biểu thức phức tạp hơn.
            ProgressResponse response = progressService.updateProgress(progressId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Endpoint để lấy tiến độ của một loại hoạt động cụ thể trong một bài học của người dùng.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể xem của chính mình.
     * Ví dụ: /api/progress/user/1/lesson/101/activity/READING_MATERIAL
     * @param userId ID của người dùng.
     * @param lessonId ID của bài học.
     * @param activityType Loại hoạt động (ví dụ: READING_MATERIAL, FLASHCARDS, QUIZ).
     * @return ResponseEntity chứa ProgressResponse của tiến độ hoạt động cụ thể.
     */
    @GetMapping("/user/{userId}/lesson/{lessonId}/activity/{activityType}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ví dụ: @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public ResponseEntity<ProgressResponse> getProgressByActivity(@PathVariable Integer userId,
                                                                  @PathVariable Integer lessonId,
                                                                  @PathVariable String activityType) {
        try {
            ProgressResponse response = progressService.getProgressByActivity(userId, lessonId, activityType);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint để lấy tổng phần trăm hoàn thành của một bài học cho một người dùng.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể xem của chính mình.
     * Phương thức này sẽ tính toán dựa trên tiến độ của tất cả các hoạt động con trong bài học.
     * Ví dụ: /api/progress/user/1/lesson/101/overall
     * @param userId ID của người dùng.
     * @param lessonId ID của bài học.
     * @return ResponseEntity chứa ProgressResponse tổng thể của bài học.
     */
    @GetMapping("/user/{userId}/lesson/{lessonId}/overall")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ví dụ: @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public ResponseEntity<ProgressResponse> getOverallLessonProgress(@PathVariable Integer userId,
                                                                     @PathVariable Integer lessonId) {
        try {
            ProgressResponse response = progressService.getOverallLessonProgress(userId, lessonId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint để lấy tất cả các bản ghi tiến độ của một người dùng.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể xem của chính mình.
     * Sẽ trả về danh sách các bản ghi tiến độ cho TỪNG hoạt động mà người dùng đã thực hiện.
     * Ví dụ: /api/progress/user/1
     * @param userId ID của người dùng.
     * @return ResponseEntity chứa danh sách ProgressResponse của người dùng.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ví dụ: @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public ResponseEntity<List<ProgressResponse>> getProgressByUser(@PathVariable Integer userId) {
        try {
            List<ProgressResponse> responses = progressService.getProgressByUser(userId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Xóa một bản ghi tiến độ.
     * Chỉ ADMIN mới có quyền.
     * @param progressId ID của bản ghi tiến độ cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{progressId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteProgress(@PathVariable Integer progressId) {
        try {
            progressService.deleteProgress(progressId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Endpoint để tìm kiếm và phân trang các bản ghi tiến độ.
     * Endpoint Backend: POST /api/progress/search
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa các tiêu chí tìm kiếm (userId, lessonId, activityType, status, minCompletionPercentage, maxCompletionPercentage) và thông tin phân trang/sắp xếp.
     * @return ResponseEntity chứa ProgressPageResponse của trang kết quả.
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ProgressPageResponse> searchProgress(@Valid @RequestBody ProgressSearchRequest request) {
        try {
            ProgressPageResponse response = progressService.searchProgress(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}