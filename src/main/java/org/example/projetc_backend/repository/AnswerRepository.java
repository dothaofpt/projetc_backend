package org.example.projetc_backend.repository;// org.example.projetc_backend.repository.AnswerRepository
// ... các imports và khai báo khác ...

import org.example.projetc_backend.entity.Answer;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    List<Answer> findByQuestionQuestionIdAndIsDeletedFalse(Integer questionId);
    List<Answer> findByQuestionQuestionIdAndIsActiveTrue(Integer questionId);

    // THÊM DÒNG NÀY: Phương thức để tìm kiếm câu trả lời đúng, đang hoạt động và chưa bị xóa mềm
    List<Answer> findByQuestionQuestionIdAndIsCorrectTrueAndIsActiveTrueAndIsDeletedFalse(Integer questionId);

    @Query("SELECT a FROM Answer a WHERE " +
            // BỎ isDeleted = false cứng nhắc nếu muốn search cả các câu đã bị xóa mềm.
            // Nếu bạn muốn search mặc định chỉ các câu chưa xóa mềm, giữ lại.
            // Nếu muốn tìm kiếm cả các câu đã xóa mềm (tùy theo request.isDeleted), thì bỏ dòng này và thêm param isDeleted vào WHERE clause.
            // Dựa trên AnswerSearchRequest của bạn có isDeleted, nên bỏ cái này và thêm param.
            "(:questionId IS NULL OR a.question.questionId = :questionId) AND " +
            "(:isCorrect IS NULL OR a.isCorrect = :isCorrect) AND " +
            "(:isActive IS NULL OR a.isActive = :isActive) AND " +
            // THÊM DÒNG NÀY: Đảm bảo isDeleted được truyền vào query
            "(:isDeleted IS NULL OR a.isDeleted = :isDeleted) AND " +
            "(:answerText IS NULL OR LOWER(a.answerText) LIKE LOWER(CONCAT('%', :answerText, '%')))")
    Page<Answer> searchAnswers(
            @Param("questionId") Integer questionId,
            @Param("isCorrect") Boolean isCorrect,
            @Param("isActive") Boolean isActive,
            @Param("isDeleted") Boolean isDeleted, // THÊM THAM SỐ NÀY
            @Param("answerText") String answerText,
            Pageable pageable);
}