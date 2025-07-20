package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.PracticeActivityRequest;
import org.example.projetc_backend.dto.PracticeActivityResponse;
import org.example.projetc_backend.dto.PracticeActivityPageResponse;
import org.example.projetc_backend.entity.PracticeActivity; // Import để sử dụng enum Skill và ActivityType
import org.example.projetc_backend.service.PracticeActivityService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/practice-activities")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class PracticeActivityController {

    private final PracticeActivityService practiceActivityService;

    public PracticeActivityController(PracticeActivityService practiceActivityService) {
        this.practiceActivityService = practiceActivityService;
    }

    /**
     * Tạo một hoạt động luyện tập mới.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa thông tin hoạt động luyện tập. (Bao gồm materialUrl, transcriptText, promptText, expectedOutputText)
     * @return ResponseEntity với PracticeActivityResponse của hoạt động đã tạo.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PracticeActivityResponse> createPracticeActivity(@Valid @RequestBody PracticeActivityRequest request) {
        try {
            PracticeActivityResponse response = practiceActivityService.createPracticeActivity(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Trả về một phản hồi lỗi rõ ràng hơn thay vì chỉ null
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new PracticeActivityResponse(null, null, e.getMessage(), null, null, null, null, null, null, null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy thông tin hoạt động luyện tập theo ID.
     * Có thể truy cập công khai.
     * @param activityId ID của hoạt động luyện tập.
     * @return ResponseEntity với PracticeActivityResponse. (Bao gồm materialUrl, transcriptText, promptText, expectedOutputText)
     */
    @GetMapping("/{activityId}")
    public ResponseEntity<PracticeActivityResponse> getPracticeActivityById(@PathVariable Integer activityId) {
        try {
            PracticeActivityResponse response = practiceActivityService.getPracticeActivityById(activityId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy danh sách các hoạt động luyện tập theo ID bài học.
     * Có thể truy cập công khai.
     * @param lessonId ID của bài học.
     * @return ResponseEntity với danh sách PracticeActivityResponse. (Bao gồm materialUrl, transcriptText, promptText, expectedOutputText)
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<PracticeActivityResponse>> getPracticeActivitiesByLessonId(@PathVariable Integer lessonId) {
        try {
            List<PracticeActivityResponse> responses = practiceActivityService.getPracticeActivitiesByLessonId(lessonId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy tất cả các hoạt động luyện tập hiện có trong hệ thống.
     * Có thể truy cập công khai.
     * @return ResponseEntity với danh sách PracticeActivityResponse. (Bao gồm materialUrl, transcriptText, promptText, expectedOutputText)
     */
    @GetMapping
    public ResponseEntity<List<PracticeActivityResponse>> getAllPracticeActivities() {
        List<PracticeActivityResponse> responses = practiceActivityService.getAllPracticeActivities();
        return new ResponseEntity<>(responses, HttpStatus.OK);
    }

    /**
     * Tìm kiếm và phân trang các hoạt động luyện tập dựa trên các tiêu chí tùy chọn.
     * @param lessonId ID bài học (tùy chọn).
     * @param title Tiêu đề (tùy chọn).
     * @param skill Kỹ năng (tùy chọn).
     * @param activityType Loại hoạt động (tùy chọn).
     * @param page Số trang (mặc định 0).
     * @param size Kích thước trang (mặc định 10).
     * @param sortBy Trường để sắp xếp (mặc định "activityId").
     * @param sortDir Hướng sắp xếp (mặc định "ASC").
     * @return ResponseEntity với một trang (Page) các PracticeActivityResponse. (Bao gồm materialUrl, transcriptText, promptText, expectedOutputText)
     */
    @GetMapping("/search")
    public ResponseEntity<PracticeActivityPageResponse> searchPracticeActivities(
            @RequestParam(required = false) Integer lessonId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) PracticeActivity.ActivitySkill skill,
            @RequestParam(required = false) PracticeActivity.ActivityType activityType,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(defaultValue = "activityId") String sortBy,
            @RequestParam(defaultValue = "ASC") String sortDir) {
        try {
            PracticeActivityPageResponse response = practiceActivityService.searchPracticeActivities(
                    lessonId, title, skill, activityType, page, size, sortBy, sortDir
            );
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    /**
     * Cập nhật thông tin của một hoạt động luyện tập.
     * Chỉ ADMIN mới có quyền.
     * @param activityId ID của hoạt động luyện tập.
     * @param request DTO chứa thông tin cập nhật. (Bao gồm materialUrl, transcriptText, promptText, expectedOutputText)
     * @return ResponseEntity với PracticeActivityResponse đã cập nhật.
     */
    @PutMapping("/{activityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PracticeActivityResponse> updatePracticeActivity(@PathVariable Integer activityId,
                                                                           @Valid @RequestBody PracticeActivityRequest request) {
        try {
            PracticeActivityResponse response = practiceActivityService.updatePracticeActivity(activityId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Xóa một hoạt động luyện tập.
     * Chỉ ADMIN mới có quyền.
     * @param activityId ID của hoạt động luyện tập cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{activityId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePracticeActivity(@PathVariable Integer activityId) {
        try {
            practiceActivityService.deletePracticeActivity(activityId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}