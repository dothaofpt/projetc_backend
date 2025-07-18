package org.example.projetc_backend.controller;

import com.paypal.base.rest.PayPalRESTException;
import jakarta.validation.Valid;
import org.example.projetc_backend.dto.PaymentRequest;
import org.example.projetc_backend.dto.PaymentResponse;
import org.example.projetc_backend.dto.PaymentSearchRequest;
import org.example.projetc_backend.service.PaymentService;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.view.RedirectView;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/payments")
@CrossOrigin(origins = "*")
public class PaymentController {

    private final PaymentService paymentService;

    public PaymentController(PaymentService paymentService) {
        this.paymentService = paymentService;
    }

    @PostMapping("/paypal/initiate")
    @PreAuthorize("isAuthenticated()")
    public ResponseEntity<String> initiatePayPalPayment(@Valid @RequestBody PaymentRequest request) throws PayPalRESTException {
        String approvalUrl = paymentService.initiatePayPalPayment(request);
        return ResponseEntity.ok(approvalUrl);
    }

    @GetMapping("/paypal/complete")
    public RedirectView completePayPalPayment(
            @RequestParam("paymentId") String paymentId,
            @RequestParam("PayerID") String payerId) throws PayPalRESTException {
        Map<String, String> redirectInfo = paymentService.completePayPalPayment(paymentId, payerId);
        return new RedirectView(redirectInfo.get("redirectUrl"));
    }

    @GetMapping("/paypal/cancel")
    public RedirectView cancelPayPalPayment(@RequestParam("token") String token) {
        Map<String, String> redirectInfo = paymentService.cancelPayPalPayment(token);
        return new RedirectView(redirectInfo.get("redirectUrl"));
    }

    @GetMapping("/user/{userId}")
    @PreAuthorize("hasRole('ADMIN') or #userId == authentication.principal.userId")
    public ResponseEntity<List<PaymentResponse>> getPaymentsByUserId(@PathVariable Integer userId) {
        List<PaymentResponse> payments = paymentService.getPaymentsByUserId(userId);
        return ResponseEntity.ok(payments);
    }

    // Các endpoint của Admin
    @PostMapping("/search")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Page<PaymentResponse>> searchPayments(@RequestBody PaymentSearchRequest request) {
        Page<PaymentResponse> responses = paymentService.searchPayments(request);
        return ResponseEntity.ok(responses);
    }

    @GetMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<PaymentResponse> getPaymentById(@PathVariable Integer id) {
        PaymentResponse payment = paymentService.getPaymentById(id);
        return ResponseEntity.ok(payment);
    }

    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<List<PaymentResponse>> getAllPayments() {
        List<PaymentResponse> payments = paymentService.getAllPayments();
        return ResponseEntity.ok(payments);
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deletePayment(@PathVariable Integer id) {
        paymentService.deletePayment(id);
        return ResponseEntity.noContent().build();
    }
}