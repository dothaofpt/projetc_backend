package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Quiz;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    List<Quiz> findByLessonLessonId(Integer lessonId);
}