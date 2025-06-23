package org.example.projetc_backend.service;

import com.paypal.api.payments.*;
import com.paypal.base.rest.APIContext;
import com.paypal.base.rest.PayPalRESTException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.annotation.PostConstruct; // Import for @PostConstruct
import java.util.ArrayList;
import java.util.List;

@Service
public class PayPalService {
    private final APIContext apiContext;
    private final String clientId;
    private final String clientSecret;

    public PayPalService(
            @Value("${paypal.client.id}") String clientId,
            @Value("${paypal.client.secret}") String clientSecret,
            @Value("${paypal.base.url}") String baseUrl
    ) {
        this.clientId = clientId;
        this.clientSecret = clientSecret;

        String mode;
        if (baseUrl.contains("sandbox")) {
            mode = "sandbox";
        } else if (baseUrl.contains("api-m.paypal.com")) {
            mode = "live";
        } else {
            mode = "sandbox"; // Mặc định là sandbox nếu không rõ
            System.err.println("Cảnh báo: baseUrl không xác định môi trường PayPal, mặc định là sandbox.");
        }

        this.apiContext = new APIContext(clientId, clientSecret, mode);
    }

    @PostConstruct
    public void logPayPalConfig() {
        System.out.println("--- PayPalService Configuration ---");
        System.out.println("Injected Client ID: " + clientId);
        // Chỉ in một phần của Secret Key để tránh lộ thông tin nhạy cảm trong log công khai
        System.out.println("Injected Client Secret (first 5 chars): " +
                (clientSecret != null && clientSecret.length() >= 5 ? clientSecret.substring(0, 5) + "..." : clientSecret));
        System.out.println("--- End PayPalService Configuration ---");
    }


    public com.paypal.api.payments.Payment createPayment(
            double total,
            String currency, // Đảm bảo currency này phù hợp với tài khoản PayPal của bạn (USD, VND,...)
            String method,
            String intent,
            String description,
            String cancelUrl,
            String successUrl
    ) throws PayPalRESTException {
        Amount amount = new Amount();
        amount.setCurrency(currency);
        amount.setTotal(String.format("%.2f", total));

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

    public com.paypal.api.payments.Payment executePayment(String paymentId, String payerId) throws PayPalRESTException {
        com.paypal.api.payments.Payment payment = new com.paypal.api.payments.Payment();
        payment.setId(paymentId);
        PaymentExecution paymentExecution = new PaymentExecution();
        paymentExecution.setPayerId(payerId);
        return payment.execute(apiContext, paymentExecution);
    }
}