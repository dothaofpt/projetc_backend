package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.entity.Question.QuestionType; // Bổ sung
import org.springframework.data.domain.Page; // Bổ sung
import org.springframework.data.domain.Pageable; // Bổ sung
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Bổ sung
import org.springframework.data.repository.query.Param; // Bổ sung
import org.springframework.stereotype.Repository; // Thêm import này

import java.util.List;

@Repository // Thêm annotation này
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findByQuizQuizId(Integer quizId);

    @Query("SELECT q FROM Question q WHERE " +
            "(:quizId IS NULL OR q.quiz.quizId = :quizId) AND " +
            "(:questionText IS NULL OR LOWER(q.questionText) LIKE LOWER(CONCAT('%', :questionText, '%'))) AND " +
            "(:questionType IS NULL OR q.questionType = :questionType)")
    Page<Question> searchQuestions(
            @Param("quizId") Integer quizId,
            @Param("questionText") String questionText,
            @Param("questionType") QuestionType questionType,
            Pageable pageable);
}