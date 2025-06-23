package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.UserFlashcard;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface UserFlashcardRepository extends JpaRepository<UserFlashcard, Integer> {
    Optional<UserFlashcard> findByUserIdAndWordId(Integer userId, Integer wordId);
    List<UserFlashcard> findByUserId(Integer userId);
    List<UserFlashcard> findByWordId(Integer wordId);

    @Query("SELECT uf FROM UserFlashcard uf " +
            "JOIN Vocabulary v ON uf.wordId = v.wordId " +
            "JOIN LessonVocabulary lv ON v.wordId = lv.id.wordId " +
            "WHERE uf.userId = :userId AND " +
            "(:lessonId IS NULL OR lv.id.lessonId = :lessonId) AND " +
            "(:word IS NULL OR LOWER(v.word) LIKE LOWER(CONCAT('%', :word, '%'))) AND " +
            "(:meaning IS NULL OR LOWER(v.meaning) LIKE LOWER(CONCAT('%', :meaning, '%'))) AND " +
            "(:isKnown IS NULL OR uf.isKnown = :isKnown) AND " +
            "(:difficultyLevel IS NULL OR v.difficultyLevel = :difficultyLevel)")
    Page<UserFlashcard> searchUserFlashcards(
            @Param("userId") Integer userId,
            @Param("lessonId") Integer lessonId,
            @Param("word") String word,
            @Param("meaning") String meaning,
            @Param("isKnown") Boolean isKnown,
            @Param("difficultyLevel") String difficultyLevel,
            Pageable pageable);
}