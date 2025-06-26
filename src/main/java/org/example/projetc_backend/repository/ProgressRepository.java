package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Progress;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProgressRepository extends JpaRepository<Progress, Integer> {
    Optional<Progress> findByUserUserIdAndLessonLessonIdAndActivityType(Integer userId, Integer lessonId, Progress.ActivityType activityType);
    List<Progress> findByUserUserIdAndLessonLessonId(Integer userId, Integer lessonId);
    List<Progress> findByUserUserId(Integer userId);
    List<Progress> findByStatus(Progress.Status status);

    @Query("SELECT p FROM Progress p WHERE " +
            "(:userId IS NULL OR p.user.userId = :userId) AND " +
            "(:lessonId IS NULL OR p.lesson.lessonId = :lessonId) AND " +
            "(:activityType IS NULL OR p.activityType = :activityType) AND " +
            "(:status IS NULL OR p.status = :status) AND " +
            "(:minCompletionPercentage IS NULL OR p.completionPercentage >= :minCompletionPercentage) AND " +
            "(:maxCompletionPercentage IS NULL OR p.completionPercentage <= :maxCompletionPercentage)")
    Page<Progress> searchProgress(
            @Param("userId") Integer userId,
            @Param("lessonId") Integer lessonId,
            @Param("activityType") Progress.ActivityType activityType,
            @Param("status") Progress.Status status,
            @Param("minCompletionPercentage") Integer minCompletionPercentage,
            @Param("maxCompletionPercentage") Integer maxCompletionPercentage,
            Pageable pageable);
}