package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.OrderDetail;
import org.example.projetc_backend.entity.Order;
import org.example.projetc_backend.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page; // Bổ sung
import org.springframework.data.domain.Pageable; // Bổ sung
import org.springframework.data.jpa.repository.Query; // Bổ sung
import org.springframework.data.repository.query.Param; // Bổ sung
import org.springframework.stereotype.Repository; // Thêm import này
import java.util.List;
import java.util.Optional;

@Repository // Thêm annotation này
public interface OrderDetailRepository extends JpaRepository<OrderDetail, Integer> {
    List<OrderDetail> findByOrder(Order order);
    List<OrderDetail> findByLesson(Lesson lesson);
    Optional<OrderDetail> findByOrderAndLesson(Order order, Lesson lesson);

    @Query("SELECT od FROM OrderDetail od WHERE " +
            "(:orderId IS NULL OR od.order.orderId = :orderId) AND " +
            "(:lessonId IS NULL OR od.lesson.lessonId = :lessonId)")
    Page<OrderDetail> searchOrderDetails(
            @Param("orderId") Integer orderId,
            @Param("lessonId") Integer lessonId,
            Pageable pageable);
}