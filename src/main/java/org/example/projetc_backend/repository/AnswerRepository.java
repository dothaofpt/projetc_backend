package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    List<Answer> findByQuestionQuestionIdAndIsDeletedFalse(Integer questionId);
    List<Answer> findByQuestionQuestionIdAndIsActiveTrue(Integer questionId);

    @Query("SELECT a FROM Answer a WHERE " +
            "a.isDeleted = false AND " +
            "(:questionId IS NULL OR a.question.questionId = :questionId) AND " +
            "(:isCorrect IS NULL OR a.isCorrect = :isCorrect) AND " +
            "(:isActive IS NULL OR a.isActive = :isActive) AND " +
            "(:answerText IS NULL OR LOWER(a.answerText) LIKE LOWER(CONCAT('%', :answerText, '%')))")
    Page<Answer> searchAnswers(
            @Param("questionId") Integer questionId,
            @Param("isCorrect") Boolean isCorrect,
            @Param("isActive") Boolean isActive,
            @Param("answerText") String answerText,
            Pageable pageable);
}