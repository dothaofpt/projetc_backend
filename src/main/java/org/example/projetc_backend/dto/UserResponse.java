// src/main/java/org/example/projetc_backend/dto/UserResponse.java
package org.example.projetc_backend.dto;

import java.time.LocalDateTime;
import org.example.projetc_backend.entity.User; // Import entity để dùng enum Role

public record UserResponse(
        Integer userId,
        String username,
        String email,
        String fullName,
        String avatarUrl,
        LocalDateTime createdAt,
        User.Role role // <-- Giữ nguyên là User.Role
) {}