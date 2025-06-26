package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Payment;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Order;
import org.springframework.data.domain.Page; // Bổ sung
import org.springframework.data.domain.Pageable; // Bổ sung
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Thêm import này

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

@Repository // Thêm annotation này
public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByUser(User user);
    Optional<Payment> findByOrder(Order order);
    List<Payment> findByStatus(Payment.PaymentStatus status);
    Optional<Payment> findByTransactionId(String transactionId);
    List<Payment> findByUserAndPaymentDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
    long countByUserAndStatus(User user, Payment.PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user = :user AND p.status = :status")
    Optional<BigDecimal> sumAmountByUserAndStatus(@Param("user") User user, @Param("status") Payment.PaymentStatus status);

    // Cập nhật searchPaymentsByKeyword để hỗ trợ tìm kiếm linh hoạt hơn và phân trang
    @Query("SELECT p FROM Payment p WHERE " +
            "(:userId IS NULL OR p.user.userId = :userId) AND " +
            "(:orderId IS NULL OR p.order.orderId = :orderId) AND " +
            "(:paymentMethod IS NULL OR LOWER(p.paymentMethod) LIKE LOWER(CONCAT('%', :paymentMethod, '%'))) AND " +
            "(:transactionId IS NULL OR LOWER(p.transactionId) LIKE LOWER(CONCAT('%', :transactionId, '%'))) AND " +
            "(:status IS NULL OR p.status = :status)")
    Page<Payment> searchPayments(
            @Param("userId") Integer userId,
            @Param("orderId") Integer orderId,
            @Param("paymentMethod") String paymentMethod,
            @Param("transactionId") String transactionId,
            @Param("status") Payment.PaymentStatus status,
            Pageable pageable);
}