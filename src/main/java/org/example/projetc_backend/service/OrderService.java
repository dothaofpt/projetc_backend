package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.OrderItemRequest;
import org.example.projetc_backend.dto.OrderDetailResponse;
import org.example.projetc_backend.dto.OrderRequest;
import org.example.projetc_backend.dto.OrderResponse;
import org.example.projetc_backend.dto.UserResponse;
import org.example.projetc_backend.dto.LessonResponse;
import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.entity.OrderDetail;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.repository.OrderDetailRepository;
import org.example.projetc_backend.repository.OrderRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final LessonService lessonService; // Để map Lesson sang DTO

    public OrderService(OrderRepository orderRepository, OrderDetailRepository orderDetailRepository, UserRepository userRepository, LessonRepository lessonRepository, LessonService lessonService) {
        this.orderRepository = orderRepository;
        this.orderDetailRepository = orderDetailRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.lessonService = lessonService;
    }

    @Transactional
    public OrderResponse createOrder(OrderRequest request) {
        if (request == null || request.userId() == null || request.items() == null || request.items().isEmpty()) {
            throw new IllegalArgumentException("Yêu cầu đơn hàng, User ID và các mục không được để trống.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        // Khởi tạo Order entity
        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING); // Mặc định là PENDING

        // Tính toán totalAmount và chuẩn bị OrderDetails
        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.items()) {
            Lesson lesson = lessonRepository.findById(itemRequest.lessonId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + itemRequest.lessonId()));

            // LỖI NẰM Ở ĐÂY TRƯỚC ĐÓ: Bạn đã dùng giá trị cố định.
            // Bây giờ chúng ta lấy giá từ Lesson entity đã tìm thấy.
            BigDecimal lessonPrice = lesson.getPrice(); // <-- ĐÂY LÀ SỬA CHỮA QUAN TRỌNG NHẤT!

            if (lessonPrice == null) { // Đảm bảo giá không phải là null từ DB
                throw new IllegalArgumentException("Giá của bài học ID " + itemRequest.lessonId() + " không được xác định.");
            }

            // Tạo OrderDetail entity
            OrderDetail orderDetail = new OrderDetail();
            // orderDetail.setOrder(order); // Sẽ được set sau khi order được lưu lần đầu
            orderDetail.setLesson(lesson);
            orderDetail.setQuantity(itemRequest.quantity());
            orderDetail.setPriceAtPurchase(lessonPrice); // Lưu giá tại thời điểm mua

            orderDetails.add(orderDetail);
            totalAmount = totalAmount.add(lessonPrice.multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        // Đặt totalAmount cho Order trước khi lưu lần đầu
        order.setTotalAmount(totalAmount);

        // Lưu Order vào Database để có orderId (nó sẽ được gắn với OrderDetail)
        Order savedOrder = orderRepository.save(order);

        // Gắn orderId cho từng OrderDetail và lưu chúng
        for (OrderDetail detail : orderDetails) {
            detail.setOrder(savedOrder); // Gán Order đã lưu vào OrderDetail
        }
        orderDetailRepository.saveAll(orderDetails); // Lưu tất cả chi tiết đơn hàng


        // Không cần save lại savedOrder ở đây vì totalAmount đã được set trước khi save lần đầu
        // và đối tượng `order` vẫn trong cùng transaction nếu bạn muốn cập nhật thêm

        return mapToOrderResponse(savedOrder);
    }

    public OrderResponse getOrderById(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID không được để trống.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));
        return mapToOrderResponse(order);
    }

    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

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

    @Transactional
    public void deleteOrder(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID không được để trống.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));

        // Xóa các OrderDetail liên quan trước (hoặc dùng CascadeType.ALL trên Order entity)
        // Nếu bạn có CascadeType.ALL trên @OneToMany của Order, bạn có thể bỏ dòng này
        orderDetailRepository.deleteAll(orderDetailRepository.findByOrder(order));

        // Sau đó xóa Order
        orderRepository.delete(order);
    }


    private OrderResponse mapToOrderResponse(Order order) {
        UserResponse userResponse = null;
        if (order.getUser() != null) {
            // Đảm bảo UserResponse constructor khớp với số lượng tham số
            userResponse = new UserResponse(
                    order.getUser().getUserId(),
                    order.getUser().getUsername(),
                    order.getUser().getEmail(),
                    order.getUser().getFullName(),
                    order.getUser().getAvatarUrl(),
                    order.getUser().getCreatedAt(),
                    order.getUser().getRole().toString()
            );
        }

        List<OrderDetailResponse> itemResponses = orderDetailRepository.findByOrder(order).stream()
                .map(this::mapToOrderDetailResponse)
                .collect(Collectors.toList());

        return new OrderResponse(
                order.getOrderId(),
                userResponse,
                order.getOrderDate(),
                order.getTotalAmount(),
                order.getStatus(),
                order.getShippingAddress(),
                itemResponses
        );
    }

    private OrderDetailResponse mapToOrderDetailResponse(OrderDetail orderDetail) {
        LessonResponse lessonResponse = null;
        if (orderDetail.getLesson() != null) {
            // Đảm bảo LessonService.mapToLessonResponse là public và constructor LessonResponse khớp
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