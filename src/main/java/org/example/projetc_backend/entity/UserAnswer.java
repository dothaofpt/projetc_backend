package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "User_Answers")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class UserAnswer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer userAnswerId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "quiz_result_id", nullable = false)
    private QuizResult quizResult; // Liên kết với kết quả tổng thể của bài quiz

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "question_id", nullable = false)
    private Question question; // Câu hỏi mà người dùng trả lời

    @Column(name = "user_answer_text", nullable = false, columnDefinition = "TEXT") // Để chứa câu trả lời dài
    private String userAnswerText;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect; // Kết quả chấm điểm (true/false)

    @Column(name = "submitted_at", nullable = false)
    private LocalDateTime submittedAt; // Thời điểm người dùng gửi câu trả lời này
}