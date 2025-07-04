package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.UserUpdateRequest;
import org.example.projetc_backend.dto.UserResponse;
import org.example.projetc_backend.dto.UserSearchRequest;
import org.example.projetc_backend.dto.UserPageResponse;
import org.example.projetc_backend.dto.RegisterRequest;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.service.UserService;
import org.example.projetc_backend.service.AuthService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class UserController {

    private final UserService userService;
    private final AuthService authService;

    public UserController(UserService userService, AuthService authService) {
        this.userService = userService;
        this.authService = authService;
    }

    /**
     * Endpoint để lấy tất cả người dùng.
     * Endpoint Backend: GET /api/users
     * Chỉ ADMIN mới có quyền truy cập.
     * @return ResponseEntity chứa danh sách UserResponse DTOs.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<UserResponse>> getAllUsers() {
        try {
            List<UserResponse> users = userService.getAllUsers();
            return new ResponseEntity<>(users, HttpStatus.OK);
        } catch (Exception e) {
            System.err.println("Error fetching all users: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint để lấy thông tin người dùng theo ID.
     * Endpoint Backend: GET /api/users/{userId}
     * Cả USER và ADMIN đều có quyền. USER chỉ được xem thông tin của chính mình.
     * @param userId ID của người dùng.
     * @return ResponseEntity chứa UserResponse của người dùng hoặc 404 NOT FOUND.
     */
    @GetMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public ResponseEntity<UserResponse> getUserById(@PathVariable Integer userId) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Integer currentUserId = null;
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                currentUserId = ((User) authentication.getPrincipal()).getUserId();
            }

            if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) &&
                    !userId.equals(currentUserId)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            return userService.findById(userId)
                    .map(this::mapToUserResponse)
                    .map(userResponse -> new ResponseEntity<>(userResponse, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error getting user by ID: " + e.getMessage());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint để cập nhật thông tin người dùng.
     * Endpoint Backend: PUT /api/users/{userId}
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể cập nhật thông tin của chính mình.
     * @param userId ID của người dùng cần cập nhật.
     * @param request DTO chứa thông tin cập nhật.
     * @return ResponseEntity chứa UserResponse của người dùng đã cập nhật.
     */
    @PutMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public ResponseEntity<UserResponse> updateUser(@PathVariable Integer userId, @Valid @RequestBody UserUpdateRequest request) {
        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            Integer currentUserId = null;
            if (authentication != null && authentication.getPrincipal() instanceof User) {
                currentUserId = ((User) authentication.getPrincipal()).getUserId();
            }

            if (!authentication.getAuthorities().stream().anyMatch(a -> a.getAuthority().equals("ROLE_ADMIN")) &&
                    !userId.equals(currentUserId)) {
                return new ResponseEntity<>(HttpStatus.FORBIDDEN);
            }

            UserResponse response = userService.updateUser(userId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error updating user: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint để xóa người dùng.
     * Endpoint Backend: DELETE /api/users/{userId}
     * Chỉ ADMIN mới có quyền.
     * @param userId ID của người dùng cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{userId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteUser(@PathVariable Integer userId) {
        try {
            userService.deleteUser(userId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            System.err.println("Error deleting user: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint để tìm kiếm và phân trang người dùng.
     * Endpoint Backend: POST /api/users/search
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa các tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return ResponseEntity chứa UserPageResponse của trang kết quả.
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserPageResponse> searchUsers(@Valid @RequestBody UserSearchRequest request) {
        try {
            UserPageResponse response = userService.searchUsers(request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("Error searching users: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint để ADMIN tạo người dùng mới, bao gồm cả ADMIN khác.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa thông tin người dùng mới (bao gồm vai trò).
     * @return ResponseEntity với UserResponse của người dùng đã tạo.
     */
    @PostMapping("/admin-create")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> adminCreateUser(@Valid @RequestBody RegisterRequest request) {
        try {
            authService.register(request);
            Optional<User> createdUser = userService.findByUsername(request.username());
            if (createdUser.isPresent()) {
                return new ResponseEntity<>(mapToUserResponse(createdUser.get()), HttpStatus.CREATED);
            } else {
                return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IllegalArgumentException e) {
            System.err.println("Error creating user by admin: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        } catch (Exception e) {
            System.err.println("An unexpected error occurred: " + e.getMessage());
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Endpoint để lấy thông tin của người dùng hiện đang đăng nhập.
     * Yêu cầu người dùng phải được xác thực.
     * @return ResponseEntity với UserResponse của người dùng hiện tại hoặc 404 NOT FOUND nếu không tìm thấy.
     */
    @GetMapping("/current")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<UserResponse> getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

        if (authentication == null || !authentication.isAuthenticated() || "anonymousUser".equals(authentication.getPrincipal())) {
            return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
        }

        Object principal = authentication.getPrincipal();

        if (principal instanceof User) {
            User currentUser = (User) principal;
            return new ResponseEntity<>(mapToUserResponse(currentUser), HttpStatus.OK);
        } else if (principal instanceof org.springframework.security.core.userdetails.UserDetails) {
            String username = ((org.springframework.security.core.userdetails.UserDetails) principal).getUsername();
            return userService.findByUsername(username)
                    .map(this::mapToUserResponse)
                    .map(userResponse -> new ResponseEntity<>(userResponse, HttpStatus.OK))
                    .orElseGet(() -> new ResponseEntity<>(HttpStatus.NOT_FOUND));
        } else {
            System.err.println("Unexpected principal type in getCurrentUser: " + principal.getClass().getName());
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }

    /**
     * Phương thức helper để ánh xạ từ User entity sang UserResponse DTO.
     * @param user Đối tượng User entity.
     * @return UserResponse DTO tương ứng.
     */
    private UserResponse mapToUserResponse(User user) {
        if (user == null) {
            return null;
        }
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getRole()
        );
    }
}