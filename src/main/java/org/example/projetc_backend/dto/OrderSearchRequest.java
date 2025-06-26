// src/main/java/org/example/projetc_backend/dto/OrderSearchRequest.java
package org.example.projetc_backend.dto;

import org.example.projetc_backend.entity.Order; // <--- MAKE SURE THIS IS PRESENT
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.format.annotation.DateTimeFormat;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * DTO dùng để truyền các tiêu chí tìm kiếm và thông tin phân trang cho đơn hàng.
 * Sử dụng record để tạo DTO bất biến, gọn gàng.
 */
public record OrderSearchRequest(
        Integer userId,
        Order.OrderStatus status, // Trạng thái đơn hàng (ví dụ: PENDING, COMPLETED)
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) // Định dạng cho LocalDateTime từ request param
        LocalDateTime minDate, // Ngày bắt đầu khoảng thời gian
        @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
        LocalDateTime maxDate, // Ngày kết thúc khoảng thời gian
        BigDecimal minTotalAmount, // Tổng tiền tối thiểu
        BigDecimal maxTotalAmount, // Tổng tiền tối đa
        String username, // Tên người dùng (tìm kiếm gần đúng)

        // Các trường cho phân trang và sắp xếp (có giá trị mặc định)
        int page,
        int size,
        String sortBy, // Tên trường để sắp xếp (ví dụ: "orderId", "orderDate")
        String sortDir // Hướng sắp xếp ("ASC" hoặc "DESC")
) {
    /**
     * Constructor mặc định để cung cấp các giá trị mặc định cho phân trang và sắp xếp.
     * Khi các trường này không được cung cấp trong request, chúng sẽ nhận giá trị mặc định.
     */
    public OrderSearchRequest {
        if (page < 0) page = 0; // Trang không thể âm
        if (size <= 0) size = 10; // Kích thước trang phải dương, mặc định 10
        if (sortBy == null || sortBy.isBlank()) sortBy = "orderId"; // Mặc định sắp xếp theo ID đơn hàng
        if (sortDir == null || sortDir.isBlank() || (!sortDir.equalsIgnoreCase("ASC") && !sortDir.equalsIgnoreCase("DESC"))) {
            sortDir = "ASC"; // Mặc định sắp xếp tăng dần
        }
    }

    /**
     * Chuyển đổi các thuộc tính phân trang của DTO này thành một đối tượng Pageable.
     * @return Một đối tượng Pageable được cấu hình.
     */
    public Pageable toPageable() {
        // Sort.Direction.fromString() sẽ chuyển đổi "ASC" hoặc "DESC" thành enum Sort.Direction
        Sort sort = Sort.by(Sort.Direction.fromString(this.sortDir()), this.sortBy());
        return PageRequest.of(this.page(), this.size(), sort);
    }
}