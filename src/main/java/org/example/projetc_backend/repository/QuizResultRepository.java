package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.QuizResult;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizResultRepository extends JpaRepository<QuizResult, Integer> {
    Optional<QuizResult> findByUserUserIdAndQuizQuizId(Integer userId, Integer quizId);
    List<QuizResult> findByUserUserId(Integer userId);
    List<QuizResult> findByQuizQuizId(Integer quizId);
}