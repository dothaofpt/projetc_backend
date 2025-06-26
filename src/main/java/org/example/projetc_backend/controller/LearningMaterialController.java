package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.LearningMaterialRequest;
import org.example.projetc_backend.dto.LearningMaterialResponse;
import org.example.projetc_backend.dto.LearningMaterialSearchRequest;
import org.example.projetc_backend.service.LearningMaterialService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/learning-materials")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class LearningMaterialController {

    private final LearningMaterialService learningMaterialService;

    public LearningMaterialController(LearningMaterialService learningMaterialService) {
        this.learningMaterialService = learningMaterialService;
    }

    /**
     * Tạo một tài liệu học tập mới.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa thông tin tài liệu.
     * @return ResponseEntity với LearningMaterialResponse của tài liệu đã tạo.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LearningMaterialResponse> createLearningMaterial(@Valid @RequestBody LearningMaterialRequest request) {
        try {
            LearningMaterialResponse response = learningMaterialService.createLearningMaterial(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Lấy thông tin một tài liệu học tập theo ID.
     * Có thể truy cập công khai.
     * @param materialId ID của tài liệu.
     * @return ResponseEntity với LearningMaterialResponse.
     */
    @GetMapping("/{materialId}")
    public ResponseEntity<LearningMaterialResponse> getLearningMaterialById(@PathVariable Integer materialId) {
        try {
            LearningMaterialResponse response = learningMaterialService.getLearningMaterialById(materialId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy danh sách tài liệu học tập theo ID bài học.
     * Có thể truy cập công khai.
     * @param lessonId ID của bài học.
     * @return ResponseEntity với danh sách LearningMaterialResponse.
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<LearningMaterialResponse>> getLearningMaterialsByLessonId(@PathVariable Integer lessonId) {
        try {
            List<LearningMaterialResponse> responses = learningMaterialService.getLearningMaterialsByLessonId(lessonId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Cập nhật thông tin một tài liệu học tập.
     * Chỉ ADMIN mới có quyền.
     * @param materialId ID của tài liệu.
     * @param request DTO chứa thông tin cập nhật.
     * @return ResponseEntity với LearningMaterialResponse đã cập nhật.
     */
    @PutMapping("/{materialId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LearningMaterialResponse> updateLearningMaterial(@PathVariable Integer materialId,
                                                                           @Valid @RequestBody LearningMaterialRequest request) {
        try {
            LearningMaterialResponse response = learningMaterialService.updateLearningMaterial(materialId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Xóa một tài liệu học tập.
     * Chỉ ADMIN mới có quyền.
     * @param materialId ID của tài liệu.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{materialId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLearningMaterial(@PathVariable Integer materialId) {
        try {
            learningMaterialService.deleteLearningMaterial(materialId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Tìm kiếm tài liệu học tập với các tiêu chí và phân trang.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return ResponseEntity với Page của LearningMaterialResponse.
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<LearningMaterialResponse>> searchLearningMaterials(@RequestBody LearningMaterialSearchRequest request) {
        try {
            Page<LearningMaterialResponse> responses = learningMaterialService.searchLearningMaterials(request);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }
}