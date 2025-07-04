package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.UserListeningAttempt;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface UserListeningAttemptRepository extends JpaRepository<UserListeningAttempt, Integer> {
    List<UserListeningAttempt> findByUserUserId(Integer userId);
    // Đã thay đổi: Liên kết với PracticeActivity
    List<UserListeningAttempt> findByPracticeActivityActivityId(Integer practiceActivityId);

    @Query("SELECT ula FROM UserListeningAttempt ula WHERE " +
            "(:userId IS NULL OR ula.user.userId = :userId) AND " +
            // Đã thay đổi: Liên kết với PracticeActivity
            "(:practiceActivityId IS NULL OR ula.practiceActivity.activityId = :practiceActivityId) AND " +
            "(:minAccuracyScore IS NULL OR ula.accuracyScore >= :minAccuracyScore) AND " +
            "(:maxAccuracyScore IS NULL OR ula.accuracyScore <= :maxAccuracyScore)")
    Page<UserListeningAttempt> searchListeningAttempts(
            @Param("userId") Integer userId,
            // Đã thay đổi: Tham số là practiceActivityId
            @Param("practiceActivityId") Integer practiceActivityId,
            @Param("minAccuracyScore") Integer minAccuracyScore,
            @Param("maxAccuracyScore") Integer maxAccuracyScore,
            Pageable pageable); // Tham số Pageable đã có sẵn
}