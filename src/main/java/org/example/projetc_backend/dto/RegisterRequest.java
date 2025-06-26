// src/main/java/org/example/projetc_backend/dto/RegisterRequest.java
package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.example.projetc_backend.entity.User; // Import User để sử dụng enum Role

public record RegisterRequest(
        @NotBlank(message = "Username is required")
        String username,
        @NotBlank(message = "Email is required")
        @Email(message = "Invalid email format")
        String email,
        @NotBlank(message = "Password is required")
        @Size(min = 6, message = "Password must be at least 6 characters")
        String password,
        String fullName,
        User.Role role // <-- Đảm bảo có trường này, kiểu là User.Role enum
) {}