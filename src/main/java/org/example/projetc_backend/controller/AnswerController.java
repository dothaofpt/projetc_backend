package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.AnswerRequest;
import org.example.projetc_backend.dto.AnswerResponse;
import org.example.projetc_backend.dto.AnswerSearchRequest;
import org.example.projetc_backend.service.AnswerService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Đảm bảo import này chính xác

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    /**
     * Endpoint để tạo một câu trả lời mới cho một câu hỏi.
     * Yêu cầu quyền ADMIN.
     * @param request DTO chứa thông tin câu trả lời (bao gồm questionId, content, isCorrect, isActive).
     * @return ResponseEntity với AnswerResponse của câu trả lời đã tạo.
     * Trả về HttpStatus.CREATED nếu thành công.
     * Trả về HttpStatus.BAD_REQUEST nếu dữ liệu không hợp lệ hoặc logic nghiệp vụ bị vi phạm.
     * Trả về HttpStatus.INTERNAL_SERVER_ERROR cho các lỗi hệ thống.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerResponse> createAnswer(@Valid @RequestBody AnswerRequest request) {
        try {
            AnswerResponse response = answerService.createAnswer(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AnswerResponse(null, null, e.getMessage(), null, null, null));
        } catch (Exception e) {
            // Log the exception for debugging in a real application
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AnswerResponse(null, null, "Lỗi hệ thống khi tạo câu trả lời.", null, null, null));
        }
    }

    /**
     * Endpoint để lấy thông tin một câu trả lời theo ID.
     * Có thể truy cập công khai.
     * @param answerId ID của câu trả lời.
     * @return ResponseEntity với AnswerResponse.
     * Trả về HttpStatus.OK nếu tìm thấy.
     * Trả về HttpStatus.NOT_FOUND nếu không tìm thấy câu trả lời.
     * Trả về HttpStatus.INTERNAL_SERVER_ERROR cho các lỗi hệ thống.
     */
    @GetMapping("/{answerId}")
    public ResponseEntity<AnswerResponse> getAnswerById(@PathVariable Integer answerId) {
        try {
            AnswerResponse response = answerService.getAnswerById(answerId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AnswerResponse(null, null, e.getMessage(), null, null, null));
        } catch (Exception e) {
            // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AnswerResponse(null, null, "Lỗi hệ thống khi lấy câu trả lời.", null, null, null));
        }
    }

    /**
     * Endpoint để lấy danh sách các câu trả lời đang hoạt động (isActive=true) và chưa bị xóa mềm của một câu hỏi cụ thể.
     * Dành cho người dùng cuối.
     * @param questionId ID của câu hỏi.
     * @return ResponseEntity với danh sách AnswerResponse.
     * Trả về HttpStatus.OK (danh sách có thể rỗng).
     * Trả về HttpStatus.NOT_FOUND nếu questionId không hợp lệ hoặc không tìm thấy câu hỏi (mặc dù service trả về danh sách rỗng, ở đây controller có thể linh hoạt hơn).
     * Trả về HttpStatus.INTERNAL_SERVER_ERROR cho các lỗi hệ thống.
     */
    @GetMapping("/question/{questionId}/active")
    public ResponseEntity<List<AnswerResponse>> getActiveAnswersByQuestionId(@PathVariable Integer questionId) {
        try {
            List<AnswerResponse> responses = answerService.getAnswersByQuestionIdForUser(questionId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Nếu questionId không hợp lệ, trả về BAD_REQUEST hoặc NOT_FOUND
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of());
        } catch (Exception e) {
            // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    /**
     * Endpoint để lấy tất cả các câu trả lời (kể cả không hoạt động nhưng chưa bị xóa mềm) của một câu hỏi cụ thể.
     * Yêu cầu quyền ADMIN.
     * @param questionId ID của câu hỏi.
     * @return ResponseEntity với danh sách AnswerResponse.
     * Trả về HttpStatus.OK (danh sách có thể rỗng).
     * Trả về HttpStatus.NOT_FOUND nếu questionId không hợp lệ.
     * Trả về HttpStatus.INTERNAL_SERVER_ERROR cho các lỗi hệ thống.
     */
    @GetMapping("/question/{questionId}/admin")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<AnswerResponse>> getAllAnswersForAdminByQuestionId(@PathVariable Integer questionId) {
        try {
            List<AnswerResponse> responses = answerService.getAllAnswersForAdminByQuestionId(questionId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(List.of());
        } catch (Exception e) {
            // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(List.of());
        }
    }

    /**
     * Endpoint để cập nhật thông tin một câu trả lời.
     * Yêu cầu quyền ADMIN.
     * @param answerId ID của câu trả lời.
     * @param request DTO chứa thông tin cập nhật (bao gồm content, isCorrect, isActive).
     * @return ResponseEntity với AnswerResponse đã cập nhật.
     * Trả về HttpStatus.OK nếu thành công.
     * Trả về HttpStatus.BAD_REQUEST nếu dữ liệu không hợp lệ hoặc logic nghiệp vụ bị vi phạm (ví dụ: đã xóa mềm, hoặc vi phạm quy tắc 1 câu trả lời đúng).
     * Trả về HttpStatus.NOT_FOUND nếu không tìm thấy câu trả lời.
     * Trả về HttpStatus.INTERNAL_SERVER_ERROR cho các lỗi hệ thống.
     */
    @PutMapping("/{answerId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerResponse> updateAnswer(@PathVariable Integer answerId, @Valid @RequestBody AnswerRequest request) {
        try {
            AnswerResponse response = answerService.updateAnswer(answerId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Service có thể ném IllegalArgumentException cho cả trường hợp not found và bad request
            // Có thể dùng instanceof để phân biệt hoặc service ném ra các exception cụ thể hơn
            // Hiện tại, tạm thời trả về BAD_REQUEST cho tất cả IllegalArgumentException
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AnswerResponse(null, null, e.getMessage(), null, null, null));
        } catch (Exception e) {
            // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AnswerResponse(null, null, "Lỗi hệ thống khi cập nhật câu trả lời.", null, null, null));
        }
    }

    /**
     * Endpoint để thay đổi trạng thái hoạt động (active/inactive) của một câu trả lời.
     * Yêu cầu quyền ADMIN.
     * @param answerId ID của câu trả lời.
     * @param newStatus Trạng thái mới (true: active, false: inactive).
     * @return ResponseEntity với AnswerResponse đã cập nhật trạng thái.
     * Trả về HttpStatus.OK nếu thành công.
     * Trả về HttpStatus.BAD_REQUEST nếu dữ liệu không hợp lệ hoặc logic nghiệp vụ bị vi phạm (ví dụ: đã xóa mềm, hoặc vi phạm quy tắc 1 câu trả lời đúng).
     * Trả về HttpStatus.NOT_FOUND nếu không tìm thấy câu trả lời.
     * Trả về HttpStatus.INTERNAL_SERVER_ERROR cho các lỗi hệ thống.
     */
    @PatchMapping("/{answerId}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerResponse> toggleAnswerStatus(@PathVariable Integer answerId, @RequestParam boolean newStatus) {
        try {
            AnswerResponse response = answerService.toggleAnswerStatus(answerId, newStatus);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AnswerResponse(null, null, e.getMessage(), null, null, null));
        } catch (Exception e) {
            // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AnswerResponse(null, null, "Lỗi hệ thống khi thay đổi trạng thái.", null, null, null));
        }
    }

    /**
     * Endpoint để xóa mềm (soft delete) một câu trả lời.
     * Câu trả lời sẽ được đánh dấu là isDeleted = true và isActive = false.
     * Yêu cầu quyền ADMIN.
     * @param answerId ID của câu trả lời.
     * @return ResponseEntity với HttpStatus.NO_CONTENT nếu thành công.
     * Trả về HttpStatus.NOT_FOUND nếu không tìm thấy câu trả lời.
     * Trả về HttpStatus.BAD_REQUEST nếu câu trả lời đã bị xóa mềm trước đó.
     * Trả về HttpStatus.INTERNAL_SERVER_ERROR cho các lỗi hệ thống.
     */
    @DeleteMapping("/{answerId}/soft")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> softDeleteAnswer(@PathVariable Integer answerId) {
        try {
            answerService.softDeleteAnswer(answerId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            // Phân biệt NOT_FOUND và BAD_REQUEST (đã bị xóa mềm)
            if (e.getMessage().contains("không tìm thấy") || e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).build();
            } else { // "Câu trả lời đã bị xóa mềm trước đó."
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).build();
            }
        } catch (Exception e) {
            // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    /**
     * Endpoint để khôi phục (restore) một câu trả lời đã bị xóa mềm.
     * Câu trả lời sẽ được đánh dấu là isDeleted = false.
     * Yêu cầu quyền ADMIN.
     * @param answerId ID của câu trả lời.
     * @return ResponseEntity với AnswerResponse đã khôi phục.
     * Trả về HttpStatus.OK nếu thành công.
     * Trả về HttpStatus.NOT_FOUND nếu không tìm thấy câu trả lời.
     * Trả về HttpStatus.BAD_REQUEST nếu câu trả lời không bị xóa mềm.
     * Trả về HttpStatus.INTERNAL_SERVER_ERROR cho các lỗi hệ thống.
     */
    @PatchMapping("/{answerId}/restore")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<AnswerResponse> restoreAnswer(@PathVariable Integer answerId) {
        try {
            AnswerResponse response = answerService.restoreAnswer(answerId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            // Phân biệt NOT_FOUND và BAD_REQUEST (không bị xóa mềm)
            if (e.getMessage().contains("không tìm thấy") || e.getMessage().contains("not found")) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new AnswerResponse(null, null, e.getMessage(), null, null, null));
            } else { // "Câu trả lời không bị xóa mềm, không cần khôi phục."
                return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(new AnswerResponse(null, null, e.getMessage(), null, null, null));
            }
        } catch (Exception e) {
            // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new AnswerResponse(null, null, "Lỗi hệ thống khi khôi phục câu trả lời.", null, null, null));
        }
    }

    /**
     * Endpoint để tìm kiếm câu trả lời với các tiêu chí và phân trang.
     * Yêu cầu quyền ADMIN.
     * @param request DTO chứa tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return ResponseEntity với Page của AnswerResponse.
     * Trả về HttpStatus.OK nếu thành công.
     * Trả về HttpStatus.BAD_REQUEST nếu request tìm kiếm không hợp lệ.
     * Trả về HttpStatus.INTERNAL_SERVER_ERROR cho các lỗi hệ thống.
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<AnswerResponse>> searchAnswers(@RequestBody AnswerSearchRequest request) {
        try {
            Page<AnswerResponse> responses = answerService.searchAnswers(request);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(Page.empty());
        } catch (Exception e) {
            // Log the exception for debugging
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(Page.empty());
        }
    }
}