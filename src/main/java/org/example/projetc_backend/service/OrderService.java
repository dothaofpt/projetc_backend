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
import org.springframework.data.jpa.domain.Specification; // Import này là quan trọng
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import jakarta.persistence.criteria.Predicate; // Import này là quan trọng

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
    private final LessonService lessonService;

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

        Order order = new Order();
        order.setUser(user);
        order.setOrderDate(LocalDateTime.now());
        order.setStatus(Order.OrderStatus.PENDING); // Mặc định là PENDING

        BigDecimal totalAmount = BigDecimal.ZERO;
        List<OrderDetail> orderDetails = new ArrayList<>();

        for (OrderItemRequest itemRequest : request.items()) {
            Lesson lesson = lessonRepository.findById(itemRequest.lessonId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + itemRequest.lessonId()));

            BigDecimal lessonPrice = lesson.getPrice();

            if (lessonPrice == null) {
                throw new IllegalArgumentException("Giá của bài học ID " + itemRequest.lessonId() + " không được xác định.");
            }

            OrderDetail orderDetail = new OrderDetail();
            orderDetail.setLesson(lesson);
            orderDetail.setQuantity(itemRequest.quantity());
            orderDetail.setPriceAtPurchase(lessonPrice); // Lưu giá tại thời điểm mua
            orderDetail.setOrder(order); // Gắn Order (chưa được lưu) vào OrderDetail
            orderDetails.add(orderDetail);
            totalAmount = totalAmount.add(lessonPrice.multiply(BigDecimal.valueOf(itemRequest.quantity())));
        }

        order.setTotalAmount(totalAmount);
        order.setOrderDetails(orderDetails); // QUAN TRỌNG: Gắn danh sách OrderDetail vào Order

        Order savedOrder = orderRepository.save(order); // Lưu Order (và cả OrderDetails nhờ CascadeType.ALL)

        return mapToOrderResponse(savedOrder);
    }

    @Transactional(readOnly = true)
    public OrderResponse getOrderById(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID không được để trống.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));
        return mapToOrderResponse(order);
    }

    @Transactional(readOnly = true)
    public List<OrderResponse> getAllOrders() {
        return orderRepository.findAll().stream()
                .map(this::mapToOrderResponse)
                .collect(Collectors.toList());
    }

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
     * Tìm kiếm đơn hàng dựa trên các tiêu chí tùy chọn.
     * @param userId ID của người dùng (tùy chọn)
     * @param status Trạng thái đơn hàng (tùy chọn)
     * @param minDate Ngày bắt đầu khoảng thời gian (tùy chọn)
     * @param maxDate Ngày kết thúc khoảng thời gian (tùy chọn)
     * @param minTotalAmount Tổng tiền tối thiểu (tùy chọn)
     * @param maxTotalAmount Tổng tiền tối đa (tùy chọn)
     * @param username Tên người dùng (tìm kiếm gần đúng, không phân biệt chữ hoa/thường) (tùy chọn)
     * @return Danh sách các OrderResponse phù hợp với tiêu chí tìm kiếm.
     */
    @Transactional(readOnly = true)
    public List<OrderResponse> searchOrders(
            Integer userId,
            Order.OrderStatus status,
            LocalDateTime minDate,
            LocalDateTime maxDate,
            BigDecimal minTotalAmount,
            BigDecimal maxTotalAmount,
            String username
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
                // Tìm kiếm không phân biệt chữ hoa/thường và theo chuỗi con trong tên người dùng
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("user").get("username")), "%" + username.toLowerCase() + "%"));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        return orderRepository.findAll(spec).stream()
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

        orderRepository.delete(order);
    }


    private OrderResponse mapToOrderResponse(Order order) {
        UserResponse userResponse = null;
        if (order.getUser() != null) {
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

        List<OrderDetailResponse> itemResponses = order.getOrderDetails().stream()
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