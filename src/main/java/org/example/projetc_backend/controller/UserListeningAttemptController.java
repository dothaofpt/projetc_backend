package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.UserListeningAttemptRequest;
import org.example.projetc_backend.dto.UserListeningAttemptResponse;
import org.example.projetc_backend.dto.UserListeningAttemptSearchRequest;
import org.example.projetc_backend.service.UserListeningAttemptService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.data.domain.Page;

import jakarta.validation.Valid;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import jakarta.servlet.http.HttpServletRequest;

@RestController
@RequestMapping("/api/listening-attempts")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class UserListeningAttemptController {

    private static final Logger logger = LoggerFactory.getLogger(UserListeningAttemptController.class);

    private final UserListeningAttemptService userListeningAttemptService;

    public UserListeningAttemptController(UserListeningAttemptService userListeningAttemptService) {
        this.userListeningAttemptService = userListeningAttemptService;
    }

    /**
     * Lưu một lần thử nghe của người dùng.
     * Người dùng có thể tự gửi kết quả của mình.
     * @param request Dữ liệu lần thử nghe. (Không còn accuracyScore trong request)
     * @return ResponseEntity với UserListeningAttemptResponse đã lưu.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<UserListeningAttemptResponse> saveListeningAttempt(@Valid @RequestBody UserListeningAttemptRequest request,
                                                                             HttpServletRequest httpRequest) {
        logger.info("Received POST request to /api/listening-attempts from IP: {}. Request body: {}",
                httpRequest.getRemoteAddr(), request);
        try {
            UserListeningAttemptResponse response = userListeningAttemptService.saveListeningAttempt(request);
            logger.info("Successfully saved listening attempt with ID: {}. User ID: {}", response.attemptId(), response.userId());
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            logger.warn("400 Bad Request for POST /api/listening-attempts from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage());
            logger.debug("Details for 400 error on POST /api/listening-attempts: Request body: {}", request);
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("500 Internal Server Error for POST /api/listening-attempts from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage(), e);
            logger.error("Details for 500 error on POST /api/listening-attempts: Request body: {}", request);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Cập nhật một lần thử nghe hiện có của người dùng.
     * Chỉ ADMIN mới có quyền thực hiện.
     * @param attemptId ID của lần thử nghe cần cập nhật.
     * @param request DTO chứa thông tin cập nhật lần thử nghe. (Không còn accuracyScore trong request)
     * @return ResponseEntity với UserListeningAttemptResponse của lần thử đã cập nhật.
     */
    @PutMapping("/{attemptId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserListeningAttemptResponse> updateListeningAttempt(
            @PathVariable Integer attemptId,
            @Valid @RequestBody UserListeningAttemptRequest request,
            HttpServletRequest httpRequest) {
        logger.info("Received PUT request to /api/listening-attempts/{} from IP: {}. Request body: {}",
                attemptId, httpRequest.getRemoteAddr(), request);
        try {
            UserListeningAttemptResponse response = userListeningAttemptService.updateListeningAttempt(attemptId, request);
            logger.info("Successfully updated listening attempt with ID: {}", attemptId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("400 Bad Request/404 Not Found for PUT /api/listening-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("500 Internal Server Error for PUT /api/listening-attempts/{} from IP: {}. Error: {}",
                    attemptId, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy thông tin một lần thử nghe bằng ID.
     * ADMIN có thể xem bất kỳ lần thử nào. USER chỉ có thể xem lần thử của chính mình.
     * @param id ID của lần thử nghe.
     * @return ResponseEntity với UserListeningAttemptResponse. (Có các trường audioUrl, actualTranscriptText)
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN') or @userSecurity.isOwnerOfListeningAttempt(#id)")
    public ResponseEntity<UserListeningAttemptResponse> getListeningAttemptById(@PathVariable Integer id,
                                                                                HttpServletRequest httpRequest) {
        logger.info("Received GET request to /api/listening-attempts/{} from IP: {}", id, httpRequest.getRemoteAddr());
        try {
            UserListeningAttemptResponse response = userListeningAttemptService.getListeningAttemptById(id);
            logger.info("Successfully retrieved listening attempt with ID: {}", id);
            return new ResponseEntity<>(response, HttpStatus.OK);
        }
        // Thêm bắt IllegalArgumentException cho trường hợp not found rõ ràng hơn
        catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for GET /api/listening-attempts/{} from IP: {}. Error: {}",
                    id, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        catch (Exception e) {
            logger.error("500 Internal Server Error for GET /api/listening-attempts/{} from IP: {}. Error: {}",
                    id, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy tất cả các lần thử nghe của một người dùng cụ thể.
     * ADMIN có thể xem của bất kỳ ai. USER chỉ có thể xem của chính mình.
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách UserListeningAttemptResponse. (Có các trường audioUrl, actualTranscriptText)
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public ResponseEntity<List<UserListeningAttemptResponse>> getListeningAttemptsByUser(@PathVariable Integer userId,
                                                                                         HttpServletRequest httpRequest) {
        logger.info("Received GET request to /api/listening-attempts/user/{} from IP: {}", userId, httpRequest.getRemoteAddr());
        try {
            List<UserListeningAttemptResponse> responses = userListeningAttemptService.getListeningAttemptsByUser(userId);
            logger.info("Successfully retrieved {} listening attempts for user ID: {}", responses.size(), userId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        }
        // Thêm bắt IllegalArgumentException cho trường hợp not found rõ ràng hơn
        catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for GET /api/listening-attempts/user/{} from IP: {}. Error: {}",
                    userId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        catch (Exception e) {
            logger.error("500 Internal Server Error for GET /api/listening-attempts/user/{} from IP: {}. Error: {}",
                    userId, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy tất cả các lần thử nghe cho một hoạt động luyện tập cụ thể.
     * ADMIN có quyền xem. Có thể mở public nếu cần cho mục đích thống kê hoặc hiển thị.
     * @param practiceActivityId ID của hoạt động luyện tập.
     * @return ResponseEntity với danh sách UserListeningAttemptResponse. (Có các trường audioUrl, actualTranscriptText)
     */
    @GetMapping("/practice-activity/{practiceActivityId}")
    @PreAuthorize("hasRole('ADMIN') or hasRole('USER')")
    public ResponseEntity<List<UserListeningAttemptResponse>> getListeningAttemptsByPracticeActivity(@PathVariable Integer practiceActivityId,
                                                                                                     HttpServletRequest httpRequest) {
        logger.info("Received GET request to /api/listening-attempts/practice-activity/{} from IP: {}", practiceActivityId, httpRequest.getRemoteAddr());
        try {
            List<UserListeningAttemptResponse> responses = userListeningAttemptService.getListeningAttemptsByPracticeActivity(practiceActivityId);
            logger.info("Successfully retrieved {} listening attempts for practice activity ID: {}", responses.size(), practiceActivityId);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        }
        // Thêm bắt IllegalArgumentException cho trường hợp not found rõ ràng hơn
        catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for GET /api/listening-attempts/practice-activity/{} from IP: {}. Error: {}",
                    practiceActivityId, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        catch (Exception e) {
            logger.error("500 Internal Server Error for GET /api/listening-attempts/practice-activity/{} from IP: {}. Error: {}",
                    practiceActivityId, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Tìm kiếm và phân trang các lần thử nghe của người dùng.
     * Cho phép lọc theo userId, practiceActivityId, minAccuracyScore, maxAccuracyScore, và phân trang.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param userId ID người dùng để lọc (tùy chọn).
     * @param practiceActivityId ID hoạt động luyện tập để lọc (tùy chọn).
     * @param minAccuracyScore Điểm chính xác tối thiểu (tùy chọn).
     * @param maxAccuracyScore Điểm chính xác tối đa (tùy chọn).
     * @param page Số trang (mặc định là 0).
     * @param size Kích thước trang (mặc định là 10).
     * @return ResponseEntity chứa Page của UserListeningAttemptResponse. (Có các trường audioUrl, actualTranscriptText)
     */
    @GetMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<UserListeningAttemptResponse>> searchListeningAttempts(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Integer practiceActivityId,
            @RequestParam(required = false) Integer minAccuracyScore,
            @RequestParam(required = false) Integer maxAccuracyScore,
            @RequestParam(defaultValue = "0") int page,
            @RequestParam(defaultValue = "10") int size,
            HttpServletRequest httpRequest) {
        logger.info("Received GET request to /api/listening-attempts/search from IP: {}. Params: userId={}, practiceActivityId={}, minAccuracyScore={}, maxAccuracyScore={}, page={}, size={}",
                httpRequest.getRemoteAddr(), userId, practiceActivityId, minAccuracyScore, maxAccuracyScore, page, size);
        try {
            UserListeningAttemptSearchRequest searchRequest = UserListeningAttemptSearchRequest.builder()
                    .userId(userId)
                    .practiceActivityId(practiceActivityId)
                    .minAccuracyScore(minAccuracyScore)
                    .maxAccuracyScore(maxAccuracyScore)
                    .page(page)
                    .size(size)
                    .build();

            Page<UserListeningAttemptResponse> responsePage = userListeningAttemptService.searchAndPaginateListeningAttempts(searchRequest);
            logger.info("Successfully retrieved {} listening attempts (page {} of {}) matching criteria.",
                    responsePage.getNumberOfElements(), responsePage.getNumber(), responsePage.getTotalPages());
            return new ResponseEntity<>(responsePage, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            logger.warn("400 Bad Request for GET /api/listening-attempts/search from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            logger.error("500 Internal Server Error for GET /api/listening-attempts/search from IP: {}. Error: {}",
                    httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }


    /**
     * Xóa một lần thử nghe.
     * Chỉ ADMIN mới có quyền xóa.
     * @param id ID của lần thử nghe cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteListeningAttempt(@PathVariable Integer id,
                                                       HttpServletRequest httpRequest) {
        logger.info("Received DELETE request to /api/listening-attempts/{} from IP: {}", id, httpRequest.getRemoteAddr());
        try {
            userListeningAttemptService.deleteListeningAttempt(id);
            logger.info("Successfully deleted listening attempt with ID: {}", id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        // Thêm bắt IllegalArgumentException cho trường hợp not found rõ ràng hơn
        catch (IllegalArgumentException e) {
            logger.warn("404 Not Found for DELETE /api/listening-attempts/{} from IP: {}. Error: {}",
                    id, httpRequest.getRemoteAddr(), e.getMessage());
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
        catch (Exception e) {
            logger.error("500 Internal Server Error for DELETE /api/listening-attempts/{} from IP: {}. Error: {}",
                    id, httpRequest.getRemoteAddr(), e.getMessage(), e);
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}