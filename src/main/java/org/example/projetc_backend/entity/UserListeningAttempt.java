package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserListeningAttempts")
@Data
public class UserListeningAttempt {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer attemptId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "practice_activity_id", nullable = false) // Đã thay đổi: Liên kết với PracticeActivity
    private PracticeActivity practiceActivity;

    @Column(name = "user_transcribed_text", columnDefinition = "TEXT") // Văn bản người dùng đã gõ
    private String userTranscribedText;

    @Column(name = "accuracy_score") // Điểm chính xác (ví dụ: %)
    private Integer accuracyScore;

    @Column(name = "attempt_date", nullable = false)
    private LocalDateTime attemptDate = LocalDateTime.now();
}