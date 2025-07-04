package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Quiz;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface QuizRepository extends JpaRepository<Quiz, Integer> {
    List<Quiz> findByLessonLessonId(Integer lessonId);

    // Đã thay đổi: Sử dụng QuizType thay vì Skill
    List<Quiz> findByQuizType(Quiz.QuizType quizType);

    Optional<Quiz> findByTitle(String title);

    @Query("SELECT q FROM Quiz q WHERE " +
            "(:lessonId IS NULL OR q.lesson.lessonId = :lessonId) AND " +
            "(:title IS NULL OR LOWER(q.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            // Đã thay đổi: Sử dụng quizType trong truy vấn
            "(:quizType IS NULL OR q.quizType = :quizType)")
    Page<Quiz> searchQuizzes(
            @Param("lessonId") Integer lessonId,
            @Param("title") String title,
            // Đã thay đổi: Tham số là QuizType
            @Param("quizType") Quiz.QuizType quizType,
            Pageable pageable);
}