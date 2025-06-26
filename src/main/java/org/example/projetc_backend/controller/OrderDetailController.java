package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.OrderDetailRequest;
import org.example.projetc_backend.dto.OrderDetailResponse;
import org.example.projetc_backend.service.OrderDetailService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/order-details")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class OrderDetailController {

    private final OrderDetailService orderDetailService;

    public OrderDetailController(OrderDetailService orderDetailService) {
        this.orderDetailService = orderDetailService;
    }

    // Đã loại bỏ endpoint POST để tạo OrderDetail riêng lẻ
    // OrderDetails nên được tạo cùng lúc với Order trong OrderService.

    /**
     * Lấy thông tin chi tiết đơn hàng theo ID.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param id ID của chi tiết đơn hàng.
     * @return ResponseEntity với OrderDetailResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> getOrderDetailById(@PathVariable Integer id) {
        try {
            OrderDetailResponse orderDetail = orderDetailService.getOrderDetailById(id);
            return new ResponseEntity<>(orderDetail, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy danh sách chi tiết đơn hàng cho một ID đơn hàng cụ thể.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param orderId ID của đơn hàng.
     * @return ResponseEntity với danh sách OrderDetailResponse.
     */
    @GetMapping("/order/{orderId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDetailResponse>> getOrderDetailsByOrderId(@PathVariable Integer orderId) {
        try {
            List<OrderDetailResponse> orderDetails = orderDetailService.getOrderDetailsByOrderId(orderId);
            return new ResponseEntity<>(orderDetails, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    /**
     * Lấy tất cả các chi tiết đơn hàng.
     * Chỉ ADMIN mới có quyền truy cập.
     * @return ResponseEntity với danh sách OrderDetailResponse.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderDetailResponse>> getAllOrderDetails() {
        List<OrderDetailResponse> orderDetails = orderDetailService.getAllOrderDetails();
        return new ResponseEntity<>(orderDetails, HttpStatus.OK);
    }

    /**
     * Cập nhật thông tin chi tiết đơn hàng.
     * Chỉ ADMIN mới có quyền.
     * @param id ID của chi tiết đơn hàng.
     * @param request DTO chứa thông tin cập nhật.
     * @return ResponseEntity với OrderDetailResponse đã cập nhật.
     */
    @PutMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderDetailResponse> updateOrderDetail(
            @PathVariable Integer id,
            @Valid @RequestBody OrderDetailRequest request) {
        try {
            OrderDetailResponse updatedOrderDetail = orderDetailService.updateOrderDetail(id, request);
            return new ResponseEntity<>(updatedOrderDetail, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * Xóa một chi tiết đơn hàng.
     * Chỉ ADMIN mới có quyền.
     * @param id ID của chi tiết đơn hàng cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrderDetail(@PathVariable Integer id) {
        try {
            orderDetailService.deleteOrderDetail(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        }
        catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }
}