package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Quiz;
import org.springframework.data.domain.Page; // Bổ sung
import org.springframework.data.domain.Pageable; // Bổ sung
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query; // Bổ sung
import org.springframework.data.repository.query.Param; // Bổ sung
import org.springframework.stereotype.Repository; // Thêm import này

import java.util.List;
import java.util.Optional;

@Repository // Thêm annotation này
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    List<Quiz> findByLessonLessonId(Integer lessonId);
    List<Quiz> findBySkill(Quiz.Skill skill);
    Optional<Quiz> findByTitle(String title);

    @Query("SELECT q FROM Quiz q WHERE " +
            "(:lessonId IS NULL OR q.lesson.lessonId = :lessonId) AND " +
            "(:title IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:skill IS NULL OR q.skill = :skill)")
    Page<Quiz> searchQuizzes(
            @Param("lessonId") Integer lessonId,
            @Param("title") String title,
            @Param("skill") Quiz.Skill skill,
            Pageable pageable);
}