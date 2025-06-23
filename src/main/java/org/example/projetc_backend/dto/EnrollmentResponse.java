package org.example.projetc_backend.dto;

import java.time.LocalDateTime;
// Cần import LessonResponse để nhúng vào đây
import org.example.projetc_backend.dto.LessonResponse;

public record EnrollmentResponse(
        Integer enrollmentId,
        Integer userId,
        String userName,
        // Bỏ lessonId và lessonTitle cũ đi, vì chúng sẽ nằm trong LessonResponse
        // Integer lessonId,
        // String lessonTitle,
        LessonResponse lesson, // <--- THÊM TRƯỜNG NÀY ĐỂ CHỨA TOÀN BỘ THÔNG TIN BÀI HỌC
        LocalDateTime enrollmentDate,
        LocalDateTime expiryDate,
        String status // <--- THÊM TRƯỜNG NÀY NẾU BẠN CÓ TÍNH TOÁN STATUS Ở BACKEND
) {}