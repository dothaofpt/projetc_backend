package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.entity.User;
import org.springframework.data.domain.Page; // Bổ sung
import org.springframework.data.domain.Pageable; // Bổ sung
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query; // Bổ sung
import org.springframework.data.repository.query.Param; // Bổ sung
import org.springframework.stereotype.Repository; // Thêm import này

import java.time.LocalDateTime;
import java.util.List;

@Repository // Thêm annotation này
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {
    List<Order> findByUser(User user);
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByUserAndOrderDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate);

    @Query("SELECT o FROM Order o WHERE " +
            "(:userId IS NULL OR o.user.userId = :userId) AND " +
            "(:status IS NULL OR o.status = :status) AND " +
            "(:minTotalAmount IS NULL OR o.totalAmount >= :minTotalAmount) AND " +
            "(:maxTotalAmount IS NULL OR o.totalAmount <= :maxTotalAmount)")
    Page<Order> searchOrders(
            @Param("userId") Integer userId,
            @Param("status") Order.OrderStatus status,
            @Param("minTotalAmount") java.math.BigDecimal minTotalAmount,
            @Param("maxTotalAmount") java.math.BigDecimal maxTotalAmount,
            Pageable pageable);
}