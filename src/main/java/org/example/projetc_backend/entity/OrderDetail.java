package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;

@Entity
@Table(name = "Order_Details")
@Data
public class OrderDetail {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer orderDetailId;

    @ManyToOne
    @JoinColumn(name = "order_id", nullable = false)
    private Order order; // Liên kết với đơn hàng cha

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson; // Liên kết với bài học được mua

    @Column(name = "quantity", nullable = false)
    private Integer quantity; // Số lượng (thường là 1 cho các bài học)

    @Column(name = "price_at_purchase", nullable = false, precision = 10, scale = 2)
    private BigDecimal priceAtPurchase; // Giá của bài học tại thời điểm mua (để tránh thay đổi giá sau này)

    // Constructors (nếu không dùng Lombok @Data hoặc cần thêm logic)
    public OrderDetail() {}

    public OrderDetail(Order order, Lesson lesson, Integer quantity, BigDecimal priceAtPurchase) {
        this.order = order;
        this.lesson = lesson;
        this.quantity = quantity;
        this.priceAtPurchase = priceAtPurchase;
    }
}