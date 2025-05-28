package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.OrderDetailRequest;
import org.example.projetc_backend.dto.OrderDetailResponse;
import org.example.projetc_backend.dto.LessonResponse;
import org.example.projetc_backend.entity.OrderDetail;
import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.repository.OrderDetailRepository;
import org.example.projetc_backend.repository.OrderRepository;
import org.example.projetc_backend.repository.LessonRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class OrderDetailService {

    private final OrderDetailRepository orderDetailRepository;
    private final OrderRepository orderRepository;
    private final LessonRepository lessonRepository;
    private final LessonService lessonService;

    public OrderDetailService(OrderDetailRepository orderDetailRepository, OrderRepository orderRepository,
                              LessonRepository lessonRepository, LessonService lessonService) {
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.lessonRepository = lessonRepository;
        this.lessonService = lessonService;
    }

    // THAY ĐỔI LỚN: Loại bỏ phương thức createOrderDetail,
    // vì OrderDetails nên được tạo cùng lúc với Order trong OrderService.

    public OrderDetailResponse getOrderDetailById(Integer orderDetailId) {
        if (orderDetailId == null) {
            throw new IllegalArgumentException("OrderDetail ID không được để trống.");
        }
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn hàng với ID: " + orderDetailId));
        return mapToOrderDetailResponse(orderDetail);
    }

    public List<OrderDetailResponse> getOrderDetailsByOrderId(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID không được để trống.");
        }
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));
        return orderDetailRepository.findByOrder(order).stream()
                .map(this::mapToOrderDetailResponse)
                .collect(Collectors.toList());
    }

    public List<OrderDetailResponse> getAllOrderDetails() {
        return orderDetailRepository.findAll().stream()
                .map(this::mapToOrderDetailResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public OrderDetailResponse updateOrderDetail(Integer orderDetailId, OrderDetailRequest request) {
        if (orderDetailId == null || request == null || request.orderId() == null ||
                request.lessonId() == null || request.quantity() == null || request.priceAtPurchase() == null) {
            throw new IllegalArgumentException("OrderDetail ID và yêu cầu cập nhật không được để trống.");
        }

        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn hàng với ID: " + orderDetailId));

        Order oldOrder = orderDetail.getOrder(); // Lấy order cũ để có thể cập nhật lại nếu order thay đổi
        Order newOrder = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + request.orderId()));

        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        orderDetail.setOrder(newOrder); // Cập nhật order cha
        orderDetail.setLesson(lesson);
        orderDetail.setQuantity(request.quantity());
        orderDetail.setPriceAtPurchase(request.priceAtPurchase());

        OrderDetail updatedOrderDetail = orderDetailRepository.save(orderDetail);

        // Cập nhật totalAmount của Order cũ (nếu order thay đổi) và Order mới
        if (!oldOrder.getOrderId().equals(newOrder.getOrderId())) {
            updateOrderTotalAmount(oldOrder); // Cập nhật lại tổng tiền của đơn hàng cũ
        }
        updateOrderTotalAmount(newOrder); // Luôn cập nhật tổng tiền của đơn hàng mới

        return mapToOrderDetailResponse(updatedOrderDetail);
    }

    @Transactional
    public void deleteOrderDetail(Integer orderDetailId) {
        if (orderDetailId == null) {
            throw new IllegalArgumentException("OrderDetail ID không được để trống.");
        }
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn hàng với ID: " + orderDetailId));

        Order parentOrder = orderDetail.getOrder();
        orderDetailRepository.delete(orderDetail);

        // Cập nhật totalAmount của Order sau khi xóa
        updateOrderTotalAmount(parentOrder);
    }

    // Helper method để cập nhật totalAmount của Order
    private void updateOrderTotalAmount(Order order) {
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        BigDecimal totalAmount = orderDetails.stream()
                .map(detail -> detail.getPriceAtPurchase().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        orderRepository.save(order); // Lưu lại Order để cập nhật totalAmount
    }

    // Map OrderDetail entity to OrderDetailResponse DTO
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