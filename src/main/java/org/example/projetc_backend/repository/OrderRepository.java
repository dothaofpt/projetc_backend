package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Import này là quan trọng
import java.time.LocalDateTime;
import java.util.List;

// Mở rộng JpaRepository và JpaSpecificationExecutor
public interface OrderRepository extends JpaRepository<Order, Integer>, JpaSpecificationExecutor<Order> {
    List<Order> findByUser(User user);
    List<Order> findByStatus(Order.OrderStatus status);
    List<Order> findByUserAndOrderDateBetween(User user, LocalDateTime startDate, LocalDateTime endDate);
}