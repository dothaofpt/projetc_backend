package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.PaymentRequest;
import org.example.projetc_backend.dto.PaymentResponse;
import org.example.projetc_backend.dto.UserResponse;
// import org.example.projetc_backend.dto.LessonResponse; // Có thể xóa import này nếu không dùng trực tiếp
import org.example.projetc_backend.entity.Payment;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.entity.OrderDetail; // Cần import để duyệt OrderDetails
import org.example.projetc_backend.entity.Lesson; // Cần import nếu Lesson được truy cập trực tiếp
import org.example.projetc_backend.repository.PaymentRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.OrderRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.paypal.base.rest.PayPalRESTException;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@Service
public class PaymentService {

    private final PaymentRepository paymentRepository;
    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final LessonService lessonService; // Vẫn cần cho việc map Lesson trong OrderDetails của OrderResponse (hoặc để cấp quyền)
    private final PayPalService payPalService;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository,
                          OrderRepository orderRepository, LessonService lessonService, // Có thể cần thêm UserService hoặc một service để quản lý quyền truy cập
                          PayPalService payPalService) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.lessonService = lessonService; // Giả sử lessonService có phương thức để cấp quyền truy cập
        this.payPalService = payPalService;
    }

    // Endpoint này có thể dùng cho các phương thức thanh toán khác ngoài PayPal
    // hoặc là một bước khởi tạo Payment record trước khi chuyển sang cổng thanh toán.
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        if (request == null || request.userId() == null || request.orderId() == null || request.amount() == null) {
            throw new IllegalArgumentException("Yêu cầu thanh toán, User ID, Order ID, và Amount không được để trống.");
        }
        if (request.amount().compareTo(BigDecimal.ZERO) <= 0) {
            throw new IllegalArgumentException("Số tiền thanh toán phải lớn hơn 0.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + request.orderId()));

        if (request.amount().compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("Số tiền thanh toán (" + request.amount() + ") không khớp với tổng giá trị đơn hàng (" + order.getTotalAmount() + ").");
        }

        if (order.getStatus() == Order.OrderStatus.COMPLETED || order.getStatus() == Order.OrderStatus.PROCESSING) {
            throw new IllegalArgumentException("Đơn hàng #" + order.getOrderId() + " đã được xử lý hoặc đang xử lý.");
        }

        if (paymentRepository.findByOrder(order).isPresent()) {
            throw new IllegalArgumentException("Đơn hàng #" + order.getOrderId() + " đã có giao dịch thanh toán liên kết.");
        }

        Payment payment = new Payment();
        payment.setUser(user);
        payment.setOrder(order);
        payment.setAmount(request.amount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setTransactionId(java.util.UUID.randomUUID().toString()); // Tạo Transaction ID nội bộ
        payment.setStatus(Payment.PaymentStatus.PENDING);
        payment.setDescription(request.description() != null ? request.description() : "Thanh toán cho đơn hàng #" + order.getOrderId());

        Payment savedPayment = paymentRepository.save(payment);

        if (order.getStatus() == Order.OrderStatus.PENDING) {
            order.setStatus(Order.OrderStatus.PROCESSING);
            orderRepository.save(order);
        }

        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
    // THAY ĐỔI: Chức năng này sẽ được gọi từ PaymentController với PaymentRequest mới
    // Các tham số cancelUrl và successUrl sẽ được lấy từ request object.
    public String initiatePayPalPayment(Integer userId, Integer orderId, BigDecimal amount, String cancelUrl, String successUrl) throws PayPalRESTException {
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));

        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Đơn hàng này không ở trạng thái PENDING để khởi tạo thanh toán.");
        }
        if (amount.compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("Số tiền thanh toán (" + amount + ") không khớp với tổng giá trị đơn hàng (" + order.getTotalAmount() + ").");
        }

        Optional<Payment> existingPendingPayment = paymentRepository.findByOrder(order);
        if (existingPendingPayment.isPresent() && existingPendingPayment.get().getStatus() == Payment.PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Đơn hàng này đã có giao dịch thanh toán PENDING.");
        }

        Payment localPayment = new Payment();
        localPayment.setUser(user);
        localPayment.setOrder(order);
        localPayment.setAmount(amount);
        localPayment.setPaymentDate(LocalDateTime.now());
        localPayment.setPaymentMethod("PayPal");
        localPayment.setStatus(Payment.PaymentStatus.PENDING);
        localPayment.setDescription("Thanh toán PayPal cho đơn hàng #" + order.getOrderId());

        com.paypal.api.payments.Payment paypalPayment = payPalService.createPayment(
                amount.doubleValue(),
                "USD", // ĐẢM BẢO CURRENCY NÀY KHỚP VỚI CẤU HÌNH PAYPAL VÀ SỐ TIỀN THỰC TẾ
                "paypal",
                "sale",
                localPayment.getDescription(),
                cancelUrl,
                successUrl
        );

        localPayment.setTransactionId(paypalPayment.getId()); // ID của PayPal transaction
        paymentRepository.save(localPayment);

        order.setStatus(Order.OrderStatus.PROCESSING); // Đơn hàng đang chờ PayPal xử lý
        orderRepository.save(order);

        for (com.paypal.api.payments.Links link : paypalPayment.getLinks()) {
            if (link.getRel().equals("approval_url")) {
                return link.getHref();
            }
        }
        throw new IllegalStateException("Không tìm thấy URL phê duyệt từ PayPal.");
    }

    @Transactional
    public PaymentResponse completePayPalPayment(String paypalPaymentId, String payerId) throws PayPalRESTException {
        Payment localPayment = paymentRepository.findByTransactionId(paypalPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch PayPal cục bộ với ID: " + paypalPaymentId));

        if (localPayment.getStatus() != Payment.PaymentStatus.PENDING) {
            // Nếu giao dịch đã được xử lý (COMPLETED/FAILED/CANCELLED), không cần xử lý lại
            return mapToPaymentResponse(localPayment);
        }

        com.paypal.api.payments.Payment executedPayment = payPalService.executePayment(paypalPaymentId, payerId);

        Order order = localPayment.getOrder();

        if (executedPayment.getState().equals("approved")) {
            localPayment.setStatus(Payment.PaymentStatus.COMPLETED);
            order.setStatus(Order.OrderStatus.COMPLETED);

            // BỔ SUNG: LOGIC CẤP QUYỀN TRUY CẬP BÀI HỌC/KHÓA HỌC SAU KHI THANH TOÁN HOÀN TẤT
            // Duyệt qua các OrderDetail trong Order để lấy thông tin các bài học/khóa học đã mua
            // và cấp quyền truy cập cho User (localPayment.getUser())
            if (order.getOrderDetails() != null) {
                for (OrderDetail detail : order.getOrderDetails()) {
                    Lesson lesson = detail.getLesson(); // Giả sử OrderDetail có trường 'lesson' hoặc 'course'
                    if (lesson != null) {
                        // Gọi một phương thức trong LessonService hoặc một service khác
                        // để cập nhật trạng thái truy cập của người dùng cho bài học này
                        // Ví dụ: lessonService.grantAccessToLesson(order.getUser(), lesson);
                        // Cần đảm bảo LessonService có phương thức này và xử lý lưu vào DB
                        System.out.println("Cấp quyền truy cập bài học: " + lesson.getTitle() + " cho người dùng: " + order.getUser().getUsername());
                        // Ví dụ thực tế:
                        // userLessonAccessService.grantAccess(order.getUser().getUserId(), lesson.getLessonId());
                        // Cần thêm Service và Entity UserLessonAccess nếu chưa có
                    }
                }
            }

        } else if (executedPayment.getState().equals("failed") || executedPayment.getState().equals("denied")) {
            localPayment.setStatus(Payment.PaymentStatus.FAILED);
            order.setStatus(Order.OrderStatus.CANCELLED); // Hoặc FAILED_PAYMENT nếu bạn có trạng thái đó
        } else {
            localPayment.setStatus(Payment.PaymentStatus.PENDING); // Giữ nguyên trạng thái PENDING nếu PayPal chưa xác nhận
        }

        localPayment.setPaymentDate(LocalDateTime.now());
        Payment savedPayment = paymentRepository.save(localPayment);
        orderRepository.save(order);

        return mapToPaymentResponse(savedPayment);
    }

    // Các phương thức khác (getPaymentById, getAllPayments, etc.) giữ nguyên và không cần thay đổi
    public PaymentResponse getPaymentById(Integer paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID không được để trống.");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán với ID: " + paymentId));
        return mapToPaymentResponse(payment);
    }

    public List<PaymentResponse> getAllPayments() {
        return paymentRepository.findAll().stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    public List<PaymentResponse> getPaymentsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        return paymentRepository.findByUser(user).stream()
                .map(this::mapToPaymentResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public void deletePayment(Integer paymentId) {
        if (paymentId == null) {
            throw new IllegalArgumentException("Payment ID không được để trống.");
        }
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán với ID: " + paymentId));
        paymentRepository.delete(payment);
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        UserResponse userResponse = null;
        if (payment.getUser() != null) {
            userResponse = new UserResponse(
                    payment.getUser().getUserId(),
                    payment.getUser().getUsername(),
                    payment.getUser().getEmail(),
                    payment.getUser().getFullName(),
                    payment.getUser().getAvatarUrl(),
                    payment.getUser().getCreatedAt(),
                    payment.getUser().getRole().toString()
            );
        }

        Integer orderId = null;
        if (payment.getOrder() != null) {
            orderId = payment.getOrder().getOrderId();
        }

        return new PaymentResponse(
                payment.getPaymentId(),
                userResponse,
                orderId,
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getDescription()
        );
    }
}