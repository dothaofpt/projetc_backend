package org.example.projetc_backend.repository;

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
public interface VocabularyRepository extends JpaRepository<Vocabulary, Integer> {
    List<Vocabulary> findByDifficultyLevel(Vocabulary.DifficultyLevel difficultyLevel);

    // THÊM phương thức này để giải quyết lỗi 'findByWordIgnoreCase'
    Optional<Vocabulary> findByWordIgnoreCase(String word);

    // Bạn có thể giữ findByWord nếu có trường hợp sử dụng cụ thể,
    // nhưng để kiểm tra trùng lặp không phân biệt chữ hoa/thường, findByWordIgnoreCase là tốt hơn.
    Optional<Vocabulary> findByWord(String word);

    boolean existsByWord(String word); // Giữ lại hoặc thay bằng findByWordIgnoreCase().isPresent()

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