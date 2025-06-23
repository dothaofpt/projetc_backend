package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Question;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor; // Import này là quan trọng

import java.util.List;

// Mở rộng JpaRepository và JpaSpecificationExecutor
public interface QuestionRepository extends JpaRepository<Question, Integer>, JpaSpecificationExecutor<Question> {
    List<Question> findByQuizQuizId(Integer quizId);
}