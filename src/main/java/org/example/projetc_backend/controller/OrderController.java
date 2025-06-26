package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.OrderRequest;
import org.example.projetc_backend.dto.OrderResponse;
import org.example.projetc_backend.dto.OrderSearchRequest; // <-- Rất quan trọng: Thêm import này
import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.service.OrderService;
import org.springframework.data.domain.Page; // <-- Rất quan trọng: Thêm import này để xử lý kết quả phân trang
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid; // Để kích hoạt validation cho OrderRequest và OrderSearchRequest
import java.util.List;

@RestController
@RequestMapping("/api/orders")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class OrderController {

    private final OrderService orderService;

    public OrderController(OrderService orderService) {
        this.orderService = orderService;
    }

    /**
     * Tạo một đơn hàng mới.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể tạo đơn hàng cho chính mình.
     * @param request DTO chứa thông tin đơn hàng.
     * @return ResponseEntity với OrderResponse của đơn hàng đã tạo.
     */
    @PostMapping
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<OrderResponse> createOrder(@Valid @RequestBody OrderRequest request) {
        try {
            OrderResponse newOrder = orderService.createOrder(request);
            return new ResponseEntity<>(newOrder, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            // Cung cấp thông báo lỗi rõ ràng hơn trong body
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            // Xử lý các lỗi không mong muốn khác
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Lấy thông tin đơn hàng bằng ID.
     * Chỉ ADMIN mới có quyền truy cập mặc định.
     * Cân nhắc thêm quyền cho USER để truy cập đơn hàng của chính họ.
     * @param id ID của đơn hàng.
     * @return ResponseEntity với OrderResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')") // Ví dụ nâng cao: "hasRole('ADMIN') or (hasRole('USER') and @orderService.isOrderBelongsToUser(#id, authentication.principal.id))"
    public ResponseEntity<OrderResponse> getOrderById(@PathVariable Integer id) {
        try {
            OrderResponse order = orderService.getOrderById(id);
            return new ResponseEntity<>(order, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Lấy tất cả các đơn hàng.
     * Chỉ ADMIN mới có quyền truy cập.
     * (Lưu ý: Endpoint này có thể được gộp vào `/api/orders/search` để linh hoạt hơn và sử dụng phân trang).
     * @return ResponseEntity với danh sách OrderResponse.
     */
    @GetMapping("/all") // Đổi sang /all để tránh xung đột với /api/orders/search (nếu bạn muốn giữ riêng getAll)
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<OrderResponse>> getAllOrders() {
        List<OrderResponse> orders = orderService.getAllOrders();
        return new ResponseEntity<>(orders, HttpStatus.OK);
    }

    /**
     * Lấy tất cả các đơn hàng của một người dùng cụ thể.
     * Cả USER và ADMIN đều có quyền (USER chỉ xem được của chính mình, ADMIN xem được của bất kỳ ai).
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách OrderResponse.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or (hasRole('USER') and #userId == authentication.principal.id)") // Kiểm tra quyền truy cập của USER
    public ResponseEntity<List<OrderResponse>> getOrdersByUserId(@PathVariable Integer userId) {
        try {
            List<OrderResponse> orders = orderService.getOrdersByUserId(userId);
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Tìm kiếm và phân trang đơn hàng với các tiêu chí tùy chọn.
     * Chỉ ADMIN mới có quyền truy cập.
     * Endpoint này nhận các tham số tìm kiếm và phân trang thông qua `OrderSearchRequest` DTO.
     *
     * @param searchRequest DTO chứa các tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * Spring sẽ tự động bind các query parameter vào DTO này.
     * @return ResponseEntity với Page<OrderResponse> phù hợp.
     */
    @GetMapping("/search") // Sử dụng GET cho tìm kiếm với query parameters, Spring sẽ ánh xạ vào DTO
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<OrderResponse>> searchOrders(@Valid @ModelAttribute OrderSearchRequest searchRequest) {
        try {
            // Gọi phương thức searchOrders trong service với DTO
            Page<OrderResponse> orders = orderService.searchOrders(
                    searchRequest.userId(),
                    searchRequest.status(),
                    searchRequest.minDate(),
                    searchRequest.maxDate(),
                    searchRequest.minTotalAmount(),
                    searchRequest.maxTotalAmount(),
                    searchRequest.username(),
                    searchRequest.toPageable() // <-- Phương thức toPageable() mới trong DTO
            );
            return new ResponseEntity<>(orders, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Cập nhật trạng thái đơn hàng.
     * Chỉ ADMIN mới có quyền.
     * @param id ID của đơn hàng.
     * @param newStatus Trạng thái mới.
     * @return ResponseEntity với OrderResponse đã cập nhật.
     */
    @PutMapping("/{id}/status")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<OrderResponse> updateOrderStatus(
            @PathVariable Integer id,
            @RequestParam("status") Order.OrderStatus newStatus) {
        try {
            OrderResponse updatedOrder = orderService.updateOrderStatus(id, newStatus);
            return new ResponseEntity<>(updatedOrder, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Xóa một đơn hàng.
     * Chỉ ADMIN mới có quyền.
     * @param id ID của đơn hàng cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteOrder(@PathVariable Integer id) {
        try {
            orderService.deleteOrder(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }
}