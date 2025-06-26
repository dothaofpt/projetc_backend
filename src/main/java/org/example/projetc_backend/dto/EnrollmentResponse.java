package org.example.projetc_backend.dto;

import java.time.LocalDateTime;
import org.example.projetc_backend.dto.LessonResponse; // Đảm bảo LessonResponse DTO tồn tại

public record EnrollmentResponse(
        Integer enrollmentId,
        Integer userId,
        String userName, // Thêm trường này cho dễ hiển thị
        LessonResponse lesson, // Bao gồm đầy đủ thông tin bài học
        LocalDateTime enrollmentDate,
        LocalDateTime expiryDate, // THÊM DÒNG NÀY: Ngày hết hạn của đăng ký
        String status // THÊM DÒNG NÀY: Trạng thái của đăng ký (ACTIVE/EXPIRED)
) {}