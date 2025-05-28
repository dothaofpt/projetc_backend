package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "Payments")
@Data
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer paymentId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user; // Liên kết với người dùng thực hiện thanh toán

    // THAY ĐỔI MỚI: Liên kết với Order thay vì Lesson trực tiếp
    @OneToOne // Một Payment cho một Order (giả định 1-1)
    @JoinColumn(name = "order_id", nullable = false, unique = true) // Mỗi Order chỉ có 1 Payment
    private Order order; // Đơn hàng mà thanh toán này xử lý

    // Bạn có thể bỏ Lesson lesson nếu mọi thứ đều qua OrderDetail
    // Hoặc giữ lại nếu muốn linh hoạt thanh toán trực tiếp cho Lesson mà không cần Order
    // Để giữ logic chặt chẽ, tôi sẽ loại bỏ Lesson lesson ở đây vì nó đã có trong OrderDetail
    // @ManyToOne
    // @JoinColumn(name = "lesson_id", nullable = true)
    // private Lesson lesson;

    @Column(name = "amount", nullable = false, precision = 10, scale = 2)
    private BigDecimal amount;

    @Column(name = "payment_date", nullable = false)
    private LocalDateTime paymentDate;

    @Column(name = "payment_method", length = 50)
    private String paymentMethod;

    @Column(name = "transaction_id", unique = true, length = 100)
    private String transactionId;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private PaymentStatus status;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    public enum PaymentStatus {
        PENDING,
        COMPLETED,
        FAILED,
        REFUNDED,
        CANCELLED
    }

    @PrePersist
    protected void onCreate() {
        if (paymentDate == null) {
            paymentDate = LocalDateTime.now();
        }
        if (status == null) {
            status = PaymentStatus.PENDING;
        }
    }
}