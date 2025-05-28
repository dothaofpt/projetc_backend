package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface QuestionRepository extends JpaRepository<Question, Integer> {
    List<Question> findByQuizQuizId(Integer quizId);
    List<Question> findBySkill(Quiz.Skill skill);
}