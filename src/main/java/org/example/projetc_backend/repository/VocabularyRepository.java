package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Vocabulary;
import org.springframework.data.domain.Page; // Bổ sung
import org.springframework.data.domain.Pageable; // Bổ sung
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param; // Bổ sung
import org.springframework.stereotype.Repository; // Thêm import này

import java.util.List;
import java.util.Optional;

@Repository // Thêm annotation này
public interface VocabularyRepository extends JpaRepository<Vocabulary, Integer> {
    List<Vocabulary> findByDifficultyLevel(Vocabulary.DifficultyLevel difficultyLevel);
    Optional<Vocabulary> findByWord(String word);
    boolean existsByWord(String word);

    // Cập nhật phương thức tìm kiếm keyword hoặc tạo mới để hỗ trợ phân trang
    @Query("SELECT v FROM Vocabulary v WHERE " +
            "(:word IS NULL OR LOWER(v.word) LIKE LOWER(CONCAT('%', :word, '%'))) AND " +
            "(:meaning IS NULL OR LOWER(v.meaning) LIKE LOWER(CONCAT('%', :meaning, '%'))) AND " +
            "(:difficultyLevel IS NULL OR v.difficultyLevel = :difficultyLevel)")
    Page<Vocabulary> searchVocabularies(
            @Param("word") String word,
            @Param("meaning") String meaning,
            @Param("difficultyLevel") Vocabulary.DifficultyLevel difficultyLevel,
            Pageable pageable);
}