package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserWritingAttempts")
@Data
public class UserWritingAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attemptId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id") // Câu hỏi/gợi ý viết (có thể là một WritingPrompt Question)
    private Question question;

    @Column(name = "original_prompt_text", columnDefinition = "TEXT") // Gợi ý viết gốc
    private String originalPromptText;

    @Column(name = "user_written_text", columnDefinition = "TEXT") // Văn bản người dùng đã viết
    private String userWrittenText;

    @Column(name = "grammar_feedback", columnDefinition = "TEXT") // Phản hồi về ngữ pháp
    private String grammarFeedback;

    @Column(name = "spelling_feedback", columnDefinition = "TEXT") // Phản hồi về chính tả
    private String spellingFeedback;

    @Column(name = "cohesion_feedback", columnDefinition = "TEXT") // Phản hồi về mạch lạc (nếu có thể tự động)
    private String cohesionFeedback;

    @Column(name = "overall_score") // Điểm tổng thể của bài viết (nếu có thể tự động chấm hoặc giáo viên chấm)
    private Integer overallScore;

    @Column(name = "attempt_date", nullable = false)
    private LocalDateTime attemptDate = LocalDateTime.now();
}