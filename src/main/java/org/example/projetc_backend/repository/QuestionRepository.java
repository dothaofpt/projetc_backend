package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findByQuizQuizId(Integer quizId);
    List<Question> findByType(Question.QuestionType type);
}