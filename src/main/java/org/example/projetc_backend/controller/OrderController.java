package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.OrderRequest;
import org.example.projetc_backend.dto.OrderResponse;
import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.service.OrderService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = "*") // Đảm bảo đã có CORS nếu cần
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Tạo một đơn hàng mới.
     * POST /api/orders
     * @param request DTO chứa thông tin đơn hàng.
     * @return ResponseEntity với OrderResponse của đơn hàng đã tạo.
     */
    @PostMapping
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        try {
            OrderResponse newOrder = orderService.createOrder(request);
            return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Lấy thông tin đơn hàng bằng ID.
     * GET /api/orders/{id}
     * @param id ID của đơn hàng.
     * @return ResponseEntity với OrderResponse.
     */
    @GetMapping("/{id}")
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Integer id) {
        try {
            OrderResponse order = orderService.getOrderById(id);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy tất cả các đơn hàng.
     * GET /api/orders
     * @return ResponseEntity với danh sách OrderResponse.
     */
    @GetMapping
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    /**
     * Lấy tất cả các đơn hàng của một người dùng cụ thể.
     * GET /api/orders/user/{userId}
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách OrderResponse.
     */
    @GetMapping("/user/{userId}")
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Integer userId) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Tìm kiếm đơn hàng với các tiêu chí tùy chọn.
     * GET /api/orders/search
     * @param userId ID của người dùng (tùy chọn)
     * @param status Trạng thái đơn hàng (tùy chọn, ví dụ: PENDING, COMPLETED)
     * @param minDate Ngày bắt đầu khoảng thời gian (ISO-8601 format, ví dụ: 2023-01-01T00:00:00) (tùy chọn)
     * @param maxDate Ngày kết thúc khoảng thời gian (ISO-8601 format, ví dụ: 2023-12-31T23:59:59) (tùy chọn)
     * @param minTotalAmount Tổng tiền tối thiểu (tùy chọn)
     * @param maxTotalAmount Tổng tiền tối đa (tùy chọn)
     * @param username Tên người dùng (tìm kiếm gần đúng, không phân biệt chữ hoa/thường) (tùy chọn)
     * @return ResponseEntity với danh sách OrderResponse phù hợp.
     */
    @GetMapping("/search")
    public ResponseEntity<List<OrderResponse>> searchOrders(
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) Order.OrderStatus status,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime minDate,
            @RequestParam(required = false) @org.springframework.format.annotation.DateTimeFormat(iso = org.springframework.format.annotation.DateTimeFormat.ISO.DATE_TIME) LocalDateTime maxDate,
            @RequestParam(required = false) BigDecimal minTotalAmount,
            @RequestParam(required = false) BigDecimal maxTotalAmount,
            @RequestParam(required = false) String username) {
        List<OrderResponse> orders = orderService.searchOrders(
                userId, status, minDate, maxDate, minTotalAmount, maxTotalAmount, username);
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    /**
     * Cập nhật trạng thái đơn hàng.
     * PUT /api/orders/{id}/status
     * @param id ID của đơn hàng.
     * @param newStatus Trạng thái mới.
     * @return ResponseEntity với OrderResponse đã cập nhật.
     */
    @PutMapping("/{id}/status")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam("status") Order.OrderStatus newStatus) {
        try {
            OrderResponse updatedOrder = orderService.updateOrderStatus(id, newStatus);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Xóa một đơn hàng.
     * DELETE /api/orders/{id}
     * @param id ID của đơn hàng cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        try {
            orderService.deleteOrder(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}