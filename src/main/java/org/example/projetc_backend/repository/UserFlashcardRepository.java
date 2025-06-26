package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.UserFlashcard;
import org.example.projetc_backend.entity.Vocabulary;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserFlashcardRepository extends JpaRepository<UserFlashcard, Integer> {
    Optional<UserFlashcard> findByUserUserIdAndVocabularyWordId(Integer userId, Integer wordId);
    List<UserFlashcard> findByUserUserId(Integer userId);
    List<UserFlashcard> findByVocabularyWordId(Integer wordId);

    @Query("SELECT uf FROM UserFlashcard uf " +
            "JOIN uf.vocabulary v " +
            "LEFT JOIN FlashcardSetVocabulary fsv ON v.wordId = fsv.vocabulary.wordId " +
            "WHERE uf.user.userId = :userId AND " +
            "(:setId IS NULL OR fsv.flashcardSet.setId = :setId) AND " +
            "(:wordId IS NULL OR v.wordId = :wordId) AND " +
            "(:word IS NULL OR LOWER(v.word) LIKE LOWER(CONCAT('%', :word, '%'))) AND " +
            "(:meaning IS NULL OR LOWER(v.meaning) LIKE LOWER(CONCAT('%', :meaning, '%'))) AND " +
            "(:isKnown IS NULL OR uf.isKnown = :isKnown) AND " +
            "(:difficultyLevel IS NULL OR v.difficultyLevel = :difficultyLevel) AND " +
            "(:minReviewIntervalDays IS NULL OR uf.reviewIntervalDays >= :minReviewIntervalDays) AND " +
            "(:maxReviewIntervalDays IS NULL OR uf.reviewIntervalDays <= :maxReviewIntervalDays) AND " +
            "(:minEaseFactor IS NULL OR uf.easeFactor >= :minEaseFactor) AND " +
            "(:maxEaseFactor IS NULL OR uf.easeFactor <= :maxEaseFactor)")
    Page<UserFlashcard> searchUserFlashcards(
            @Param("userId") Integer userId,
            @Param("setId") Integer setId,
            @Param("wordId") Integer wordId,
            @Param("word") String word,
            @Param("meaning") String meaning,
            @Param("isKnown") Boolean isKnown,
            @Param("difficultyLevel") Vocabulary.DifficultyLevel difficultyLevel,
            @Param("minReviewIntervalDays") Integer minReviewIntervalDays,
            @Param("maxReviewIntervalDays") Integer maxReviewIntervalDays,
            @Param("minEaseFactor") Double minEaseFactor,
            @Param("maxEaseFactor") Double maxEaseFactor,
            Pageable pageable);
}