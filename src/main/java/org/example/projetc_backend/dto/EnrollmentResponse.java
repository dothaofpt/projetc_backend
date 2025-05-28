package org.example.projetc_backend.dto;

import java.time.LocalDateTime;

public record EnrollmentResponse(
        Integer enrollmentId,
        Integer userId,
        String userName, // Tên người dùng để hiển thị cho admin
        Integer lessonId,
        String lessonTitle, // Tên bài học để hiển thị
        LocalDateTime enrollmentDate,
        LocalDateTime expiryDate // Ngày hết hạn của khóa học
) {}