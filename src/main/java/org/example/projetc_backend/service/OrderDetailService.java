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
    private final LessonService lessonService; // Bổ sung dependency này

    public OrderDetailService(OrderDetailRepository orderDetailRepository, OrderRepository orderRepository,
                              LessonRepository lessonRepository, LessonService lessonService) {
        this.orderDetailRepository = orderDetailRepository;
        this.orderRepository = orderRepository;
        this.lessonRepository = lessonRepository;
        this.lessonService = lessonService; // Inject lessonService
    }

    // Ghi chú: Phương thức `createOrderDetail` đã được loại bỏ như bạn đề xuất.
    // OrderDetails nên được tạo cùng lúc với Order trong OrderService.
    // Các phương thức GET, UPDATE, DELETE cho OrderDetail vẫn được giữ nguyên
    // để quản lý các chi tiết của đơn hàng sau khi Order đã được tạo.

    /**
     * Lấy chi tiết đơn hàng theo ID.
     *
     * @param orderDetailId ID của chi tiết đơn hàng.
     * @return OrderDetailResponse tương ứng.
     * @throws IllegalArgumentException nếu ID chi tiết đơn hàng null hoặc không tìm thấy.
     */
    @Transactional(readOnly = true)
    public OrderDetailResponse getOrderDetailById(Integer orderDetailId) {
        if (orderDetailId == null) {
            throw new IllegalArgumentException("OrderDetail ID không được để trống.");
        }
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn hàng với ID: " + orderDetailId));
        return mapToOrderDetailResponse(orderDetail);
    }

    /**
     * Lấy tất cả các chi tiết đơn hàng của một đơn hàng cụ thể.
     *
     * @param orderId ID của đơn hàng.
     * @return Danh sách OrderDetailResponse.
     * @throws IllegalArgumentException nếu ID đơn hàng null hoặc không tìm thấy.
     */
    @Transactional(readOnly = true)
    public List<OrderDetailResponse> getOrderDetailsByOrderId(Integer orderId) {
        if (orderId == null) {
            throw new IllegalArgumentException("Order ID không được để trống.");
        }
        // Đảm bảo đơn hàng tồn tại trước khi tìm chi tiết của nó
        Order order = orderRepository.findById(orderId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + orderId));

        return orderDetailRepository.findByOrder(order).stream()
                .map(this::mapToOrderDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả các chi tiết đơn hàng trong hệ thống.
     *
     * @return Danh sách OrderDetailResponse.
     */
    @Transactional(readOnly = true)
    public List<OrderDetailResponse> getAllOrderDetails() {
        return orderDetailRepository.findAll().stream()
                .map(this::mapToOrderDetailResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin của một chi tiết đơn hàng.
     * Lưu ý: Việc thay đổi orderId của OrderDetail sẽ tác động đến tổng tiền của hai Order liên quan.
     *
     * @param orderDetailId ID của chi tiết đơn hàng cần cập nhật.
     * @param request DTO chứa thông tin cập nhật.
     * @return OrderDetailResponse của chi tiết đơn hàng đã cập nhật.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc không tìm thấy chi tiết/đơn hàng/bài học.
     */
    @Transactional
    public OrderDetailResponse updateOrderDetail(Integer orderDetailId, OrderDetailRequest request) {
        // 1. Xác thực đầu vào cơ bản
        if (orderDetailId == null || request == null || request.orderId() == null ||
                request.lessonId() == null || request.quantity() == null || request.priceAtPurchase() == null) {
            throw new IllegalArgumentException("OrderDetail ID và yêu cầu cập nhật không được để trống hoặc thiếu thông tin.");
        }

        // 2. Tìm chi tiết đơn hàng cần cập nhật
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn hàng với ID: " + orderDetailId));

        // Lưu trữ Order cũ để cập nhật lại tổng tiền nếu OrderId thay đổi
        Order oldOrder = orderDetail.getOrder();

        // 3. Tìm và xác thực Order mới và Lesson mới
        Order newOrder = orderRepository.findById(request.orderId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đơn hàng với ID: " + request.orderId()));

        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        // 4. Cập nhật thông tin cho OrderDetail
        orderDetail.setOrder(newOrder);
        orderDetail.setLesson(lesson);
        orderDetail.setQuantity(request.quantity());
        orderDetail.setPriceAtPurchase(request.priceAtPurchase());

        OrderDetail updatedOrderDetail = orderDetailRepository.save(orderDetail);

        // 5. Cập nhật lại tổng tiền của các Order bị ảnh hưởng
        // Cập nhật tổng tiền của Order cũ nếu OrderDetail được chuyển sang Order khác
        if (!oldOrder.getOrderId().equals(newOrder.getOrderId())) {
            updateOrderTotalAmount(oldOrder);
        }
        // Luôn cập nhật tổng tiền của Order mới (có thể là Order cũ nếu orderId không đổi)
        updateOrderTotalAmount(newOrder);

        return mapToOrderDetailResponse(updatedOrderDetail);
    }

    /**
     * Xóa một chi tiết đơn hàng.
     * Khi xóa, tổng số tiền của đơn hàng cha sẽ được cập nhật.
     *
     * @param orderDetailId ID của chi tiết đơn hàng cần xóa.
     * @throws IllegalArgumentException nếu ID chi tiết đơn hàng null hoặc không tìm thấy.
     */
    @Transactional
    public void deleteOrderDetail(Integer orderDetailId) {
        if (orderDetailId == null) {
            throw new IllegalArgumentException("OrderDetail ID không được để trống.");
        }
        OrderDetail orderDetail = orderDetailRepository.findById(orderDetailId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy chi tiết đơn hàng với ID: " + orderDetailId));

        Order parentOrder = orderDetail.getOrder(); // Lấy Order cha trước khi xóa OrderDetail
        orderDetailRepository.delete(orderDetail);

        // Cập nhật totalAmount của Order sau khi xóa chi tiết
        updateOrderTotalAmount(parentOrder);
    }

    /**
     * Phương thức trợ giúp để cập nhật tổng số tiền của một Order.
     * Nên được gọi mỗi khi OrderDetails của một Order thay đổi.
     *
     * @param order Đối tượng Order cần cập nhật tổng tiền.
     */
    private void updateOrderTotalAmount(Order order) {
        // Fetch lại orderDetails để đảm bảo dữ liệu mới nhất (sau khi add/update/delete)
        List<OrderDetail> orderDetails = orderDetailRepository.findByOrder(order);
        BigDecimal totalAmount = orderDetails.stream()
                .map(detail -> detail.getPriceAtPurchase().multiply(BigDecimal.valueOf(detail.getQuantity())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        order.setTotalAmount(totalAmount);
        orderRepository.save(order); // Lưu lại Order với totalAmount đã cập nhật
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
                orderDetail.getOrder().getOrderId(), // Lấy orderId từ Order cha
                lessonResponse,
                orderDetail.getQuantity(),
                orderDetail.getPriceAtPurchase()
        );
    }
}