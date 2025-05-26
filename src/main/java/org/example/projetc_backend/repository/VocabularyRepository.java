package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;
import java.util.Optional;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Integer> {
    List<Vocabulary> findByDifficultyLevel(Vocabulary.DifficultyLevel difficultyLevel);
    Optional<Vocabulary> findByWord(String word);
    boolean existsByWord(String word);
    @Query("SELECT v FROM Vocabulary v WHERE v.word LIKE %:keyword%")
    List<Vocabulary> findByWordContaining(String keyword);
}