package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.ProgressRequest;
import org.example.projetc_backend.dto.ProgressResponse;
import org.example.projetc_backend.service.ProgressService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Import Valid

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
     * Endpoint để cập nhật hoặc tạo mới một bản ghi tiến độ cho một người dùng, bài học và loại hoạt động cụ thể.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể cập nhật tiến độ của chính mình.
     * @param request Dữ liệu yêu cầu cập nhật tiến độ (bao gồm userId, lessonId, activityType, status, completionPercentage).
     * @return ResponseEntity chứa ProgressResponse của bản ghi đã được xử lý.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ví dụ: @PreAuthorize("hasRole('ADMIN') or (#request.userId() == authentication.principal.id)")
    public ResponseEntity<ProgressResponse> updateProgress(@Valid @RequestBody ProgressRequest request) {
        try {
            ProgressResponse response = progressService.updateProgress(request);
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
}