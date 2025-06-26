package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserUpdateRequest;
import org.example.projetc_backend.dto.UserResponse;
import org.example.projetc_backend.dto.UserSearchRequest; // <-- Import mới
import org.example.projetc_backend.dto.UserPageResponse;   // <-- Import mới
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.repository.UserRepository;
import org.springframework.data.domain.Page; // <-- Import mới
import org.springframework.data.domain.PageRequest; // <-- Import mới
import org.springframework.data.domain.Sort; // <-- Import mới
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Transactional(readOnly = true)
    public List<UserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public Optional<User> findByUsername(String username) {
        if (username == null || username.trim().isEmpty()) {
            throw new IllegalArgumentException("Username không được để trống.");
        }
        return userRepository.findByUsername(username);
    }

    @Transactional(readOnly = true)
    public Optional<User> findByEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email không được để trống.");
        }
        return userRepository.findByEmail(email);
    }

    @Transactional(readOnly = true)
    public Optional<User> findById(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        return userRepository.findById(userId);
    }

    @Transactional
    public UserResponse createUser(String username, String email, String password, String fullName, String avatarUrl, String role) {
        if (username == null || username.trim().isEmpty() ||
                email == null || email.trim().isEmpty() ||
                password == null || password.isEmpty()) {
            throw new IllegalArgumentException("Username, email, và password là bắt buộc và không được để trống.");
        }

        if (userRepository.existsByUsername(username)) {
            throw new IllegalArgumentException("Username đã tồn tại: " + username);
        }

        if (userRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("Email đã tồn tại: " + email);
        }

        User user = new User();
        user.setUsername(username);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(password));
        user.setFullName(fullName);
        user.setAvatarUrl(avatarUrl);

        try {
            // Chuyển đổi String role sang User.Role enum
            user.setRole(role != null && !role.trim().isEmpty() ? User.Role.valueOf(role.toUpperCase()) : User.Role.ROLE_USER);
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Vai trò không hợp lệ: " + role + ". Chỉ chấp nhận 'ROLE_ADMIN' hoặc 'ROLE_USER'.");
        }

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Transactional
    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        if (userId == null || request == null) {
            throw new IllegalArgumentException("User ID hoặc request không được để trống.");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        if (request.username() != null && !request.username().trim().isEmpty()) {
            if (userRepository.findByUsername(request.username())
                    .filter(existingUser -> !existingUser.getUserId().equals(userId))
                    .isPresent()) {
                throw new IllegalArgumentException("Username đã tồn tại: " + request.username());
            }
            user.setUsername(request.username().trim());
        }

        if (request.email() != null && !request.email().trim().isEmpty()) {
            if (userRepository.findByEmail(request.email())
                    .filter(existingUser -> !existingUser.getUserId().equals(userId))
                    .isPresent()) {
                throw new IllegalArgumentException("Email đã tồn tại: " + request.email());
            }
            user.setEmail(request.email().trim());
        }

        // Cập nhật password nếu được cung cấp (đã mã hóa)
        if (request.password() != null && !request.password().trim().isEmpty()) {
            user.setPassword(passwordEncoder.encode(request.password().trim()));
        }

        if (request.fullName() != null) {
            user.setFullName(request.fullName().trim().isEmpty() ? null : request.fullName().trim());
        }
        if (request.avatarUrl() != null) {
            user.setAvatarUrl(request.avatarUrl().trim().isEmpty() ? null : request.avatarUrl().trim());
        }

        // Cập nhật vai trò (đã là User.Role trong DTO)
        if (request.role() != null) { // Vì role là enum, không cần kiểm tra .trim().isEmpty()
            user.setRole(request.role());
        }

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    @Transactional
    public void deleteUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
        }
        userRepository.deleteById(userId);
    }



    /**
     * Tìm kiếm và phân trang người dùng dựa trên các tiêu chí tùy chọn.
     * @param request DTO chứa các tiêu chí tìm kiếm (username, email, fullName, role) và thông tin phân trang/sắp xếp.
     * @return Trang các UserResponse phù hợp với tiêu chí tìm kiếm.
     * @throws IllegalArgumentException Nếu Search request trống.
     */
    @Transactional(readOnly = true)
    public UserPageResponse searchUsers(UserSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, request.sortBy());
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<User> userPage = userRepository.searchUsers(
                request.username(),
                request.email(),
                request.fullName(),
                request.role(),
                pageable
        );

        List<UserResponse> content = userPage.getContent().stream()
                .map(this::mapToUserResponse)
                .collect(Collectors.toList());

        return new UserPageResponse(
                content,
                userPage.getTotalElements(),
                userPage.getTotalPages(),
                userPage.getNumber(),
                userPage.getSize()
        );
    }


    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getRole() // <-- Trả về trực tiếp User.Role, không cần toString()
        );
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}