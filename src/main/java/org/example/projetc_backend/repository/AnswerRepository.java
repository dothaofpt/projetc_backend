package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Answer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface AnswerRepository extends JpaRepository<Answer, Integer> {
    // Phương thức để lấy tất cả các câu trả lời (kể cả inactive) cho một câu hỏi cụ thể
    // THAY ĐỔI MỚI: Phương thức này giờ sẽ chỉ lấy những câu chưa bị xóa mềm
    List<Answer> findByQuestionQuestionIdAndIsDeletedFalse(Integer questionId);

    // Phương thức để chỉ lấy các câu trả lời active cho một câu hỏi cụ thể
    // Lưu ý: Những câu đã bị xóa mềm (isDeleted=true) cũng sẽ không phải là active.
    List<Answer> findByQuestionQuestionIdAndIsActiveTrue(Integer questionId);

    // Phương thức để lấy tất cả câu trả lời bao gồm cả đã xóa mềm (dùng cho mục đích recovery hoặc logs nếu cần)
    // List<Answer> findByQuestionQuestionId(Integer questionId); // Giữ nguyên hoặc bỏ nếu không dùng

    // Có thể thêm nếu bạn muốn tìm kiếm theo ID và active state,
    // nhưng hiện tại getAnswerById trong service đã lấy tất cả theo ID rồi xử lý.
    // Optional<Answer> findByAnswerIdAndIsActiveTrue(Integer answerId);
}