package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.VocabularyRequest;
import org.example.projetc_backend.dto.VocabularyResponse;
import org.example.projetc_backend.dto.VocabularySearchRequest; // Import VocabularySearchRequest DTO mới
import org.example.projetc_backend.service.VocabularyService;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/vocabulary")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class VocabularyController {

    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    /**
     * Tạo một từ vựng mới.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa thông tin từ vựng.
     * @return ResponseEntity với VocabularyResponse của từ vựng đã tạo.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VocabularyResponse> createVocabulary(@Valid @RequestBody VocabularyRequest request) {
        try {
            VocabularyResponse response = vocabularyService.createVocabulary(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Bao gồm thông báo lỗi trong response body
            return new ResponseEntity<>(new VocabularyResponse(null, null, null, null, null, null, null, null, null), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Lấy thông tin một từ vựng theo ID.
     * Có thể truy cập công khai.
     * @param wordId ID của từ vựng.
     * @return ResponseEntity với VocabularyResponse.
     */
    @GetMapping("/{wordId}")
    public ResponseEntity<VocabularyResponse> getVocabularyById(@PathVariable Integer wordId) {
        try {
            VocabularyResponse response = vocabularyService.getVocabularyById(wordId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy tất cả các từ vựng hoặc tìm kiếm/phân trang từ vựng.
     * Có thể truy cập công khai.
     *
     * @param searchRequest DTO chứa các tiêu chí tìm kiếm và thông tin phân trang/sắp xếp (optional).
     * @return ResponseEntity với Page<VocabularyResponse> hoặc List<VocabularyResponse> nếu không có tiêu chí tìm kiếm.
     */
    @GetMapping // Endpoint này sẽ xử lý cả getAll và search
    public ResponseEntity<?> getOrSearchVocabulary(@Valid VocabularySearchRequest searchRequest) {
        // Kiểm tra nếu tất cả các trường tìm kiếm là null và page/size là mặc định,
        // thì có thể xem xét trả về tất cả (getAllVocabulary)
        // Tuy nhiên, việc sử dụng search luôn là cách tiếp cận nhất quán và linh hoạt hơn
        // vì nó đã bao gồm logic phân trang/sắp xếp.

        // Nếu người dùng không cung cấp các tham số phân trang, chúng ta có thể đặt mặc định trong DTO.
        // VocabularySearchRequest đã có logic đặt giá trị mặc định cho page, size, sortBy, sortDir.
        Page<VocabularyResponse> responsePage = vocabularyService.searchVocabularies(searchRequest);
        return new ResponseEntity<>(responsePage, HttpStatus.OK);

        /*
        // Cách cũ nếu bạn muốn giữ riêng getAllVocabulary:
        if (searchRequest.word() == null && searchRequest.meaning() == null && searchRequest.difficultyLevel() == null &&
            searchRequest.page() == 0 && searchRequest.size() == 10 && searchRequest.sortBy().equals("wordId") && searchRequest.sortDir().equals("ASC")) {
            List<VocabularyResponse> allResponses = vocabularyService.getAllVocabulary();
            return new ResponseEntity<>(allResponses, HttpStatus.OK); // Trả về List nếu không có tham số tìm kiếm/phân trang
        } else {
            Page<VocabularyResponse> responsePage = vocabularyService.searchVocabularies(searchRequest);
            return new ResponseEntity<>(responsePage, HttpStatus.OK); // Trả về Page nếu có
        }
        */
    }


    /**
     * Cập nhật thông tin một từ vựng.
     * Chỉ ADMIN mới có quyền.
     * @param wordId ID của từ vựng.
     * @param request DTO chứa thông tin cập nhật.
     * @return ResponseEntity với VocabularyResponse đã cập nhật.
     */
    @PutMapping("/{wordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VocabularyResponse> updateVocabulary(@PathVariable Integer wordId,
                                                               @Valid @RequestBody VocabularyRequest request) {
        try {
            VocabularyResponse response = vocabularyService.updateVocabulary(wordId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(new VocabularyResponse(null, null, null, null, null, null, null, null, null), HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Xóa một từ vựng.
     * Chỉ ADMIN mới có quyền.
     * @param wordId ID của từ vựng.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{wordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVocabulary(@PathVariable Integer wordId) {
        try {
            vocabularyService.deleteVocabulary(wordId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }
}