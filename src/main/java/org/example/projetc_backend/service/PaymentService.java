package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.PaymentRequest;
import org.example.projetc_backend.dto.PaymentResponse;
import org.example.projetc_backend.dto.PaymentSearchRequest; // Bổ sung import này
import org.example.projetc_backend.dto.UserResponse;
import org.example.projetc_backend.entity.Payment;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.entity.OrderDetail;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.repository.PaymentRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.OrderRepository;
import org.springframework.data.domain.Page; // Bổ sung import này
import org.springframework.data.domain.PageRequest; // Bổ sung import này
import org.springframework.data.domain.Sort; // Bổ sung import này
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
    private final EnrollmentService enrollmentService; // Sử dụng EnrollmentService để cấp quyền
    private final PayPalService payPalService;

    public PaymentService(PaymentRepository paymentRepository, UserRepository userRepository,
                          OrderRepository orderRepository, EnrollmentService enrollmentService,
                          PayPalService payPalService) {
        this.paymentRepository = paymentRepository;
        this.userRepository = userRepository;
        this.orderRepository = orderRepository;
        this.enrollmentService = enrollmentService; // Inject EnrollmentService
        this.payPalService = payPalService;
    }

    /**
     * Tạo một bản ghi thanh toán trong hệ thống.
     * Phương thức này có thể được gọi cho các phương thức thanh toán không phải PayPal.
     * Đối với PayPal, initiatePayPalPayment sẽ được sử dụng trước.
     * @param request PaymentRequest chứa thông tin thanh toán.
     * @return PaymentResponse của giao dịch đã tạo.
     */
    @Transactional
    public PaymentResponse createPayment(PaymentRequest request) {
        if (request == null || request.userId() == null || request.orderId() == null || request.amount() == null ||
                request.paymentMethod() == null || request.paymentMethod().trim().isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu thanh toán, User ID, Order ID, Amount, và Payment Method không được để trống.");
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

        if (order.getStatus() == Order.OrderStatus.COMPLETED) { // Nếu đã hoàn tất thì không tạo thanh toán mới
            throw new IllegalArgumentException("Đơn hàng #" + order.getOrderId() + " đã hoàn tất.");
        }

        // Kiểm tra nếu đã có thanh toán PENDING hoặc COMPLETED cho đơn hàng này
        Optional<Payment> existingPayment = paymentRepository.findByOrder(order);
        if (existingPayment.isPresent() && (existingPayment.get().getStatus() == Payment.PaymentStatus.PENDING || existingPayment.get().getStatus() == Payment.PaymentStatus.COMPLETED)) {
            throw new IllegalArgumentException("Đơn hàng #" + order.getOrderId() + " đã có giao dịch thanh toán liên kết ở trạng thái PENDING hoặc COMPLETED.");
        }


        Payment payment = new Payment();
        payment.setUser(user);
        payment.setOrder(order);
        payment.setAmount(request.amount());
        payment.setPaymentDate(LocalDateTime.now());
        payment.setPaymentMethod(request.paymentMethod());
        payment.setTransactionId(java.util.UUID.randomUUID().toString()); // Tạo Transaction ID nội bộ
        payment.setStatus(Payment.PaymentStatus.COMPLETED); // Giả định thanh toán thành công ngay lập tức
        payment.setDescription(request.description() != null ? request.description() : "Thanh toán cho đơn hàng #" + order.getOrderId());

        Payment savedPayment = paymentRepository.save(payment);

        // Cập nhật trạng thái đơn hàng nếu chưa phải COMPLETED
        if (order.getStatus() != Order.OrderStatus.COMPLETED) {
            order.setStatus(Order.OrderStatus.COMPLETED);
            orderRepository.save(order);
            // Cấp quyền truy cập bài học
            grantAccessToLessonsInOrder(order);
        }

        return mapToPaymentResponse(savedPayment);
    }

    /**
     * Khởi tạo quá trình thanh toán PayPal, tạo bản ghi Payment ở trạng thái PENDING.
     * @param request PaymentRequest chứa thông tin cần thiết, bao gồm cancelUrl và successUrl.
     * @return URL phê duyệt của PayPal để người dùng chuyển hướng.
     * @throws PayPalRESTException nếu có lỗi khi tương tác với PayPal API.
     */
    @Transactional
    public String initiatePayPalPayment(PaymentRequest request) throws PayPalRESTException {
        if (request == null || request.userId() == null || request.orderId() == null || request.amount() == null ||
                request.cancelUrl() == null || request.cancelUrl().trim().isEmpty() ||
                request.successUrl() == null || request.successUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu thanh toán (userId, orderId, amount, cancelUrl, successUrl) không được để trống.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        Order order = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + request.orderId()));

        if (order.getStatus() != Order.OrderStatus.PENDING) {
            throw new IllegalArgumentException("Đơn hàng này không ở trạng thái PENDING để khởi tạo thanh toán.");
        }
        if (request.amount().compareTo(order.getTotalAmount()) != 0) {
            throw new IllegalArgumentException("Số tiền thanh toán (" + request.amount() + ") không khớp với tổng giá trị đơn hàng (" + order.getTotalAmount() + ").");
        }

        Optional<Payment> existingPendingPayment = paymentRepository.findByOrder(order);
        if (existingPendingPayment.isPresent() && existingPendingPayment.get().getStatus() == Payment.PaymentStatus.PENDING) {
            throw new IllegalArgumentException("Đơn hàng này đã có giao dịch thanh toán PENDING.");
        }

        // Tạo bản ghi Payment cục bộ ở trạng thái PENDING
        Payment localPayment = new Payment();
        localPayment.setUser(user);
        localPayment.setOrder(order);
        localPayment.setAmount(request.amount());
        localPayment.setPaymentDate(LocalDateTime.now());
        localPayment.setPaymentMethod("PayPal");
        localPayment.setStatus(Payment.PaymentStatus.PENDING);
        localPayment.setDescription(request.description() != null ? request.description() : "Thanh toán PayPal cho đơn hàng #" + order.getOrderId());

        com.paypal.api.payments.Payment paypalPayment = payPalService.createPayment(
                request.amount().doubleValue(),
                "USD", // ĐẢM BẢO CURRENCY NÀY KHỚP VỚI CẤU HÌNH PAYPAL VÀ SỐ TIỀN THỰC TẾ
                "paypal",
                "sale",
                localPayment.getDescription(),
                request.cancelUrl(), // Lấy từ request
                request.successUrl() // Lấy từ request
        );

        localPayment.setTransactionId(paypalPayment.getId()); // Lưu ID của PayPal transaction
        paymentRepository.save(localPayment);

        order.setStatus(Order.OrderStatus.PROCESSING); // Đặt trạng thái đơn hàng là PROCESSING
        orderRepository.save(order);

        for (com.paypal.api.payments.Links link : paypalPayment.getLinks()) {
            if (link.getRel().equals("approval_url")) {
                return link.getHref();
            }
        }
        throw new IllegalStateException("Không tìm thấy URL phê duyệt từ PayPal.");
    }

    /**
     * Hoàn tất quá trình thanh toán PayPal sau khi người dùng chấp thuận trên PayPal.
     * @param paypalPaymentId ID giao dịch PayPal.
     * @param payerId ID người thanh toán từ PayPal.
     * @return PaymentResponse của giao dịch đã hoàn tất.
     * @throws PayPalRESTException nếu có lỗi khi tương tác với PayPal API.
     */
    @Transactional
    public PaymentResponse completePayPalPayment(String paypalPaymentId, String payerId) throws PayPalRESTException {
        if (paypalPaymentId == null || paypalPaymentId.trim().isEmpty() || payerId == null || payerId.trim().isEmpty()) {
            throw new IllegalArgumentException("PayPal Payment ID và Payer ID không được để trống.");
        }

        Payment localPayment = paymentRepository.findByTransactionId(paypalPaymentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy giao dịch PayPal cục bộ với ID: " + paypalPaymentId));

        if (localPayment.getStatus() == Payment.PaymentStatus.COMPLETED) {
            // Nếu giao dịch đã được xử lý thành công, không cần xử lý lại
            return mapToPaymentResponse(localPayment);
        }
        if (localPayment.getStatus() == Payment.PaymentStatus.FAILED || localPayment.getStatus() == Payment.PaymentStatus.REFUNDED) {
            throw new IllegalArgumentException("Giao dịch này đã ở trạng thái FAILED hoặc REFUNDED, không thể hoàn tất.");
        }


        com.paypal.api.payments.Payment executedPayment = payPalService.executePayment(paypalPaymentId, payerId);

        Order order = localPayment.getOrder();

        if (executedPayment.getState().equals("approved")) {
            localPayment.setStatus(Payment.PaymentStatus.COMPLETED);
            order.setStatus(Order.OrderStatus.COMPLETED);

            // Cấp quyền truy cập bài học sau khi thanh toán hoàn tất
            grantAccessToLessonsInOrder(order);

        } else if (executedPayment.getState().equals("failed") || executedPayment.getState().equals("denied")) {
            localPayment.setStatus(Payment.PaymentStatus.FAILED);
            order.setStatus(Order.OrderStatus.CANCELLED); // Hoặc FAILED_PAYMENT nếu bạn có trạng thái đó
        } else {
            localPayment.setStatus(Payment.PaymentStatus.PENDING); // Giữ nguyên trạng thái PENDING nếu PayPal chưa xác nhận
            // Có thể có logic retry hoặc thông báo cho admin ở đây
        }

        localPayment.setPaymentDate(LocalDateTime.now()); // Cập nhật ngày thanh toán cuối cùng
        Payment savedPayment = paymentRepository.save(localPayment);
        orderRepository.save(order); // Lưu trạng thái order đã cập nhật

        return mapToPaymentResponse(savedPayment);
    }

    /**
     * Cấp quyền truy cập cho người dùng đối với các bài học trong đơn hàng đã hoàn tất.
     * @param order Đối tượng Order đã được hoàn tất thanh toán.
     */
    private void grantAccessToLessonsInOrder(Order order) {
        if (order.getOrderDetails() != null) {
            for (OrderDetail detail : order.getOrderDetails()) {
                Lesson lesson = detail.getLesson();
                if (lesson != null) {
                    // Gọi EnrollmentService để đăng ký người dùng vào bài học
                    enrollmentService.enrollUserInLesson(new org.example.projetc_backend.dto.EnrollmentRequest(
                            order.getUser().getUserId(), lesson.getLessonId()
                    ));
                    System.out.println("Đã cấp quyền truy cập bài học: " + lesson.getTitle() + " cho người dùng: " + order.getUser().getUsername());
                }
            }
        }
    }


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

    public PaymentResponse getPaymentByOrderId(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID không được để trống.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));
        Payment payment = paymentRepository.findByOrder(order)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy thanh toán cho đơn hàng ID: " + orderId));
        return mapToPaymentResponse(payment);
    }

    public Page<PaymentResponse> searchPayments(PaymentSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        String sortBy = request.sortBy();
        if (!List.of("paymentId", "userId", "orderId", "amount", "paymentDate", "status", "transactionId").contains(sortBy)) {
            sortBy = "paymentId";
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<Payment> payments = paymentRepository.searchPayments(
                request.userId(),
                request.orderId(),
                request.paymentMethod(),
                request.transactionId(),
                request.status(),
                pageable
        );
        return payments.map(this::mapToPaymentResponse);
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
                    payment.getUser().getRole() // Enum role
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
                payment.getStatus(), // Enum status
                payment.getDescription()
        );
    }
}