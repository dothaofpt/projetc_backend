package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.AnswerRequest;
import org.example.projetc_backend.dto.AnswerResponse;
import org.example.projetc_backend.service.AnswerService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/answers")
@CrossOrigin(origins = "*")
public class AnswerController {

    private final AnswerService answerService;

    public AnswerController(AnswerService answerService) {
        this.answerService = answerService;
    }

    @PostMapping
    public ResponseEntity<AnswerResponse> createAnswer(@RequestBody AnswerRequest request) {
        AnswerResponse response = answerService.createAnswer(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{answerId}")
    public ResponseEntity<AnswerResponse> getAnswerById(@PathVariable Integer answerId) {
        AnswerResponse response = answerService.getAnswerById(answerId);
        return ResponseEntity.ok(response);
    }

    // Endpoint này (getAnswersByQuestionId) chỉ trả về các câu trả lời đang ACTIVE.
    @GetMapping("/question/{questionId}")
    public ResponseEntity<List<AnswerResponse>> getAnswersByQuestionId(@PathVariable Integer questionId) {
        List<AnswerResponse> responses = answerService.getAnswersByQuestionId(questionId);
        return ResponseEntity.ok(responses);
    }

    /**
     * Lấy TẤT CẢ các câu trả lời (active và inactive) cho một câu hỏi cụ thể,
     * NHƯNG KHÔNG BAO GỒM CÁC CÂU ĐÃ BỊ XÓA MỀM (isDeleted = true).
     * Dùng cho trang quản trị viên hiển thị đầy đủ trạng thái.
     *
     * @param questionId ID của câu hỏi.
     * @return Danh sách AnswerResponse của tất cả các câu trả lời chưa bị xóa mềm.
     */
    @GetMapping("/question/{questionId}/all")
    public ResponseEntity<List<AnswerResponse>> getAllAnswersForAdminByQuestionId(@PathVariable Integer questionId) {
        List<AnswerResponse> responses = answerService.getAllAnswersForAdminByQuestionId(questionId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{answerId}")
    public ResponseEntity<AnswerResponse> updateAnswer(@PathVariable Integer answerId, @RequestBody AnswerRequest request) {
        AnswerResponse response = answerService.updateAnswer(answerId, request);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để bật/tắt trạng thái (active/inactive) của một câu trả lời.
     * Khi newStatus là false, câu trả lời sẽ bị 'disable'.
     *
     * @param answerId ID của câu trả lời.
     * @param newStatus Trạng thái mới (true = active, false = inactive/disabled).
     * @return AnswerResponse với trạng thái được cập nhật.
     */
    @PatchMapping("/{answerId}/status")
    public ResponseEntity<AnswerResponse> toggleAnswerStatus(@PathVariable Integer answerId, @RequestParam boolean newStatus) {
        AnswerResponse response = answerService.toggleAnswerStatus(answerId, newStatus);
        return ResponseEntity.ok(response);
    }

    /**
     * Endpoint để "xóa mềm" một câu trả lời bằng cách đặt trạng thái isDeleted thành true.
     * Câu trả lời sẽ không bị xóa khỏi cơ sở dữ liệu.
     *
     * @param answerId ID của câu trả lời cần xóa mềm.
     * @return ResponseEntity.noContent() nếu thành công.
     */
    @DeleteMapping("/{answerId}")
    public ResponseEntity<Void> softDeleteAnswer(@PathVariable Integer answerId) {
        answerService.softDeleteAnswer(answerId);
        return ResponseEntity.noContent().build();
    }
}