package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserSpeakingAttempts")
@Data
public class UserSpeakingAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attemptId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false) // Câu hỏi/gợi ý nói
    private Question question;

    @Column(name = "original_prompt_text", columnDefinition = "TEXT") // Văn bản gốc của gợi ý (ví dụ: câu mẫu để lặp lại)
    private String originalPromptText;

    @Column(name = "user_audio_url", columnDefinition = "TEXT") // URL của bản ghi âm của người dùng
    private String userAudioUrl;

    @Column(name = "user_transcribed_by_stt", columnDefinition = "TEXT") // Văn bản được chuyển đổi từ giọng nói người dùng (STT)
    private String userTranscribedBySTT;

    @Column(name = "pronunciation_score") // Điểm phát âm (ví dụ: 0-100)
    private Integer pronunciationScore;

    @Column(name = "fluency_score") // Điểm lưu loát (nếu có thể đánh giá)
    private Integer fluencyScore;

    @Column(name = "overall_score") // Điểm tổng thể của bài nói
    private Integer overallScore;

    @Column(name = "attempt_date", nullable = false)
    private LocalDateTime attemptDate = LocalDateTime.now();
}