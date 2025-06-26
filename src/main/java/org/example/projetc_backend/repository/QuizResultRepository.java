package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.QuizResult;
import org.springframework.data.domain.Page; // Bổ sung
import org.springframework.data.domain.Pageable; // Bổ sung
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Bổ sung
import org.springframework.data.repository.query.Param; // Bổ sung
import org.springframework.stereotype.Repository; // Thêm import này

import java.util.List;
import java.util.Optional;

@Repository // Thêm annotation này
public interface QuizResultRepository extends JpaRepository<QuizResult, Integer> {
    Optional<QuizResult> findByUserUserIdAndQuizQuizId(Integer userId, Integer quizId);
    List<QuizResult> findByUserUserId(Integer userId);
    List<QuizResult> findByQuizQuizId(Integer quizId);

    @Query("SELECT qr FROM QuizResult qr WHERE " +
            "(:userId IS NULL OR qr.user.userId = :userId) AND " +
            "(:quizId IS NULL OR qr.quiz.quizId = :quizId) AND " +
            "(:minScore IS NULL OR qr.score >= :minScore) AND " +
            "(:maxScore IS NULL OR qr.score <= :maxScore)")
    Page<QuizResult> searchQuizResults(
            @Param("userId") Integer userId,
            @Param("quizId") Integer quizId,
            @Param("minScore") Double minScore, // Đã thay đổi từ Integer sang Double
            @Param("maxScore") Double maxScore, // Đã thay đổi từ Integer sang Double
            Pageable pageable);
}