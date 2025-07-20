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

    // SỬA: Đảm bảo tìm kiếm từ vựng CHƯA BỊ XÓA MỀM khi kiểm tra trùng lặp
    Optional<Vocabulary> findByWordIgnoreCaseAndIsDeletedFalse(String word);

    // Bạn có thể xóa findByWord nếu không có trường hợp sử dụng cụ thể,
    // hoặc giữ lại nếu bạn cần tìm kiếm chính xác (case-sensitive) cả từ đã xóa mềm.
    // Với mục đích của soft delete, findByWordIgnoreCaseAndIsDeletedFalse là quan trọng nhất.
    // Optional<Vocabulary> findByWord(String word);

    // CŨ: boolean existsByWord(String word);
    // MỚI: Nên dùng findByWordIgnoreCaseAndIsDeletedFalse().isPresent() cho logic chuẩn soft delete
    // Hoặc nếu muốn kiểm tra tồn tại tổng thể (kể cả đã xóa mềm), hãy tạo phương thức mới.

    // Cập nhật query để CHỈ TRẢ VỀ CÁC TỪ VỰNG CHƯA BỊ XÓA MỀM
    @Query("SELECT v FROM Vocabulary v WHERE " +
            "(:word IS NULL OR LOWER(v.word) LIKE LOWER(CONCAT('%', :word, '%'))) AND " +
            "(:meaning IS NULL OR LOWER(v.meaning) LIKE LOWER(CONCAT('%', :meaning, '%'))) AND " +
            "(:difficultyLevel IS NULL OR v.difficultyLevel = :difficultyLevel) AND " +
            "v.isDeleted = false") // THÊM ĐIỀU KIỆN QUAN TRỌNG NÀY
    Page<Vocabulary> searchVocabularies(
            @Param("word") String word,
            @Param("meaning") String meaning,
            @Param("difficultyLevel") Vocabulary.DifficultyLevel difficultyLevel,
            Pageable pageable);
}