package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.OrderDetail;
import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;

public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrder(Order order);
    List<OrderDetail> findByLesson(Lesson lesson);
    Optional<OrderDetail> findByOrderAndLesson(Order order, Lesson lesson);
}