package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.FlashcardSetVocabulary;
import org.example.projetc_backend.entity.FlashcardSetVocabularyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardSetVocabularyRepository extends JpaRepository<FlashcardSetVocabulary, FlashcardSetVocabularyId> {
    List<FlashcardSetVocabulary> findByFlashcardSetSetId(Integer setId);
    List<FlashcardSetVocabulary> findByVocabularyWordId(Integer wordId);
    boolean existsByFlashcardSetSetIdAndVocabularyWordId(Integer setId, Integer wordId);
    void deleteByFlashcardSetSetIdAndVocabularyWordId(Integer setId, Integer wordId); // Để xóa một từ ra khỏi một bộ
}