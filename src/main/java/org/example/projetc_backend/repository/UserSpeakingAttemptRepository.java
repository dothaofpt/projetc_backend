package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.UserSpeakingAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserSpeakingAttemptRepository extends JpaRepository<UserSpeakingAttempt, Integer> {
    List<UserSpeakingAttempt> findByUserUserId(Integer userId);
    // Đã thay đổi: Liên kết với PracticeActivity
    List<UserSpeakingAttempt> findByPracticeActivityActivityId(Integer practiceActivityId);

    @Query("SELECT usa FROM UserSpeakingAttempt usa WHERE " +
            "(:userId IS NULL OR usa.user.userId = :userId) AND " +
            // Đã thay đổi: Liên kết với PracticeActivity
            "(:practiceActivityId IS NULL OR usa.practiceActivity.activityId = :practiceActivityId) AND " +
            "(:minOverallScore IS NULL OR usa.overallScore >= :minOverallScore) AND " +
            "(:maxOverallScore IS NULL OR usa.overallScore <= :maxOverallScore)")
    Page<UserSpeakingAttempt> searchSpeakingAttempts(
            @Param("userId") Integer userId,
            // Đã thay đổi: Tham số là practiceActivityId
            @Param("practiceActivityId") Integer practiceActivityId,
            @Param("minOverallScore") Integer minOverallScore,
            @Param("maxOverallScore") Integer maxOverallScore,
            Pageable pageable);
}