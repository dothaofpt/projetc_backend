// src/main/java/org/example/projetc_backend/dto/UserSearchRequest.java
package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Min;
import org.example.projetc_backend.entity.User; // Import để dùng enum Role

public record UserSearchRequest(
        String username,
        String email,
        String fullName,
        User.Role role, // Cho phép tìm kiếm theo vai trò
        @Min(0) Integer page,
        @Min(1) Integer size,
        String sortBy,
        String sortDir
) {
    public UserSearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "userId"; // Mặc định sắp xếp theo userId
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}