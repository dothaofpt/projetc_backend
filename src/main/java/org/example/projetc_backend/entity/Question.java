package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Questions")
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer questionId;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "question_text", columnDefinition = "TEXT")
    private String questionText = ""; // Nội dung câu hỏi (ví dụ: đoạn văn đọc, câu hỏi trắc nghiệm)

    @Column(name = "question_type") // Bổ sung: Loại câu hỏi (trắc nghiệm, điền từ, nghe chép, v.v.)
    @Enumerated(EnumType.STRING)
    private QuestionType questionType;

    @Column(name = "audio_url", columnDefinition = "TEXT") // Bổ sung: Dành cho câu hỏi nghe
    private String audioUrl;

    @Column(name = "image_url", columnDefinition = "TEXT") // Bổ sung: Dành cho câu hỏi hình ảnh
    private String imageUrl;

    @Column(name = "correct_answer_text", columnDefinition = "TEXT") // Bổ sung: Đáp án chính xác cho các loại câu hỏi không có Answer riêng (VD: Nghe chép, điền từ)
    private String correctAnswerText;

    public enum QuestionType {
        MULTIPLE_CHOICE, // Trắc nghiệm
        FILL_IN_THE_BLANK, // Điền vào chỗ trống
        DICTATION, // Nghe chép chính tả
        SPEAKING_PROMPT, // Gợi ý cho bài nói
        WRITING_PROMPT, // Gợi ý cho bài viết
        MATCHING, // Nối từ
        TRUE_FALSE // Đúng sai
    }
}