package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.PaymentRequest;
import org.example.projetc_backend.dto.PaymentResponse;
import org.example.projetc_backend.service.PaymentService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.paypal.base.rest.PayPalRESTException;

import jakarta.validation.Valid;
import java.util.List;

@RestController
@RequestMapping("/api/payments")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    // Endpoint này có thể dùng cho các phương thức thanh toán khác ngoài PayPal
    // hoặc là một bước khởi tạo Payment record trước khi chuyển sang cổng thanh toán.
    @PostMapping
    public ResponseEntity<PaymentResponse> createPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            PaymentResponse newPayment = paymentService.createPayment(request);
            return new ResponseEntity<>(newPayment, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.BAD_REQUEST); // Nên trả về lỗi body với thông báo
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // Lỗi không mong muốn
        }
    }

    @GetMapping("/{id}")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Integer id) {
        try {
            PaymentResponse payment = paymentService.getPaymentById(id);
            return new ResponseEntity<>(payment, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @GetMapping
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return new ResponseEntity<>(payments, HttpStatus.OK);
    }

    @GetMapping("/user/{userId}")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Integer userId) {
        try {
            List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
            return new ResponseEntity<>(payments, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deletePayment(@PathVariable Integer id) {
        try {
            paymentService.deletePayment(id);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(HttpStatus.NOT_FOUND);
        }
    }

    @PostMapping("/paypal/initiate")
    // CẬP NHẬT: Nhận cancelUrl và successUrl trực tiếp từ PaymentRequest
    public ResponseEntity<String> initiatePayPalPayment(@Valid @RequestBody PaymentRequest request) {
        try {
            String redirectUrl = paymentService.initiatePayPalPayment(
                    request.userId(),
                    request.orderId(),
                    request.amount(),
                    request.cancelUrl(), // Lấy từ PaymentRequest
                    request.successUrl()  // Lấy từ PaymentRequest
            );
            return new ResponseEntity<>(redirectUrl, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(e.getMessage());
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi khi khởi tạo thanh toán PayPal: " + e.getMessage());
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Lỗi hệ thống khi khởi tạo thanh toán: " + e.getMessage());
        }
    }

    @GetMapping("/paypal/complete")
    public ResponseEntity<PaymentResponse> completePayPalPayment(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) {
        try {
            PaymentResponse completedPayment = paymentService.completePayPalPayment(paymentId, payerId);
            return new ResponseEntity<>(completedPayment, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(null); // Nên trả về body lỗi rõ ràng hơn
        } catch (PayPalRESTException e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Nên trả về body lỗi rõ ràng hơn
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null); // Lỗi không mong muốn
        }
    }

    @GetMapping("/paypal/cancel")
    public ResponseEntity<String> cancelPayPalPayment(@RequestParam("token") String token) {
        // Logic để xử lý khi người dùng hủy thanh toán trên PayPal và được redirect về
        // Bạn có thể tìm kiếm Payment/Order liên quan đến 'token' và cập nhật trạng thái là CANCELLED
        System.out.println("Thanh toán PayPal đã bị hủy. Token: " + token);
        return new ResponseEntity<>("Thanh toán PayPal đã bị hủy bởi người dùng.", HttpStatus.OK);
    }
}