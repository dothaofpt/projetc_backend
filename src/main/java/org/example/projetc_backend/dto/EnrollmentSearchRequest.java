// org.example.projetc_backend.dto.EnrollmentSearchRequest.java
package org.example.projetc_backend.dto;

import jakarta.validation.constraints.Pattern;

public record EnrollmentSearchRequest(
        Integer userId,
        Integer lessonId,
        // Có thể thêm các tiêu chí tìm kiếm khác nếu cần, ví dụ:
        // LocalDateTime enrollmentDateFrom,
        // LocalDateTime enrollmentDateTo,
        // Boolean isActive (nếu bạn có trường trạng thái active/inactive cho enrollment)
        // String status (nếu bạn muốn tìm kiếm theo trạng thái "ACTIVE"/"EXPIRED")

        Integer page,
        Integer size,
        String sortBy,
        @Pattern(regexp = "ASC|DESC", message = "Sort direction must be ASC or DESC")
        String sortDir
) {
    // Constructor để đặt giá trị mặc định nếu không được cung cấp
    public EnrollmentSearchRequest {
        if (page == null || page < 0) page = 0;
        if (size == null || size <= 0) size = 10;
        // Đảm bảo sortBy bao gồm các trường có thể sort từ Enrollment entity
        if (sortBy == null || sortBy.isBlank() ||
                !(sortBy.equals("enrollmentId") || sortBy.equals("enrollmentDate") ||
                        sortBy.equals("user.userId") || sortBy.equals("lesson.lessonId"))) {
            sortBy = "enrollmentId";
        }
        if (sortDir == null || sortDir.isBlank()) sortDir = "ASC";
    }
}