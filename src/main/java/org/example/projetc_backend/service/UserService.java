package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.UserUpdateRequest;
import org.example.projetc_backend.dto.UserResponse;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Optional;

@Service
public class UserService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    public UserService(UserRepository userRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
    }

    public Optional<User> findByUsername(String username) {
        if (username == null) {
            throw new IllegalArgumentException("Username không được để trống");
        }
        return userRepository.findByUsername(username);
    }

    public Optional<User> findByEmail(String email) {
        if (email == null) {
            throw new IllegalArgumentException("Email không được để trống");
        }
        return userRepository.findByEmail(email);
    }

    public Optional<User> findById(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống");
        }
        return userRepository.findById(userId);
    }

    public UserResponse registerUser(String username, String email, String password, String fullName, String role) {
        if (username == null || email == null || password == null) {
            throw new IllegalArgumentException("Username, email, và password là bắt buộc");
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
        user.setRole("ROLE_ADMIN".equalsIgnoreCase(role) ? User.Role.ROLE_ADMIN : User.Role.ROLE_USER);

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    public UserResponse updateUser(Integer userId, UserUpdateRequest request) {
        if (userId == null || request == null) {
            throw new IllegalArgumentException("User ID hoặc request không được để trống");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        user.setFullName(request.fullName() != null ? request.fullName() : user.getFullName());
        user.setAvatarUrl(request.avatarUrl() != null ? request.avatarUrl() : user.getAvatarUrl());

        user = userRepository.save(user);

        return mapToUserResponse(user);
    }

    private UserResponse mapToUserResponse(User user) {
        return new UserResponse(
                user.getUserId(),
                user.getUsername(),
                user.getEmail(),
                user.getFullName(),
                user.getAvatarUrl(),
                user.getCreatedAt(),
                user.getRole().toString()
        );
    }

    public PasswordEncoder getPasswordEncoder() {
        return passwordEncoder;
    }
}
