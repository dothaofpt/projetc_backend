package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.PaymentRequest;
import org.example.projetc_backend.dto.PaymentResponse;
import org.example.projetc_backend.dto.UserResponse;
 import org.example.projetc_backend.dto.LessonResponse; // Có thể xóa import này nếu không dùng trực tiếp
import org.example.projetc_backend.entity.Payment;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Order;
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
    private final LessonService lessonService; // Vẫn cần cho việc map Lesson trong OrderDetails của OrderResponse
    private final PayPalService payPalService;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository,
                          OrderRepository orderRepository, LessonService lessonService,
                          PayPalService payPalService) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.lessonService = lessonService;
        this.payPalService = payPalService;
    }

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

        // THAY ĐỔI NHỎ: Kiểm tra số tiền chính xác hơn
        if (request.amount().compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("Số tiền thanh toán (" + request.amount() + ") không khớp với tổng giá trị đơn hàng (" + order.getTotalAmount() + ").");
        }

        // THAY ĐỔI NHỎ: Kiểm tra trạng thái đơn hàng để tránh thanh toán lại
        if (order.getStatus() == Order.OrderStatus.COMPLETED || order.getStatus() == Order.OrderStatus.PROCESSING) {
            throw new IllegalArgumentException("Đơn hàng #" + order.getOrderId() + " đã được xử lý hoặc đang xử lý.");
        }

        // THAY ĐỔI NHỎ: Kiểm tra xem đã có Payment cho Order này chưa.
        // Mặc dù bạn đã có unique = true trên order_id trong entity Payment,
        // việc kiểm tra trước sẽ giúp hiển thị lỗi thân thiện hơn.
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

        // THAY ĐỔI NHỎ: Luôn cập nhật trạng thái đơn hàng nếu thanh toán được tạo thành công
        // Nếu payment này là bước đầu tiên trong luồng thanh toán (ví dụ: cho các cổng khác PayPal)
        // Order có thể chuyển từ PENDING sang PROCESSING hoặc COMPLETED tùy thuộc vào phương thức.
        // Với PayPal, trạng thái sẽ được xử lý trong initiatePayPalPayment.
        // Bạn có thể bỏ đoạn này nếu chỉ dùng initiatePayPalPayment làm entry point
        // và muốn trạng thái order chỉ được update sau khi PayPal phản hồi.
        if (order.getStatus() == Order.OrderStatus.PENDING) {
            order.setStatus(Order.OrderStatus.PROCESSING); // Hoặc giữ nguyên PENDING nếu chỉ để theo dõi trạng thái payment
            orderRepository.save(order);
        }

        return mapToPaymentResponse(savedPayment);
    }

    @Transactional
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

        // THAY ĐỔI NHỎ: Kiểm tra xem đã có Payment PENDING cho Order này chưa
        Optional<Payment> existingPendingPayment = paymentRepository.findByOrder(order);
        if (existingPendingPayment.isPresent() && existingPendingPayment.get().getStatus() == Payment.PaymentStatus.PENDING) {
            // Có thể trả về URL hiện có hoặc báo lỗi nếu muốn mỗi Order chỉ có 1 lần khởi tạo
            // return "Đơn hàng này đã có giao dịch thanh toán PENDING. URL: " + existingPendingPayment.get().getDescription(); // Ví dụ
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
                "USD", // THAY ĐỔI CẦN LƯU Ý: Currency cần được cấu hình chính xác, ví dụ "VND" nếu PayPal hỗ trợ hoặc chuyển đổi.
                "paypal",
                "sale",
                localPayment.getDescription(),
                cancelUrl,
                successUrl
        );

        localPayment.setTransactionId(paypalPayment.getId()); // ID của PayPal transaction
        paymentRepository.save(localPayment);

        // THAY ĐỔI NHỎ: Trạng thái Order nên chuyển sang PROCESSING ngay sau khi khởi tạo PayPal
        // để tránh người dùng khởi tạo lại hoặc thanh toán bằng phương thức khác cùng lúc.
        order.setStatus(Order.OrderStatus.PROCESSING);
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
        // Lấy Payment cục bộ trước để có Order
        Payment localPayment = paymentRepository.findByTransactionId(paypalPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch PayPal cục bộ với ID: " + paypalPaymentId));

        // Kiểm tra xem Payment đã hoàn thành/thất bại chưa
        if (localPayment.getStatus() != Payment.PaymentStatus.PENDING) {
            // Có thể trả về PaymentResponse hiện tại hoặc báo lỗi nếu muốn
            return mapToPaymentResponse(localPayment);
        }

        com.paypal.api.payments.Payment executedPayment = payPalService.executePayment(paypalPaymentId, payerId);

        Order order = localPayment.getOrder();

        if (executedPayment.getState().equals("approved")) {
            localPayment.setStatus(Payment.PaymentStatus.COMPLETED);
            order.setStatus(Order.OrderStatus.COMPLETED);
            // THÊM LOGIC GÁN QUYỀN TRUY CẬP BÀI HỌC Ở ĐÂY SAU KHI THANH TOÁN HOÀN TẤT
            // Ví dụ: user.addAccessToLesson(lesson);
            // Hoặc bạn có thể có một service riêng để xử lý việc kích hoạt khóa học.
        } else if (executedPayment.getState().equals("failed") || executedPayment.getState().equals("denied")) {
            localPayment.setStatus(Payment.PaymentStatus.FAILED);
            order.setStatus(Order.OrderStatus.CANCELLED); // Hoặc FAILED_PAYMENT nếu bạn có trạng thái đó
        } else {
            // Nếu trạng thái khác approved/failed/denied (ví dụ: created, pending)
            // Có thể giữ nguyên PENDING hoặc xử lý cụ thể hơn tùy vào luồng của PayPal
            localPayment.setStatus(Payment.PaymentStatus.PENDING);
            // order.setStatus(Order.OrderStatus.PROCESSING); // Giữ nguyên trạng thái Processing
        }

        localPayment.setPaymentDate(LocalDateTime.now()); // Cập nhật thời gian hoàn tất/thất bại
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
        // Bạn có thể cân nhắc logic khi xóa payment: có nên hoàn tiền, thay đổi trạng thái order không?
        // Ví dụ:
        // Order order = payment.getOrder();
        // if (order != null && order.getStatus() == Order.OrderStatus.COMPLETED) {
        //     order.setStatus(Order.OrderStatus.CANCELLED); // Hoặc một trạng thái "refunded"
        //     orderRepository.save(order);
        // }
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

        // THAY ĐỔI: Sử dụng orderId từ payment thay vì LessonResponse
        Integer orderId = null;
        if (payment.getOrder() != null) {
            orderId = payment.getOrder().getOrderId();
        }

        return new PaymentResponse(
                payment.getPaymentId(),
                userResponse,
                orderId, // Trả về orderId
                payment.getAmount(),
                payment.getPaymentDate(),
                payment.getPaymentMethod(),
                payment.getTransactionId(),
                payment.getStatus(),
                payment.getDescription()
        );
    }
}