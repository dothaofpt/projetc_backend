package org.example.projetc_backend.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct;
import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {
    private final APIContext apiContext;
    private final String clientId;
    private final String clientSecret;
    private final String mode; // <-- Thêm biến này để lưu trữ mode

    public PayPalService(
            @Value("${paypal.client.id}") String clientId,
            @Value("${paypal.client.secret}") String clientSecret,
            @Value("${paypal.mode}") String mode
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;
        this.mode = mode; // <-- Gán giá trị mode vào biến instance
        this.apiContext = new APIContext(clientId, clientSecret, mode);
    }

    @PostConstruct
    public void logPayPalConfig() {
        System.out.println("--- PayPalService Configuration ---");
        System.out.println("Injected Client ID: " + clientId);
        // Chỉ in một phần của Secret Key để tránh lộ thông tin nhạy cảm trong log công khai
        System.out.println("Injected Client Secret (first 5 chars): " +
                (clientSecret != null && clientSecret.length() >= 5 ? clientSecret.substring(0, 5) + "..." : "[Hidden]"));
        System.out.println("PayPal API Mode: " + this.mode); // <-- Sử dụng biến instance 'mode'
        System.out.println("--- End PayPalService Configuration ---");
    }

    /**
     * Tạo một thanh toán PayPal.
     * @param total Tổng số tiền.
     * @param currency Loại tiền tệ (ví dụ: "USD").
     * @param method Phương thức thanh toán (ví dụ: "paypal").
     * @param intent Mục đích giao dịch (ví dụ: "sale").
     * @param description Mô tả giao dịch.
     * @param cancelUrl URL nếu người dùng hủy thanh toán.
     * @param successUrl URL nếu thanh toán thành công.
     * @return Đối tượng Payment từ PayPal API.
     * @throws PayPalRESTException nếu có lỗi khi tạo thanh toán.
     */
    public com.paypal.api.payments.Payment createPayment(
            double total,
            String currency,
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException {
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f", total)); // Định dạng 2 chữ số thập phân

        Transaction transaction = new Transaction();
        transaction.setDescription(description);
        transaction.setAmount(amount);

        List<Transaction> transactions = new ArrayList<>();
        transactions.add(transaction);

        Payer payer = new Payer();
        payer.setPaymentMethod(method);

        com.paypal.api.payments.Payment payment = new com.paypal.api.payments.Payment();
        payment.setIntent(intent);
        payment.setPayer(payer);
        payment.setTransactions(transactions);

        RedirectUrls redirectUrls = new RedirectUrls();
        redirectUrls.setCancelUrl(cancelUrl);
        redirectUrls.setReturnUrl(successUrl);
        payment.setRedirectUrls(redirectUrls);

        return payment.create(apiContext);
    }

    /**
     * Thực thi một thanh toán PayPal sau khi người dùng chấp thuận.
     * @param paymentId ID của thanh toán từ PayPal.
     * @param payerId ID người thanh toán được PayPal cung cấp sau khi chấp thuận.
     * @return Đối tượng Payment đã được thực thi từ PayPal API.
     * @throws PayPalRESTException nếu có lỗi khi thực thi thanh toán.
     */
    public com.paypal.api.payments.Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        com.paypal.api.payments.Payment payment = new com.paypal.api.payments.Payment();
        payment.setId(paymentId); // Set ID của thanh toán cần thực thi

        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId); // Set Payer ID từ người dùng

        // Thực thi thanh toán
        return payment.execute(apiContext, paymentExecution);
    }
}