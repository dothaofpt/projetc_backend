package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.UserWritingAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserWritingAttemptRepository extends JpaRepository<UserWritingAttempt, Integer> {
    List<UserWritingAttempt> findByUserUserId(Integer userId);
    // Đã thay đổi: Liên kết với PracticeActivity
    List<UserWritingAttempt> findByPracticeActivityActivityId(Integer practiceActivityId);

    @Query("SELECT uwa FROM UserWritingAttempt uwa WHERE " +
            "(:userId IS NULL OR uwa.user.userId = :userId) AND " +
            // Đã thay đổi: Liên kết với PracticeActivity
            "(:practiceActivityId IS NULL OR uwa.practiceActivity.activityId = :practiceActivityId) AND " +
            "(:minOverallScore IS NULL OR uwa.overallScore >= :minOverallScore) AND " +
            "(:maxOverallScore IS NULL OR uwa.overallScore <= :maxOverallScore)")
    Page<UserWritingAttempt> searchWritingAttempts(
            @Param("userId") Integer userId,
            // Đã thay đổi: Tham số là practiceActivityId
            @Param("practiceActivityId") Integer practiceActivityId,
            @Param("minOverallScore") Integer minOverallScore,
            @Param("maxOverallScore") Integer maxOverallScore,
            Pageable pageable);
}