package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    List<Answer> findByQuestionQuestionId(Integer questionId);
}