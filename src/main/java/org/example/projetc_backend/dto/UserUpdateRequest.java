package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.User; // Import entity để dùng enum Role

public record UserUpdateRequest(
        String username,
        String email,
        String password, // Lưu ý: Cần xử lý mã hóa password ở service
        String fullName,
        String avatarUrl,
        User.Role role // Cho phép admin cập nhật vai trò người dùng
) {}