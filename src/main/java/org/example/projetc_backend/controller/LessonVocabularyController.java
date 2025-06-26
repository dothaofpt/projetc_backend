package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.LessonVocabularyRequest;
import org.example.projetc_backend.dto.LessonVocabularyResponse;
import org.example.projetc_backend.service.LessonVocabularyService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/lesson-vocabulary")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class LessonVocabularyController {

    private final LessonVocabularyService lessonVocabularyService;

    public LessonVocabularyController(LessonVocabularyService lessonVocabularyService) {
        this.lessonVocabularyService = lessonVocabularyService;
    }

    /**
     * Tạo một liên kết mới giữa bài học và từ vựng.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa lessonId và wordId.
     * @return ResponseEntity với LessonVocabularyResponse của liên kết đã tạo, hoặc lỗi BAD_REQUEST nếu không hợp lệ.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<LessonVocabularyResponse> createLessonVocabulary(@Valid @RequestBody LessonVocabularyRequest request) {
        try {
            LessonVocabularyResponse response = lessonVocabularyService.createLessonVocabulary(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Khi service ném IllegalArgumentException (ví dụ: ID không tồn tại, liên kết đã tồn tại)
            // Trả về HttpStatus.BAD_REQUEST kèm thông báo lỗi cụ thể để client biết.
            // Nếu bạn có một Global Exception Handler, bạn có thể chỉ cần ném lỗi và để handler xử lý.
            System.err.println("Lỗi khi tạo liên kết bài học-từ vựng: " + e.getMessage()); // Log lỗi
            return ResponseEntity.badRequest().body(null); // Hoặc một ErrorResponse DTO nếu có
        }
    }

    /**
     * Lấy danh sách từ vựng liên quan đến một bài học cụ thể.
     * Có thể truy cập công khai (hoặc role USER/ADMIN nếu muốn giới hạn).
     * @param lessonId ID của bài học.
     * @return ResponseEntity với danh sách LessonVocabularyResponse, hoặc HttpStatus.NOT_FOUND nếu bài học không tồn tại.
     */
    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<LessonVocabularyResponse>> getLessonVocabulariesByLessonId(@PathVariable Integer lessonId) {
        try {
            List<LessonVocabularyResponse> responses = lessonVocabularyService.getLessonVocabulariesByLessonId(lessonId);
            // Nếu danh sách rỗng, vẫn trả về OK và danh sách rỗng, không phải NOT_FOUND.
            // NOT_FOUND chỉ khi lessonId không tồn tại.
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Service ném IllegalArgumentException nếu lessonId không tồn tại.
            System.err.println("Lỗi khi lấy từ vựng theo lessonId: " + e.getMessage()); // Log lỗi
            return ResponseEntity.notFound().build(); // Trả về 404
        }
    }

    /**
     * Lấy danh sách bài học liên quan đến một từ vựng cụ thể.
     * Có thể truy cập công khai.
     * @param wordId ID của từ vựng.
     * @return ResponseEntity với danh sách LessonVocabularyResponse, hoặc HttpStatus.NOT_FOUND nếu từ vựng không tồn tại.
     */
    @GetMapping("/vocabulary/{wordId}")
    public ResponseEntity<List<LessonVocabularyResponse>> getLessonVocabulariesByWordId(@PathVariable Integer wordId) {
        try {
            List<LessonVocabularyResponse> responses = lessonVocabularyService.getLessonVocabulariesByWordId(wordId);
            // Nếu danh sách rỗng, vẫn trả về OK và danh sách rỗng.
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Service ném IllegalArgumentException nếu wordId không tồn tại.
            System.err.println("Lỗi khi lấy bài học theo wordId: " + e.getMessage()); // Log lỗi
            return ResponseEntity.notFound().build(); // Trả về 404
        }
    }


    /**
     * Xóa một liên kết cụ thể giữa bài học và từ vựng.
     * Chỉ ADMIN mới có quyền.
     * @param lessonId ID của bài học.
     * @param wordId ID của từ vựng.
     * @return ResponseEntity với HttpStatus.NO_CONTENT nếu xóa thành công, hoặc HttpStatus.NOT_FOUND nếu liên kết không tồn tại.
     */
    @DeleteMapping("/lesson/{lessonId}/word/{wordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteLessonVocabulary(@PathVariable Integer lessonId,
                                                       @PathVariable Integer wordId) {
        try {
            lessonVocabularyService.deleteLessonVocabulary(lessonId, wordId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT); // 204 No Content là chuẩn cho DELETE thành công
        } catch (IllegalArgumentException e) {
            // Service ném IllegalArgumentException nếu liên kết không tồn tại.
            System.err.println("Lỗi khi xóa liên kết bài học-từ vựng: " + e.getMessage()); // Log lỗi
            return ResponseEntity.notFound().build(); // Trả về 404
        }
    }
}