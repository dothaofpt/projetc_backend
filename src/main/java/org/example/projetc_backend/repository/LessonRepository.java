package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.Lesson.Level;
import org.example.projetc_backend.entity.Lesson.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository; // Thêm import này

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

@Repository // Thêm annotation này
public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    Optional<Lesson> findByTitle(String title);

    List<Lesson> findByIsDeletedFalse(); // Đổi tên cho rõ ràng hơn

    @Query("SELECT l FROM Lesson l WHERE l.isDeleted = false AND " +
            "(:title IS NULL OR LOWER(l.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:level IS NULL OR l.level = :level) AND " +
            "(:skill IS NULL OR l.skill = :skill) AND " +
            "(:minPrice IS NULL OR l.price >= :minPrice) AND " +
            "(:maxPrice IS NULL OR l.price <= :maxPrice)")
    Page<Lesson> searchLessons(
            @Param("title") String title,
            @Param("level") Level level,
            @Param("skill") Skill skill,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}