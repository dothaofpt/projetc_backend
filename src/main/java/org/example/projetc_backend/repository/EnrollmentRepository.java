package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.domain.Page; // Bổ sung
import org.springframework.data.domain.Pageable; // Bổ sung
import org.springframework.data.jpa.repository.Query; // Bổ sung
import org.springframework.data.repository.query.Param; // Bổ sung
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    Optional<Enrollment> findByUserUserIdAndLessonLessonId(Integer userId, Integer lessonId);
    List<Enrollment> findByLessonLessonId(Integer lessonId);
    List<Enrollment> findByUserUserId(Integer userId);

    @Query("SELECT e FROM Enrollment e WHERE " +
            "(:userId IS NULL OR e.user.userId = :userId) AND " +
            "(:lessonId IS NULL OR e.lesson.lessonId = :lessonId)")
    Page<Enrollment> searchEnrollments(
            @Param("userId") Integer userId,
            @Param("lessonId") Integer lessonId,
            Pageable pageable);
}