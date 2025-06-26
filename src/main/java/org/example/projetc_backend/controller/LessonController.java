package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.LessonRequest;
import org.example.projetc_backend.dto.LessonResponse;
import org.example.projetc_backend.dto.LessonSearchRequest;
import org.example.projetc_backend.dto.LessonPageResponse;
import org.example.projetc_backend.service.LessonService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/lessons")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class LessonController {

    private final LessonService lessonService;

    public LessonController(LessonService lessonService) {
        this.lessonService = lessonService;
    }

    /**
     * Tạo một bài học mới.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa thông tin bài học.
     * @return ResponseEntity với LessonResponse của bài học đã tạo.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> createLesson(@Valid @RequestBody LessonRequest request) {
        try {
            LessonResponse response = lessonService.createLesson(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED); // Trả về 201 Created
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Trả về 400 Bad Request
        }
    }

    /**
     * Lấy thông tin một bài học theo ID.
     * Có thể truy cập công khai.
     * @param lessonId ID của bài học.
     * @return ResponseEntity với LessonResponse.
     */
    @GetMapping("/{lessonId}")
    public ResponseEntity<LessonResponse> getLessonById(@PathVariable Integer lessonId) {
        try {
            LessonResponse response = lessonService.getLessonById(lessonId);
            return new ResponseEntity<>(response, HttpStatus.OK); // Trả về 200 OK
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // Trả về 404 Not Found
        }
    }

    /**
     * Lấy tất cả các bài học đang hoạt động (không bị xóa mềm).
     * Có thể truy cập công khai.
     * @return ResponseEntity với danh sách LessonResponse.
     */
    @GetMapping // Endpoint chung để lấy tất cả active lessons
    public ResponseEntity<List<LessonResponse>> getAllActiveLessons() {
        List<LessonResponse> responses = lessonService.getAllActiveLessons();
        return new ResponseEntity<>(responses, HttpStatus.OK); // Trả về 200 OK
    }

    /**
     * Cập nhật thông tin một bài học.
     * Chỉ ADMIN mới có quyền.
     * @param lessonId ID của bài học.
     * @param request DTO chứa thông tin cập nhật.
     * @return ResponseEntity với LessonResponse đã cập nhật.
     */
    @PutMapping("/{lessonId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> updateLesson(@PathVariable Integer lessonId, @Valid @RequestBody LessonRequest request) {
        try {
            LessonResponse response = lessonService.updateLesson(lessonId, request);
            return new ResponseEntity<>(response, HttpStatus.OK); // Trả về 200 OK
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Trả về 400 Bad Request
        }
    }

    /**
     * Xóa mềm (soft delete) một bài học.
     * Chỉ ADMIN mới có quyền.
     * @param lessonId ID của bài học.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{lessonId}/soft") // Endpoint rõ ràng hơn cho soft delete
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteLesson(@PathVariable Integer lessonId) {
        try {
            lessonService.softDeleteLesson(lessonId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // Trả về 204 No Content
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND); // Trả về 404 Not Found nếu không tìm thấy bài học
        }
    }

    /**
     * Khôi phục (restore) một bài học đã bị xóa mềm.
     * Chỉ ADMIN mới có quyền.
     * @param lessonId ID của bài học.
     * @return ResponseEntity với LessonResponse đã khôi phục.
     */
    @PatchMapping("/{lessonId}/restore") // Endpoint mới để khôi phục
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonResponse> restoreLesson(@PathVariable Integer lessonId) {
        try {
            // Service trả về LessonResponse sau khi khôi phục
            LessonResponse response = lessonService.restoreLesson(lessonId);
            return new ResponseEntity<>(response, HttpStatus.OK); // Trả về 200 OK
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND); // Trả về 404 Not Found
        }
    }

    /**
     * Tìm kiếm bài học với các tiêu chí và phân trang.
     * Có thể truy cập công khai.
     * @param request DTO chứa tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return ResponseEntity với Page của LessonResponse.
     */
    @PostMapping("/search") // Dùng POST cho search nếu request body phức tạp
    public ResponseEntity<LessonPageResponse> searchLessons(@RequestBody LessonSearchRequest request) {
        try {
            LessonPageResponse response = lessonService.searchLessons(request);
            return new ResponseEntity<>(response, HttpStatus.OK); // Trả về 200 OK
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Trả về 400 Bad Request
        }
    }
}