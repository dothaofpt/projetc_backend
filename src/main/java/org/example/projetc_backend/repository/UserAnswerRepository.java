package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.UserAnswer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface UserAnswerRepository extends JpaRepository<UserAnswer, Integer> {
    // Lấy tất cả câu trả lời của người dùng cho một QuizResult cụ thể
    List<UserAnswer> findByQuizResultResultId(Integer quizResultId);

    // Lấy câu trả lời của người dùng cho một câu hỏi cụ thể trong một QuizResult
    // (nếu bạn muốn đảm bảo mỗi câu hỏi chỉ có 1 câu trả lời trong 1 lần làm bài)
    UserAnswer findByQuizResultResultIdAndQuestionQuestionId(Integer quizResultId, Integer questionId);
}