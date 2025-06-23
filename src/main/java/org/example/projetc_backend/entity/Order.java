package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal;
import java.util.List; // Import List

@Entity
@Table(name = "Orders")
@Data
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Người dùng tạo đơn hàng

    @Column(name = "order_date", nullable = false)
    private LocalDateTime orderDate; // Ngày tạo đơn hàng

    @Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal totalAmount; // Tổng số tiền của đơn hàng

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private OrderStatus status; // Trạng thái của đơn hàng (ví dụ: PENDING, COMPLETED, CANCELLED)

    @Column(name = "shipping_address", columnDefinition = "TEXT") // Nếu có địa chỉ giao hàng (ít khả năng cho Lesson)
    private String shippingAddress;

    // Liên kết 1-1 với Payment. 'mappedBy' chỉ ra rằng trường 'order' trong Payment entity là chủ sở hữu của mối quan hệ này.
    @OneToOne(mappedBy = "order", cascade = CascadeType.ALL, fetch = FetchType.LAZY, orphanRemoval = true)
    private Payment payment; // Liên kết với giao dịch thanh toán

    // Liên kết 1-nhiều với OrderDetail
    // CascadeType.ALL: các thao tác (persist, merge, remove) trên Order sẽ ảnh hưởng đến OrderDetail liên quan.
    // orphanRemoval = true: Nếu một OrderDetail bị xóa khỏi danh sách orderDetails, nó sẽ bị xóa khỏi cơ sở dữ liệu.
    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<OrderDetail> orderDetails;

    public enum OrderStatus {
        PENDING,         // Đang chờ xử lý (chưa thanh toán hoặc đang chờ xác nhận)
        COMPLETED,       // Đã hoàn thành (đã thanh toán đầy đủ và xử lý xong)
        CANCELLED,       // Đã hủy (bởi người dùng hoặc hệ thống)
        PROCESSING       // Đang xử lý (đã thanh toán nhưng chưa hoàn thành cung cấp dịch vụ)
    }

    @PrePersist
    protected void onCreate() {
        if (orderDate == null) {
            orderDate = LocalDateTime.now();
        }
        if (status == null) {
            status = OrderStatus.PENDING; // Mặc định là PENDING khi tạo mới
        }
    }
}