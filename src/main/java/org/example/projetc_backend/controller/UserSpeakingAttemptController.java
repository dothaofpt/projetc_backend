package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.UserSpeakingAttemptRequest;
import org.example.projetc_backend.dto.UserSpeakingAttemptResponse;
import org.example.projetc_backend.dto.UserSpeakingAttemptSearchRequest; // Import DTO tìm kiếm mới
import org.example.projetc_backend.service.UserSpeakingAttemptService;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

// Thêm các import cần thiết cho Logger
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest; // Import để lấy thông tin request

@RestController
@RequestMapping("/api/speaking-attempts")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class UserSpeakingAttemptController {

    // Khởi tạo Logger cho class này
    private static final Logger logger = LoggerFactory.getLogger(UserSpeakingAttemptController.class);

    private final UserSpeakingAttemptService userSpeakingAttemptService;

    public UserSpeakingAttemptController(UserSpeakingAttemptService userSpeakingAttemptService) {
        this.userSpeakingAttemptService = userSpeakingAttemptService;
    }

    /**
     * Lưu một lần thử nói mới của người dùng.
     * Người dùng có thể tự lưu lần thử của mình. ADMIN cũng có thể lưu.
     * @param request DTO chứa thông tin lần thử nói.
     * @return ResponseEntity với UserSpeakingAttemptResponse của lần thử đã lưu.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserSpeakingAttemptResponse> saveSpeakingAttempt(@Valid @RequestBody UserSpeakingAttemptRequest request,
                                                                           HttpServletRequest httpRequest) { // Thêm HttpServletRequest
        logger.info("Received POST request to /api/speaking-attempts from IP: {}. Request body: {}",
                httpRequest.getRemoteAddr(), request); // Log request body
        try {
            UserSpeakingAttemptResponse response = userSpeakingAttemptService.saveSpeakingAttempt(request);
            logger.info("Successfully saved speaking attempt with ID: {}. User ID: {}", response.attemptId(), response.userId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.warn("400 Bad Request for POST /api/speaking-attempts from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage());
            logger.debug("Details for 400 error on POST /api/speaking-attempts: Request body: {}", request); // Thêm debug cho request body
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("500 Internal Server Error for POST /api/speaking-attempts from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage(), e); // Log full stack trace
            logger.error("Details for 500 error on POST /api/speaking-attempts: Request body: {}", request); // Log request body
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Cập nhật một lần thử nói hiện có của người dùng.
     * Chỉ ADMIN mới có quyền thực hiện.
     * @param attemptId ID của lần thử nói cần cập nhật.
     * @param request DTO chứa thông tin cập nhật lần thử nói.
     * @return ResponseEntity với UserSpeakingAttemptResponse của lần thử đã cập nhật.
     */
    @PutMapping("/{attemptId}")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có quyền cập nhật dữ liệu này
    public ResponseEntity<UserSpeakingAttemptResponse> updateSpeakingAttempt(
            @PathVariable Integer attemptId,
            @Valid @RequestBody UserSpeakingAttemptRequest request,
            HttpServletRequest httpRequest) { // Thêm HttpServletRequest
        logger.info("Received PUT request to /api/speaking-attempts/{} from IP: {}. Request body: {}",
                attemptId, httpRequest.getRemoteAddr(), request); // Log request body
        try {
            UserSpeakingAttemptResponse response = userSpeakingAttemptService.updateSpeakingAttempt(attemptId, request);
            logger.info("Successfully updated speaking attempt with ID: {}", attemptId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("400 Bad Request/404 Not Found for PUT /api/speaking-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Có thể là 404 nếu không tìm thấy ID
        } catch (Exception e) {
            logger.error("500 Internal Server Error for PUT /api/speaking-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy thông tin chi tiết một lần thử nói theo ID.
     * ADMIN có thể xem bất kỳ lần thử nào. USER chỉ có thể xem lần thử của chính mình.
     * (Cần triển khai logic kiểm tra quyền sở hữu trong SecurityConfig hoặc riêng biệt)
     * Ví dụ cho USER: @PreAuthorize("hasRole('ADMIN') or @userSecurityService.isOwnerOfSpeakingAttempt(#attemptId)")
     * @param attemptId ID của lần thử nói.
     * @return ResponseEntity với UserSpeakingAttemptResponse.
     */
    @GetMapping("/{attemptId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Tạm thời cho phép USER, cần thêm logic kiểm tra sở hữu
    public ResponseEntity<UserSpeakingAttemptResponse> getSpeakingAttemptById(@PathVariable Integer attemptId,
                                                                              HttpServletRequest httpRequest) { // Thêm HttpServletRequest
        logger.info("Received GET request to /api/speaking-attempts/{} from IP: {}", attemptId, httpRequest.getRemoteAddr());
        try {
            UserSpeakingAttemptResponse response = userSpeakingAttemptService.getSpeakingAttemptById(attemptId);
            logger.info("Successfully retrieved speaking attempt with ID: {}", attemptId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for GET /api/speaking-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Lấy tất cả các lần thử nói của một người dùng cụ thể.
     * ADMIN có thể xem của bất kỳ người dùng nào. USER chỉ có thể xem của chính mình.
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách UserSpeakingAttemptResponse.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #userId")
    public ResponseEntity<List<UserSpeakingAttemptResponse>> getSpeakingAttemptsByUser(@PathVariable Integer userId,
                                                                                       HttpServletRequest httpRequest) { // Thêm HttpServletRequest
        logger.info("Received GET request to /api/speaking-attempts/user/{} from IP: {}", userId, httpRequest.getRemoteAddr());
        try {
            List<UserSpeakingAttemptResponse> responses = userSpeakingAttemptService.getSpeakingAttemptsByUser(userId);
            logger.info("Successfully retrieved {} speaking attempts for user ID: {}", responses.size(), userId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for GET /api/speaking-attempts/user/{} from IP: {}. Error: {}",
                    userId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Lấy tất cả các lần thử nói cho một hoạt động luyện tập cụ thể.
     * ADMIN có quyền xem. Có thể mở public nếu cần cho mục đích thống kê hoặc hiển thị.
     * @param practiceActivityId ID của hoạt động luyện tập.
     * @return ResponseEntity với danh sách UserSpeakingAttemptResponse.
     */
    @GetMapping("/practice-activity/{practiceActivityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')") // Có thể truy cập công khai nếu là hoạt động luyện tập
    public ResponseEntity<List<UserSpeakingAttemptResponse>> getSpeakingAttemptsByPracticeActivity(@PathVariable Integer practiceActivityId,
                                                                                                   HttpServletRequest httpRequest) { // Thêm HttpServletRequest
        logger.info("Received GET request to /api/speaking-attempts/practice-activity/{} from IP: {}", practiceActivityId, httpRequest.getRemoteAddr());
        try {
            List<UserSpeakingAttemptResponse> responses = userSpeakingAttemptService.getSpeakingAttemptsByPracticeActivity(practiceActivityId);
            logger.info("Successfully retrieved {} speaking attempts for practice activity ID: {}", responses.size(), practiceActivityId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for GET /api/speaking-attempts/practice-activity/{} from IP: {}. Error: {}",
                    practiceActivityId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }



    /**
     * Tìm kiếm và phân trang các lần thử nói của người dùng.
     * Cho phép lọc theo userId, practiceActivityId, minOverallScore, maxOverallScore, và phân trang.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param userId ID người dùng để lọc (tùy chọn).
     * @param practiceActivityId ID hoạt động luyện tập để lọc (tùy chọn).
     * @param minOverallScore Điểm tổng thể tối thiểu (tùy chọn).
     * @param maxOverallScore Điểm tổng thể tối đa (tùy chọn).
     * @param page Số trang (mặc định là 0).
     * @param size Kích thước trang (mặc định là 10).
     * @return ResponseEntity chứa Page của UserSpeakingAttemptResponse.
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')") // Chỉ ADMIN mới có thể tìm kiếm tất cả
    public ResponseEntity<Page<UserSpeakingAttemptResponse>> searchSpeakingAttempts(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer practiceActivityId,
            @RequestParam(required = false) Integer minOverallScore,
            @RequestParam(required = false) Integer maxOverallScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        logger.info("Received GET request to /api/speaking-attempts/search from IP: {}. Params: userId={}, practiceActivityId={}, minOverallScore={}, maxOverallScore={}, page={}, size={}",
                httpRequest.getRemoteAddr(), userId, practiceActivityId, minOverallScore, maxOverallScore, page, size);
        try {
            UserSpeakingAttemptSearchRequest searchRequest = UserSpeakingAttemptSearchRequest.builder()
                    .userId(userId)
                    .practiceActivityId(practiceActivityId)
                    .minOverallScore(minOverallScore)
                    .maxOverallScore(maxOverallScore)
                    .page(page)
                    .size(size)
                    .build();

            Page<UserSpeakingAttemptResponse> responsePage = userSpeakingAttemptService.searchAndPaginateSpeakingAttempts(searchRequest);
            logger.info("Successfully retrieved {} speaking attempts (page {} of {}) matching criteria.",
                    responsePage.getNumberOfElements(), responsePage.getNumber(), responsePage.getTotalPages());
            return new ResponseEntity<>(responsePage, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("400 Bad Request for GET /api/speaking-attempts/search from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("500 Internal Server Error for GET /api/speaking-attempts/search from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }



    /**
     * Xóa một lần thử nói.
     * Chỉ ADMIN mới có quyền.
     * @param attemptId ID của lần thử nói cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{attemptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteSpeakingAttempt(@PathVariable Integer attemptId,
                                                      HttpServletRequest httpRequest) { // Thêm HttpServletRequest
        logger.info("Received DELETE request to /api/speaking-attempts/{} from IP: {}", attemptId, httpRequest.getRemoteAddr());
        try {
            userSpeakingAttemptService.deleteSpeakingAttempt(attemptId);
            logger.info("Successfully deleted speaking attempt with ID: {}", attemptId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for DELETE /api/speaking-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}