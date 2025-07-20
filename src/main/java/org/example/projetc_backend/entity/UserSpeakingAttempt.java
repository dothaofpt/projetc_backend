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
    @JoinColumn(name = "practice_activity_id", nullable = false)
    private PracticeActivity practiceActivity;

    @Column(name = "user_audio_url", columnDefinition = "TEXT")
    private String userAudioUrl;

    @Column(name = "user_transcribed_by_stt", columnDefinition = "TEXT") // Văn bản được chuyển đổi từ giọng nói người dùng (STT)
    private String userTranscribedBySTT; // THÊM LẠI: Backend sẽ lưu kết quả STT của nó

    @Column(name = "pronunciation_score")
    private Integer pronunciationScore;

    @Column(name = "fluency_score")
    private Integer fluencyScore;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(name = "attempt_date", nullable = false)
    private LocalDateTime attemptDate = LocalDateTime.now();
}