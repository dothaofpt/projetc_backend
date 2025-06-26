// src/main/java/org/example/projetc_backend/dto/PaymentSearchRequest.java
package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.Payment;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.data.domain.PageRequest; // Import này có thể cần nếu bạn thêm toPageable()
import org.springframework.data.domain.Pageable;   // Import này có thể cần nếu bạn thêm toPageable()
import org.springframework.data.domain.Sort;       // Import này có thể cần nếu bạn thêm toPageable()


import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO dùng để truyền các tiêu chí tìm kiếm và thông tin phân trang cho giao dịch thanh toán.
 * Sử dụng record để tạo DTO bất biến, gọn gàng.
 */
public record PaymentSearchRequest(
        Integer userId,
        Integer orderId, // ID của đơn hàng liên quan
        Payment.PaymentStatus status, // Trạng thái thanh toán (ví dụ: PENDING, COMPLETED)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime minDate, // Ngày bắt đầu khoảng thời gian thanh toán
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime maxDate, // Ngày kết thúc khoảng thời gian thanh toán
        BigDecimal minAmount, // Số tiền tối thiểu
        BigDecimal maxAmount, // Số tiền tối đa
        String paymentMethod, // Phương thức thanh toán (ví dụ: "PAYPAL", "CREDIT_CARD")
        String transactionId, // <-- THÊM TRƯỜNG NÀY VÀO ĐÂY

        // Các trường cho phân trang và sắp xếp (có giá trị mặc định)
        int page,
        int size,
        String sortBy, // Tên trường để sắp xếp (ví dụ: "paymentId", "paymentDate", "amount")
        String sortDir // Hướng sắp xếp ("ASC" hoặc "DESC")
) {
    /**
     * Constructor mặc định để cung cấp các giá trị mặc định cho phân trang và sắp xếp.
     */
    public PaymentSearchRequest {
        if (page < 0) page = 0;
        if (size <= 0) size = 10;
        if (sortBy == null || sortBy.isBlank()) sortBy = "paymentId"; // Mặc định sắp xếp theo ID thanh toán
        if (sortDir == null || sortDir.isBlank() || (!sortDir.equalsIgnoreCase("ASC") && !sortDir.equalsIgnoreCase("DESC"))) {
            sortDir = "ASC"; // Mặc định sắp xếp tăng dần
        }
    }

    /**
     * Chuyển đổi các thuộc tính phân trang của DTO này thành một đối tượng Pageable.
     * (Đây là phương thức đã đề xuất cho OrderSearchRequest, cũng hữu ích ở đây)
     */
    public Pageable toPageable() {
        Sort sort = Sort.by(Sort.Direction.fromString(this.sortDir()), this.sortBy());
        return PageRequest.of(this.page(), this.size(), sort);
    }
}