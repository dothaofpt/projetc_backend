package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Payment;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Order; // Import Order entity
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.math.BigDecimal;

public interface PaymentRepository extends JpaRepository<Payment, Integer> {

    List<Payment> findByUser(User user);

    Optional<Payment> findByOrder(Order order); // THAY ĐỔI: Tìm theo Order

    List<Payment> findByStatus(Payment.PaymentStatus status);

    Optional<Payment> findByTransactionId(String transactionId);

    List<Payment> findByUserAndPaymentDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    // Xóa phương thức này vì Payment không còn liên kết trực tiếp với Lesson
    // List<Payment> findByLessonAndPaymentDateBetween(Lesson lesson, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT p FROM Payment p WHERE p.description LIKE %:keyword% OR p.transactionId LIKE %:keyword%")
    List<Payment> searchPaymentsByKeyword(@Param("keyword") String keyword);

    long countByUserAndStatus(User user, Payment.PaymentStatus status);

    @Query("SELECT SUM(p.amount) FROM Payment p WHERE p.user = :user AND p.status = :status")
    Optional<BigDecimal> sumAmountByUserAndStatus(@Param("user") User user, @Param("status") Payment.PaymentStatus status);
}