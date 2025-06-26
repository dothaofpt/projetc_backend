// src/main/java/org/example/projetc_backend/controller/UserController.java

package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.UserUpdateRequest;
import org.example.projetc_backend.dto.UserResponse;
import org.example.projetc_backend.dto.UserSearchRequest;
import org.example.projetc_backend.dto.UserPageResponse;
import org.example.projetc_backend.dto.RegisterRequest; // <-- Thêm import này
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.service.UserService;
import org.example.projetc_backend.service.AuthService; // <-- Thêm import này
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;
import java.util.Optional;

@RestController
@RequestMapping("/api/users") // Vẫn giữ nguyên mapping này
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class UserController {

    private final UserService userService;
    private final AuthService authService; // <-- Tiêm AuthService vào đây

    public UserController(UserService userService, AuthService authService) { // <-- Cập nhật constructor
        this.userService = userService;
        this.authService = authService; // <-- Gán
    }

    // --- Các phương thức hiện có (getAllUsers, getUserByUsername, getUserByEmail, getUserById,
    // createUser (đã có), updateUser, deleteUser, searchUsers) vẫn giữ nguyên ---

    /**
     * Endpoint để ADMIN tạo người dùng mới, bao gồm cả ADMIN khác.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa thông tin người dùng mới (bao gồm vai trò).
     * @return ResponseEntity với UserResponse của người dùng đã tạo.
     */
    @PostMapping("/admin-create") // Endpoint mới, ví dụ: /api/users/admin-create
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<UserResponse> adminCreateUser(@Valid @RequestBody RegisterRequest request) {
        try {
            // AuthService.register sẽ xử lý logic kiểm tra allowAdminRegistration
            // UserResponse của bạn không có token, nên chúng ta chỉ trả về UserResponse.
            // Nếu bạn muốn trả về LoginResponse (có token) thì cần đổi kiểu trả về.
            authService.register(request); // Gọi service để tạo người dùng
            // Sau khi tạo, bạn có thể muốn lấy lại thông tin người dùng vừa tạo để trả về.
            // Hoặc đơn giản là trả về 201 Created mà không cần payload.
            // Tuy nhiên, để nhất quán với các response khác, chúng ta nên trả về UserResponse
            // của người dùng đã tạo. AuthService.register trả về LoginResponse,
            // nên cần một cách để lấy UserResponse hoặc thay đổi AuthService.register.
            // Để đơn giản, hãy giả định AuthService.register trả về LoginResponse,
            // bạn có thể lấy username từ request và tìm lại user để tạo UserResponse.

            // Cách tốt nhất là tạo một phương thức riêng trong AuthService/UserService
            // để chỉ tạo User và trả về User entity/UserResponse mà không tạo JWT token,
            // sau đó gọi phương thức đó ở đây.
            // Tạm thời, tôi sẽ gọi lại userService.findByUsername sau khi register thành công.

            // Một cách khác: AuthService.register có thể trả về UserResponse thay vì LoginResponse
            // khi nó được gọi từ một API admin-only.
            // Tuy nhiên, giữ nguyên AuthService.register trả về LoginResponse cho mục đích đơn giản.
            // Sau đó, bạn có thể tìm lại user vừa tạo để trả về UserResponse.

            // Giả định RegisterRequest luôn tạo User.
            // Nếu bạn muốn trả về LoginResponse cho người dùng mới này, hãy thay đổi ResponseEntity<UserResponse>
            // thành ResponseEntity<LoginResponse> và trả về response từ authService.register.
            // Ở đây, tôi sẽ trả về UserResponse bằng cách tìm lại user.

            Optional<User> createdUser = userService.findByUsername(request.username());
            if (createdUser.isPresent()) {
                return new ResponseEntity<>(mapToUserResponse(createdUser.get()), HttpStatus.CREATED);
            } else {
                // Rất khó xảy ra nếu register thành công
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

    // --- Phương thức mapToUserResponse (giữ nguyên) ---
    private UserResponse mapToUserResponse(User user) {
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