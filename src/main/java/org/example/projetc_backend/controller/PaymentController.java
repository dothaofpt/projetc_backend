package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.PaymentRequest;
import org.example.projetc_backend.dto.PaymentResponse;
import org.example.projetc_backend.dto.PaymentSearchRequest; // Import DTO tìm kiếm mới
import org.example.projetc_backend.service.PaymentService;
import org.springframework.data.domain.Page; // Import Page
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.*;
import com.paypal.base.rest.PayPalRESTException;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    /**
     * Tạo một bản ghi thanh toán.
     * Endpoint này có thể dùng cho các phương thức thanh toán khác ngoài PayPal
     * hoặc là một bước khởi tạo Payment record trước khi chuyển sang cổng thanh toán.
     * Chỉ ADMIN mới có quyền tạo thanh toán trực tiếp (ví dụ: thanh toán offline).
     * @param request DTO chứa thông tin thanh toán.
     * @return ResponseEntity với PaymentResponse của thanh toán đã tạo.
     */
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            PaymentResponse newPayment = paymentService.createPayment(request);
            return new ResponseEntity<>(newPayment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Nên trả về lỗi body với thông báo
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Lỗi không mong muốn
        }
    }

    /**
     * Lấy thông tin thanh toán bằng ID.
     * Chỉ ADMIN mới có quyền truy cập.
     * @param id ID của thanh toán.
     * @return ResponseEntity với PaymentResponse.
     */
    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Integer id) {
        try {
            PaymentResponse payment = paymentService.getPaymentById(id);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Lấy tất cả các giao dịch thanh toán.
     * Chỉ ADMIN mới có quyền truy cập.
     * @return ResponseEntity với danh sách PaymentResponse.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    /**
     * Lấy tất cả các giao dịch thanh toán của một người dùng cụ thể.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể xem của chính mình.
     * @param userId ID của người dùng.
     * @return ResponseEntity với danh sách PaymentResponse.
     */
    @GetMapping("/user/{userId}")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')") // Ví dụ: @PreAuthorize("hasRole('ADMIN') or (#userId == authentication.principal.id)")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Integer userId) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
            return new ResponseEntity<>(payments, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Xóa một giao dịch thanh toán.
     * Chỉ ADMIN mới có quyền.
     * @param id ID của thanh toán cần xóa.
     * @return ResponseEntity với HttpStatus.NO_CONTENT.
     */
    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Integer id) {
        try {
            paymentService.deletePayment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(null);
        }
    }

    /**
     * Tìm kiếm thanh toán với các tiêu chí và phân trang.
     * Chỉ ADMIN mới có quyền.
     * @param request DTO chứa tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return ResponseEntity với Page của PaymentResponse.
     */
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> searchPayments(@RequestBody PaymentSearchRequest request) {
        try {
            Page<PaymentResponse> responses = paymentService.searchPayments(request);
            return new ResponseEntity<>(responses, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        }
    }

    /**
     * Khởi tạo quá trình thanh toán PayPal.
     * Cả USER và ADMIN đều có quyền. USER chỉ có thể tạo thanh toán cho đơn hàng của mình.
     * @param request PaymentRequest chứa userId, orderId, amount, cancelUrl và successUrl.
     * @return ResponseEntity với URL phê duyệt của PayPal.
     */
    @PostMapping("/paypal/initiate")
    @PreAuthorize("hasAnyRole('USER', 'ADMIN')")
    public ResponseEntity<String> initiatePayPalPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            String redirectUrl = paymentService.initiatePayPalPayment(request);
            return new ResponseEntity<>(redirectUrl, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi khởi tạo thanh toán PayPal: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống khi khởi tạo thanh toán: " + e.getMessage());
        }
    }

    /**
     * Hoàn tất quá trình thanh toán PayPal sau khi người dùng chấp thuận.
     * Endpoint này sẽ được PayPal gọi lại.
     * @param paymentId ID giao dịch PayPal.
     * @param payerId ID người thanh toán từ PayPal.
     * @return ResponseEntity với PaymentResponse của giao dịch đã hoàn tất.
     */
    @GetMapping("/paypal/complete")
    public ResponseEntity<PaymentResponse> completePayPalPayment(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) {
        try {
            PaymentResponse completedPayment = paymentService.completePayPalPayment(paymentId, payerId);
            return new ResponseEntity<>(completedPayment, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null);
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    /**
     * Endpoint được gọi khi người dùng hủy thanh toán trên PayPal.
     * @param token Token từ PayPal (có thể sử dụng để tìm giao dịch PENDING và hủy nó).
     * @return ResponseEntity với thông báo hủy.
     */
    @GetMapping("/paypal/cancel")
    public ResponseEntity<String> cancelPayPalPayment(@RequestParam("token") String token) {
        // Bạn có thể thêm logic để cập nhật trạng thái thanh toán PENDING thành CANCELLED trong DB tại đây.
        System.out.println("Thanh toán PayPal đã bị hủy. Token: " + token);
        return new ResponseEntity<>("Thanh toán PayPal đã bị hủy bởi người dùng.", HttpStatus.OK);
    }
}