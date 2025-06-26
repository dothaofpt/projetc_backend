package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.OrderItemRequest;
import org.example.projetc_backend.dto.OrderDetailResponse;
import org.example.projetc_backend.dto.OrderRequest;
import org.example.projetc_backend.dto.OrderResponse;
import org.example.projetc_backend.dto.UserResponse; // Giả sử bạn có UserResponse DTO
import org.example.projetc_backend.dto.LessonResponse; // Giả sử bạn có LessonResponse DTO
import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.entity.OrderDetail;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.repository.OrderDetailRepository;
import org.example.projetc_backend.repository.OrderRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.LessonRepository;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable; // Bổ sung import Pageable

import jakarta.persistence.criteria.Predicate;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderService {

    private final OrderRepository orderRepository;
    private final OrderDetailRepository orderDetailRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LessonService lessonService; // Để map Lesson Entity sang DTO
    // Đã loại bỏ EnrollmentService khỏi OrderService,
    // logic cấp quyền học sẽ được xử lý trong PaymentService sau khi thanh toán hoàn tất (hoặc EnrollmentService riêng biệt).

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository,
                        UserRepository userRepository, LessonRepository lessonRepository,
                        LessonService lessonService) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.lessonService = lessonService;
    }

    /**
     * Tạo một đơn hàng mới từ yêu cầu của người dùng.
     * Bao gồm việc tạo các chi tiết đơn hàng và tính tổng số tiền.
     *
     * @param request DTO chứa User ID và danh sách các mục (bài học) trong đơn hàng.
     * @return OrderResponse của đơn hàng đã tạo.
     * @throws IllegalArgumentException nếu dữ liệu request không hợp lệ hoặc không tìm thấy người dùng/bài học.
     */
    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        // 1. Xác thực đầu vào cơ bản
        if (request == null || request.userId() == null || request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu đơn hàng phải chứa User ID và ít nhất một mục (item).");
        }

        // 2. Tìm và xác thực người dùng
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        // 3. Khởi tạo Order và các thuộc tính ban đầu
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING); // Mặc định là PENDING khi tạo

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();

        // 4. Xử lý từng OrderItemRequest để tạo OrderDetail
        for (OrderItemRequest itemRequest : request.items()) {
            // Tìm và xác thực bài học
            Lesson lesson = lessonRepository.findById(itemRequest.lessonId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + itemRequest.lessonId() + " trong danh sách đặt hàng."));

            BigDecimal lessonPrice = lesson.getPrice();
            if (lessonPrice == null) {
                throw new IllegalArgumentException("Giá của bài học ID " + itemRequest.lessonId() + " không được xác định.");
            }

            // Tạo OrderDetail và thiết lập các thuộc tính
            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setLesson(lesson);
            orderDetail.setQuantity(itemRequest.quantity());
            orderDetail.setPriceAtPurchase(lessonPrice); // Lưu giá tại thời điểm mua
            orderDetail.setOrder(order); // Gắn Order (chưa được lưu) vào OrderDetail, quan trọng cho cascade persist
            orderDetails.add(orderDetail);

            // Tính toán tổng số tiền
            totalAmount = totalAmount.add(lessonPrice.multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        // 5. Gán tổng số tiền và danh sách OrderDetails vào Order
        order.setTotalAmount(totalAmount);
        order.setOrderDetails(orderDetails); // Gắn danh sách OrderDetail vào Order để cascade persist hoạt động

        // 6. Lưu Order (và cả OrderDetails nhờ CascadeType.ALL và orphanRemoval=true trên Order entity)
        Order savedOrder = orderRepository.save(order);

        // 7. Trả về Response DTO
        return mapToOrderResponse(savedOrder);
    }

    /**
     * Lấy thông tin đơn hàng theo ID.
     *
     * @param orderId ID của đơn hàng.
     * @return OrderResponse của đơn hàng.
     * @throws IllegalArgumentException nếu ID đơn hàng null hoặc không tìm thấy.
     */
    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID không được để trống.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));
        return mapToOrderResponse(order);
    }

    /**
     * Lấy tất cả các đơn hàng trong hệ thống.
     *
     * @return Danh sách OrderResponse.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả các đơn hàng của một người dùng cụ thể.
     *
     * @param userId ID của người dùng.
     * @return Danh sách OrderResponse của người dùng.
     * @throws IllegalArgumentException nếu User ID null hoặc không tìm thấy người dùng.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> getOrdersByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        return orderRepository.findByUser(user).stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm đơn hàng với các tiêu chí tùy chỉnh và phân trang.
     *
     * @param userId ID của người dùng.
     * @param status Trạng thái đơn hàng.
     * @param minDate Ngày bắt đầu khoảng thời gian đặt hàng.
     * @param maxDate Ngày kết thúc khoảng thời gian đặt hàng.
     * @param minTotalAmount Số tiền tối thiểu của đơn hàng.
     * @param maxTotalAmount Số tiền tối đa của đơn hàng.
     * @param username Tên người dùng (tìm kiếm theo một phần).
     * @param pageable Đối tượng Pageable cho phân trang và sắp xếp.
     * @return Trang kết quả chứa OrderResponse.
     */
    @Transactional(readOnly = true)
    public Page<OrderResponse> searchOrders(
            Integer userId,
            Order.OrderStatus status,
            LocalDateTime minDate,
            LocalDateTime maxDate,
            BigDecimal minTotalAmount,
            BigDecimal maxTotalAmount,
            String username,
            Pageable pageable // Bổ sung Pageable
    ) {
        Specification<Order> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (userId != null) {
                predicates.add(criteriaBuilder.equal(root.get("user").get("userId"), userId));
            }
            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }
            if (minDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("orderDate"), minDate));
            }
            if (maxDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("orderDate"), maxDate));
            }
            if (minTotalAmount != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("totalAmount"), minTotalAmount));
            }
            if (maxTotalAmount != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("totalAmount"), maxTotalAmount));
            }
            if (username != null && !username.trim().isEmpty()) {
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("username")), "%" + username.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Sử dụng phương thức findAll(Specification, Pageable)
        return orderRepository.findAll(spec, pageable)
                .map(this::mapToOrderResponse); // Ánh xạ từng Order trong Page sang OrderResponse
    }

    /**
     * Cập nhật trạng thái của một đơn hàng.
     *
     * @param orderId ID của đơn hàng.
     * @param newStatus Trạng thái mới.
     * @return OrderResponse của đơn hàng đã cập nhật.
     * @throws IllegalArgumentException nếu ID đơn hàng hoặc trạng thái mới null, hoặc không tìm thấy đơn hàng.
     */
    @Transactional
    public OrderResponse updateOrderStatus(Integer orderId, Order.OrderStatus newStatus) {
        if (orderId == null || newStatus == null) {
            throw new IllegalArgumentException("Order ID và trạng thái mới không được để trống.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));

        order.setStatus(newStatus);
        Order updatedOrder = orderRepository.save(order);
        return mapToOrderResponse(updatedOrder);
    }

    /**
     * Xóa một đơn hàng.
     * Các chi tiết đơn hàng và thanh toán liên quan cũng sẽ được xóa do cấu hình cascade.
     *
     * @param orderId ID của đơn hàng cần xóa.
     * @throws IllegalArgumentException nếu ID đơn hàng null hoặc không tìm thấy.
     */
    @Transactional
    public void deleteOrder(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID không được để trống.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Khi xóa Order, các OrderDetail và Payment liên quan cũng sẽ được xóa
        // nhờ CascadeType.ALL và orphanRemoval=true đã cấu hình trên Order entity.
        orderRepository.delete(order);
    }

    // --- Helper mapping methods ---

    /**
     * Ánh xạ Order entity sang OrderResponse DTO.
     * Bao gồm ánh xạ User và OrderDetail.
     *
     * @param order Entity Order.
     * @return OrderResponse DTO.
     */
    private OrderResponse mapToOrderResponse(Order order) {
        UserResponse userResponse = null;
        if (order.getUser() != null) {
            // Map User entity sang UserResponse DTO để tránh lộ thông tin nhạy cảm (ví dụ: password)
            // Giả định bạn có một constructor hoặc phương thức map cho UserResponse
            userResponse = new UserResponse(
                    order.getUser().getUserId(),
                    order.getUser().getUsername(),
                    order.getUser().getEmail(),
                    order.getUser().getFullName(),
                    order.getUser().getAvatarUrl(),
                    order.getUser().getCreatedAt(),
                    order.getUser().getRole()
            );
        }

        // Lấy OrderDetails từ Order và map sang OrderDetailResponse
        List<OrderDetailResponse> itemResponses = new ArrayList<>();
        if (order.getOrderDetails() != null) {
            // Đảm bảo OrderDetails được tải (Lazy loading) nếu cần thiết
            // Nếu fetch type là LAZY, bạn có thể cần truy cập chúng để kích hoạt tải
            itemResponses = order.getOrderDetails().stream()
                    .map(this::mapToOrderDetailResponse) // Gọi phương thức ánh xạ OrderDetail
                    .collect(Collectors.toList());
        }

        return new OrderResponse(
                order.getOrderId(),
                userResponse,
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(), // Trạng thái là enum, map thẳng
                order.getShippingAddress(),
                itemResponses
        );
    }

    /**
     * Ánh xạ OrderDetail entity sang OrderDetailResponse DTO.
     *
     * @param orderDetail Entity OrderDetail.
     * @return OrderDetailResponse DTO.
     */
    private OrderDetailResponse mapToOrderDetailResponse(OrderDetail orderDetail) {
        LessonResponse lessonResponse = null;
        if (orderDetail.getLesson() != null) {
            // Sử dụng LessonService để ánh xạ Lesson Entity sang LessonResponse DTO
            lessonResponse = lessonService.mapToLessonResponse(orderDetail.getLesson());
        }

        return new OrderDetailResponse(
                orderDetail.getOrderDetailId(),
                orderDetail.getOrder().getOrderId(),
                lessonResponse,
                orderDetail.getQuantity(),
                orderDetail.getPriceAtPurchase()
        );
    }
}