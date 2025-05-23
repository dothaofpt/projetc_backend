package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Lesson;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonRepository extends JpaRepository<Lesson, Integer> {
    List<Lesson> findByLevel(Lesson.Level level);
}