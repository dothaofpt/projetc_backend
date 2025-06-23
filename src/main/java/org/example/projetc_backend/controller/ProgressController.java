package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.ProgressRequest;
import org.example.projetc_backend.dto.ProgressResponse;
import org.example.projetc_backend.service.ProgressService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/progress")
@CrossOrigin(origins = "*")
public class ProgressController {

    private final ProgressService progressService;

    public ProgressController(ProgressService progressService) {
        this.progressService = progressService;
    }

    /**
     * Endpoint để cập nhật hoặc tạo mới một bản ghi tiến độ cho một người dùng, bài học và loại hoạt động cụ thể.
     * @param request Dữ liệu yêu cầu cập nhật tiến độ (bao gồm userId, lessonId, activityType, status, completionPercentage).
     * @return ResponseEntity chứa ProgressResponse của bản ghi đã được xử lý.
     */
    @PostMapping
    public ResponseEntity<ProgressResponse> updateProgress(@RequestBody ProgressRequest request) {
        ProgressResponse response = progressService.updateProgress(request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để lấy tiến độ của một loại hoạt động cụ thể trong một bài học của người dùng.
     * Ví dụ: /api/progress/user/1/lesson/101/activity/READING_MATERIAL
     * @param userId ID của người dùng.
     * @param lessonId ID của bài học.
     * @param activityType Loại hoạt động (ví dụ: READING_MATERIAL, FLASHCARDS, QUIZ).
     * @return ResponseEntity chứa ProgressResponse của tiến độ hoạt động cụ thể.
     */
    @GetMapping("/user/{userId}/lesson/{lessonId}/activity/{activityType}")
    public ResponseEntity<ProgressResponse> getProgressByActivity(@PathVariable Integer userId,
                                                                  @PathVariable Integer lessonId,
                                                                  @PathVariable String activityType) {
        ProgressResponse response = progressService.getProgressByActivity(userId, lessonId, activityType);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để lấy tổng phần trăm hoàn thành của một bài học cho một người dùng.
     * Phương thức này sẽ tính toán dựa trên tiến độ của tất cả các hoạt động con trong bài học.
     * Ví dụ: /api/progress/user/1/lesson/101/overall
     * @param userId ID của người dùng.
     * @param lessonId ID của bài học.
     * @return ResponseEntity chứa ProgressResponse tổng thể của bài học.
     */
    @GetMapping("/user/{userId}/lesson/{lessonId}/overall")
    public ResponseEntity<ProgressResponse> getOverallLessonProgress(@PathVariable Integer userId,
                                                                     @PathVariable Integer lessonId) {
        ProgressResponse response = progressService.getOverallLessonProgress(userId, lessonId);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để lấy tất cả các bản ghi tiến độ của một người dùng.
     * Sẽ trả về danh sách các bản ghi tiến độ cho TỪNG hoạt động mà người dùng đã thực hiện.
     * Ví dụ: /api/progress/user/1
     * @param userId ID của người dùng.
     * @return ResponseEntity chứa danh sách ProgressResponse của người dùng.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<ProgressResponse>> getProgressByUser(@PathVariable Integer userId) {
        List<ProgressResponse> responses = progressService.getProgressByUser(userId);
        return ResponseEntity.ok(responses);
    }
}
