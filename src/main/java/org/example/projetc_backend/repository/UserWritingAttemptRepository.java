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
    List<UserWritingAttempt> findByQuestionQuestionId(Integer questionId);

    @Query("SELECT uwa FROM UserWritingAttempt uwa WHERE " +
            "(:userId IS NULL OR uwa.user.userId = :userId) AND " +
            "(:questionId IS NULL OR uwa.question.questionId = :questionId) AND " +
            "(:minOverallScore IS NULL OR uwa.overallScore >= :minOverallScore) AND " +
            "(:maxOverallScore IS NULL OR uwa.overallScore <= :maxOverallScore)")
    Page<UserWritingAttempt> searchWritingAttempts(
            @Param("userId") Integer userId,
            @Param("questionId") Integer questionId,
            @Param("minOverallScore") Integer minOverallScore,
            @Param("maxOverallScore") Integer maxOverallScore,
            Pageable pageable);
}