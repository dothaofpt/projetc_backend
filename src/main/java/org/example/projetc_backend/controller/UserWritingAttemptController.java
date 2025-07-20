package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.UserWritingAttemptRequest;
import org.example.projetc_backend.dto.UserWritingAttemptResponse;
import org.example.projetc_backend.dto.UserWritingAttemptSearchRequest;
import org.example.projetc_backend.service.UserWritingAttemptService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/writing-attempts")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class UserWritingAttemptController {

    private static final Logger logger = LoggerFactory.getLogger(UserWritingAttemptController.class);

    private final UserWritingAttemptService userWritingAttemptService;

    public UserWritingAttemptController(UserWritingAttemptService userWritingAttemptService) {
        this.userWritingAttemptService = userWritingAttemptService;
    }

    /**
     * Lưu một lần thử viết mới của người dùng.
     * @param request DTO chứa thông tin lần thử viết. (Không còn các trường feedback, điểm từ frontend)
     * @return ResponseEntity với UserWritingAttemptResponse của lần thử đã lưu.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserWritingAttemptResponse> saveWritingAttempt(@Valid @RequestBody UserWritingAttemptRequest request,
                                                                         HttpServletRequest httpRequest) {
        logger.info("Received POST request to /api/writing-attempts from IP: {}. Request body: {}",
                httpRequest.getRemoteAddr(), request);
        try {
            UserWritingAttemptResponse response = userWritingAttemptService.saveWritingAttempt(request);
            logger.info("Successfully saved writing attempt with ID: {}. User ID: {}", response.attemptId(), response.userId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.warn("400 Bad Request for POST /api/writing-attempts from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage());
            logger.debug("Details for 400 error on POST /api/writing-attempts: Request body: {}", request);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("500 Internal Server Error for POST /api/writing-attempts from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage(), e);
            logger.error("Details for 500 error on POST /api/writing-attempts: Request body: {}", request);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Cập nhật một lần thử viết hiện có của người dùng.
     * Chỉ ADMIN mới có quyền thực hiện.
     * @param attemptId ID của lần thử viết cần cập nhật.
     * @param request DTO chứa thông tin cập nhật lần thử viết. (Không còn các trường feedback, điểm từ frontend)
     * @return ResponseEntity với UserWritingAttemptResponse của lần thử đã cập nhật.
     */
    @PutMapping("/{attemptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserWritingAttemptResponse> updateWritingAttempt(
            @PathVariable Integer attemptId,
            @Valid @RequestBody UserWritingAttemptRequest request,
            HttpServletRequest httpRequest) {
        logger.info("Received PUT request to /api/writing-attempts/{} from IP: {}. Request body: {}",
                attemptId, httpRequest.getRemoteAddr(), request);
        try {
            UserWritingAttemptResponse response = userWritingAttemptService.updateWritingAttempt(attemptId, request);
            logger.info("Successfully updated writing attempt with ID: {}", attemptId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("400 Bad Request/404 Not Found for PUT /api/writing-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("500 Internal Server Error for PUT /api/writing-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy thông tin chi tiết một lần thử viết theo ID.
     * ADMIN có thể xem bất kỳ lần thử nào. USER chỉ có thể xem lần thử của chính mình.
     * @param attemptId ID của lần thử viết.
     * @return ResponseEntity với UserWritingAttemptResponse. (Có các trường mới từ PracticeActivity, feedback, điểm)
     */
    @GetMapping("/{attemptId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Tạm thời cho phép USER, cần thêm logic kiểm tra sở hữu
    public ResponseEntity<UserWritingAttemptResponse> getWritingAttemptById(@PathVariable Integer attemptId,
                                                                            HttpServletRequest httpRequest) {
        logger.info("Received GET request to /api/writing-attempts/{} from IP: {}", attemptId, httpRequest.getRemoteAddr());
        try {
            UserWritingAttemptResponse response = userWritingAttemptService.getWritingAttemptById(attemptId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        // Thêm bắt IllegalArgumentException cho trường hợp not found rõ ràng hơn
        catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for GET /api/writing-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        catch (Exception e) {
            logger.error("500 Internal Server Error for GET /api/writing-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy tất cả các lần thử viết của một người dùng cụ thể.
     * ADMIN có thể xem của bất kỳ người dùng nào. USER chỉ có thể xem của chính mình.
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách UserWritingAttemptResponse. (Có các trường mới từ PracticeActivity, feedback, điểm)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or authentication.principal.id == #userId")
    public ResponseEntity<List<UserWritingAttemptResponse>> getWritingAttemptsByUser(@PathVariable Integer userId,
                                                                                     HttpServletRequest httpRequest) {
        logger.info("Received GET request to /api/writing-attempts/user/{} from IP: {}", userId, httpRequest.getRemoteAddr());
        try {
            List<UserWritingAttemptResponse> responses = userWritingAttemptService.getWritingAttemptsByUser(userId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        }
        // Thêm bắt IllegalArgumentException cho trường hợp not found rõ ràng hơn
        catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for GET /api/writing-attempts/user/{} from IP: {}. Error: {}",
                    userId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        catch (Exception e) {
            logger.error("500 Internal Server Error for GET /api/writing-attempts/user/{} from IP: {}. Error: {}",
                    userId, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy tất cả các lần thử viết cho một hoạt động luyện tập cụ thể.
     * ADMIN có quyền xem. Có thể mở public nếu cần cho mục đích thống kê hoặc hiển thị.
     * @param practiceActivityId ID của hoạt động luyện tập.
     * @return ResponseEntity với danh sách UserWritingAttemptResponse. (Có các trường mới từ PracticeActivity, feedback, điểm)
     */
    @GetMapping("/practice-activity/{practiceActivityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<UserWritingAttemptResponse>> getWritingAttemptsByPracticeActivity(@PathVariable Integer practiceActivityId,
                                                                                                 HttpServletRequest httpRequest) {
        logger.info("Received GET request to /api/writing-attempts/practice-activity/{} from IP: {}", practiceActivityId, httpRequest.getRemoteAddr());
        try {
            List<UserWritingAttemptResponse> responses = userWritingAttemptService.getWritingAttemptsByPracticeActivity(practiceActivityId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        }
        // Thêm bắt IllegalArgumentException cho trường hợp not found rõ ràng hơn
        catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for GET /api/writing-attempts/practice-activity/{} from IP: {}. Error: {}",
                    practiceActivityId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        catch (Exception e) {
            logger.error("500 Internal Server Error for GET /api/writing-attempts/practice-activity/{} from IP: {}. Error: {}",
                    practiceActivityId, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Tìm kiếm và phân trang các lần thử viết của người dùng.
     * Cho phép lọc theo userId, practiceActivityId, minOverallScore, maxOverallScore, và phân trang.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param userId ID người dùng để lọc (tùy chọn).
     * @param practiceActivityId ID hoạt động luyện tập để lọc (tùy chọn).
     * @param minOverallScore Điểm tổng thể tối thiểu (tùy chọn).
     * @param maxOverallScore Điểm tổng thể tối đa (tùy chọn).
     * @param page Số trang (mặc định là 0).
     * @param size Kích thước trang (mặc định là 10).
     * @return ResponseEntity chứa Page của UserWritingAttemptResponse. (Có các trường mới từ PracticeActivity, feedback, điểm)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserWritingAttemptResponse>> searchWritingAttempts(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer practiceActivityId,
            @RequestParam(required = false) Integer minOverallScore,
            @RequestParam(required = false) Integer maxOverallScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        logger.info("Received GET request to /api/writing-attempts/search from IP: {}. Params: userId={}, practiceActivityId={}, minOverallScore={}, maxOverallScore={}, page={}, size={}",
                httpRequest.getRemoteAddr(), userId, practiceActivityId, minOverallScore, maxOverallScore, page, size);
        try {
            UserWritingAttemptSearchRequest searchRequest = UserWritingAttemptSearchRequest.builder()
                    .userId(userId)
                    .practiceActivityId(practiceActivityId)
                    .minOverallScore(minOverallScore)
                    .maxOverallScore(maxOverallScore)
                    .page(page)
                    .size(size)
                    .build();

            Page<UserWritingAttemptResponse> responsePage = userWritingAttemptService.searchAndPaginateWritingAttempts(searchRequest);
            logger.info("Successfully retrieved {} writing attempts (page {} of {}) matching criteria.",
                    responsePage.getNumberOfElements(), responsePage.getNumber(), responsePage.getTotalPages());
            return new ResponseEntity<>(responsePage, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("400 Bad Request for GET /api/writing-attempts/search from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("500 Internal Server Error for GET /api/writing-attempts/search from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Xóa một lần thử viết.
     * Chỉ ADMIN mới có quyền.
     * @param attemptId ID của lần thử viết cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{attemptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteWritingAttempt(@PathVariable Integer attemptId,
                                                     HttpServletRequest httpRequest) {
        logger.info("Received DELETE request to /api/writing-attempts/{} from IP: {}", attemptId, httpRequest.getRemoteAddr());
        try {
            userWritingAttemptService.deleteWritingAttempt(attemptId);
            logger.info("Successfully deleted writing attempt with ID: {}", attemptId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        // Thêm bắt IllegalArgumentException cho trường hợp not found rõ ràng hơn
        catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for DELETE /api/writing-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        catch (Exception e) {
            logger.error("500 Internal Server Error for DELETE /api/writing-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}