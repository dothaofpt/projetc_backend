package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    List<Quiz> findByLessonLessonId(Integer lessonId);
    List<Quiz> findBySkill(Quiz.Skill skill);
    Optional<Quiz> findByTitle(String title);
}