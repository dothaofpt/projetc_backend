package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.Lesson.Level;
import org.example.projetc_backend.entity.Lesson.Skill;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    Optional<Lesson> findByTitle(String title);

    @Query("SELECT l FROM Lesson l WHERE l.isDeleted = false")
    List<Lesson> findAllActive();

    @Query("SELECT l FROM Lesson l WHERE l.isDeleted = false AND (:title is null or l.title like %:title%) AND (:level is null or l.level = :level) AND (:skill is null or l.skill = :skill) AND (:minPrice is null or l.price >= :minPrice) AND (:maxPrice is null or l.price <= :maxPrice)")
    Page<Lesson> searchLessons(
            @Param("title") String title,
            @Param("level") Level level,
            @Param("skill") Skill skill,
            @Param("minPrice") BigDecimal minPrice,
            @Param("maxPrice") BigDecimal maxPrice,
            Pageable pageable
    );
}