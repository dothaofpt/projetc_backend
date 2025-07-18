package org.example.projetc_backend.service;

import com.paypal.api.payments.Links;
import com.paypal.base.rest.PayPalRESTException;
import org.example.projetc_backend.dto.*;
import org.example.projetc_backend.entity.*;
import org.example.projetc_backend.repository.OrderRepository;
import org.example.projetc_backend.repository.PaymentRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.math.BigDecimal;


@Service
public class PaymentService {
    private static final Logger logger = LoggerFactory.getLogger(PaymentService.class);

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final EnrollmentService enrollmentService;
    private final PayPalService payPalService;

    @Value("${app.backend.base-url}")
    private String backendBaseUrl;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository,
                          OrderRepository orderRepository, EnrollmentService enrollmentService,
                          PayPalService payPalService) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.enrollmentService = enrollmentService;
        this.payPalService = payPalService;
    }

    @Transactional
    public String initiatePayPalPayment(PaymentRequest request) throws PayPalRESTException {
        // Lấy đối tượng Principal từ SecurityContextHolder
        Object principal = SecurityContextHolder.getContext().getAuthentication().getPrincipal();

        // Kiểm tra và ép kiểu an toàn
        if (!(principal instanceof org.springframework.security.core.userdetails.User)) {
            throw new AccessDeniedException("Người dùng chưa được xác thực hoặc kiểu đối tượng không đúng.");
        }
        org.springframework.security.core.userdetails.User springUser =
                (org.springframework.security.core.userdetails.User) principal;

        // Tìm kiếm thực thể User của bạn trong cơ sở dữ liệu bằng username
        User currentUser = userRepository.findByUsername(springUser.getUsername())
                .orElseThrow(() -> new AccessDeniedException("Không tìm thấy người dùng trong cơ sở dữ liệu."));

        if (!currentUser.getUserId().equals(request.userId())) {
            throw new AccessDeniedException("Bạn không có quyền tạo thanh toán cho người dùng khác.");
        }

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + request.orderId()));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Đơn hàng này không ở trạng thái PENDING để khởi tạo thanh toán.");
        }

        Payment localPayment = new Payment();
        localPayment.setUser(currentUser);
        localPayment.setOrder(order);
        localPayment.setAmount(request.amount());
        localPayment.setPaymentDate(LocalDateTime.now());
        localPayment.setPaymentMethod("PayPal");
        localPayment.setStatus(Payment.PaymentStatus.PENDING);
        localPayment.setDescription("Thanh toán PayPal cho đơn hàng #" + order.getOrderId());
        localPayment.setSuccessUrl(request.successUrl());
        localPayment.setCancelUrl(request.cancelUrl());

        String successCallbackUrl = backendBaseUrl + "/api/payments/paypal/complete";
        String cancelCallbackUrl = backendBaseUrl + "/api/payments/paypal/cancel";

        com.paypal.api.payments.Payment paypalPayment = payPalService.createPayment(
                request.amount().doubleValue(), "USD", "paypal", "sale",
                localPayment.getDescription(), cancelCallbackUrl, successCallbackUrl
        );

        localPayment.setTransactionId(paypalPayment.getId());
        paymentRepository.save(localPayment);

        order.setStatus(Order.OrderStatus.PROCESSING);
        orderRepository.save(order);

        for (Links link : paypalPayment.getLinks()) {
            if (link.getRel().equals("approval_url")) {
                return link.getHref();
            }
        }
        throw new IllegalStateException("Không tìm thấy URL phê duyệt từ PayPal.");
    }

    @Transactional
    public Map<String, String> completePayPalPayment(String paymentId, String payerId) throws PayPalRESTException {
        Payment localPayment = paymentRepository.findByTransactionId(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch với ID: " + paymentId));

        String finalRedirectUrl;

        if (localPayment.getStatus() == Payment.PaymentStatus.COMPLETED) {
            finalRedirectUrl = UriComponentsBuilder.fromUriString(localPayment.getSuccessUrl())
                    .queryParam("status", "already_completed").build().toUriString();
            return Map.of("redirectUrl", finalRedirectUrl);
        }

        com.paypal.api.payments.Payment executedPayment = payPalService.executePayment(paymentId, payerId);
        Order order = localPayment.getOrder();

        if ("approved".equalsIgnoreCase(executedPayment.getState())) {
            localPayment.setStatus(Payment.PaymentStatus.COMPLETED);
            order.setStatus(Order.OrderStatus.COMPLETED);
            grantAccessToLessonsInOrder(order);
            finalRedirectUrl = UriComponentsBuilder.fromUriString(localPayment.getSuccessUrl())
                    .queryParam("status", "success").build().toUriString();
        } else {
            localPayment.setStatus(Payment.PaymentStatus.FAILED);
            order.setStatus(Order.OrderStatus.CANCELLED);
            finalRedirectUrl = UriComponentsBuilder.fromUriString(localPayment.getCancelUrl())
                    .queryParam("status", "failed").build().toUriString();
        }

        paymentRepository.save(localPayment);
        orderRepository.save(order);
        return Map.of("redirectUrl", finalRedirectUrl);
    }

    @Transactional
    public Map<String, String> cancelPayPalPayment(String token) {
        Payment localPayment = paymentRepository.findByTransactionId(token)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch với token: " + token));

        Order order = localPayment.getOrder();
        if (localPayment.getStatus() == Payment.PaymentStatus.PENDING || localPayment.getStatus() == Payment.PaymentStatus.PROCESSING) {
            localPayment.setStatus(Payment.PaymentStatus.CANCELLED);
            order.setStatus(Order.OrderStatus.PENDING);
            paymentRepository.save(localPayment);
            orderRepository.save(order);
        }

        String finalRedirectUrl = UriComponentsBuilder.fromUriString(localPayment.getCancelUrl())
                .queryParam("status", "cancelled_by_user").build().toUriString();
        return Map.of("redirectUrl", finalRedirectUrl);
    }

    // Các phương thức khác giữ nguyên
    private void grantAccessToLessonsInOrder(Order order) {
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Lesson lesson = detail.getLesson();
                if (lesson != null) {
                    enrollmentService.enrollUserInLesson(new EnrollmentRequest(
                            order.getUser().getUserId(), lesson.getLessonId()
                    ));
                }
            }
        }
    }
    public PaymentResponse getPaymentById(Integer paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán với ID: " + paymentId));
        return mapToPaymentResponse(payment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll(Sort.by(Sort.Direction.DESC, "paymentDate")).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByUserId(Integer userId) {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        return paymentRepository.findByUser(user).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    public Page<PaymentResponse> searchPayments(PaymentSearchRequest request) {
        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, request.sortBy());
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);
        Page<Payment> payments = paymentRepository.searchPayments(
                request.userId(), request.orderId(), request.paymentMethod(),
                request.transactionId(), request.status(), pageable
        );
        return payments.map(this::mapToPaymentResponse);
    }

    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) { return null; }

    @Transactional
    public void deletePayment(Integer paymentId) {
        paymentRepository.deleteById(paymentId);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        UserResponse userResponse = new UserResponse(
                payment.getUser().getUserId(),
                payment.getUser().getUsername(),
                payment.getUser().getEmail(),
                payment.getUser().getFullName(),
                payment.getUser().getAvatarUrl(),
                payment.getUser().getCreatedAt(),
                payment.getUser().getRole()
        );

        return new PaymentResponse(
                payment.getPaymentId(), userResponse, payment.getOrder().getOrderId(),
                payment.getAmount(), payment.getPaymentDate(), payment.getPaymentMethod(),
                payment.getTransactionId(), payment.getStatus(), payment.getDescription()
        );
    }
}