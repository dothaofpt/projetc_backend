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

    // PHƯƠNG THỨC NÀY CẦN PHẢI TỒN TẠI VÀ CHÍNH XÁC
    // Đảm bảo rằng trong entity Vocabulary của bạn có mối quan hệ ánh xạ tới FlashcardSetVocabulary
    // Ví dụ trong Vocabulary.java có thể có:
    // @OneToMany(mappedBy = "vocabulary")
    // private Set<FlashcardSetVocabulary> flashcardSetVocabularies; // Tên thuộc tính này phải khớp
    @Query("SELECT uf FROM UserFlashcard uf " +
            "JOIN uf.vocabulary v " +
            "JOIN v.flashcardSetVocabularies fsv " + // 'flashcardSetVocabularies' là tên thuộc tính trong Vocabulary entity
            "WHERE uf.user.userId = :userId " +
            "AND fsv.flashcardSet.setId = :setId")
    List<UserFlashcard> findByUserUserIdAndVocabularyFlashcardSetVocabulariesFlashcardSetSetId(
            @Param("userId") Integer userId,
            @Param("setId") Integer setId
    );

    @Query("SELECT uf FROM UserFlashcard uf " +
            "JOIN uf.vocabulary v " +
            // LEFT JOIN để đảm bảo cả các UserFlashcard không thuộc set nào cũng được xem xét nếu setId IS NULL
            "LEFT JOIN v.flashcardSetVocabularies fsv ON v.wordId = fsv.vocabulary.wordId " +
            "WHERE (:userId IS NULL OR uf.user.userId = :userId) AND " +
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
            Pageable pageable
    );
}