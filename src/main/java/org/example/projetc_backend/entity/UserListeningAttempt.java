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
    @JoinColumn(name = "question_id", nullable = false) // Câu hỏi liên quan đến bài nghe (nếu có)
    private Question question;

    @Column(name = "audio_material_url", columnDefinition = "TEXT") // URL của đoạn audio người dùng đã nghe
    private String audioMaterialUrl;

    @Column(name = "user_transcribed_text", columnDefinition = "TEXT") // Văn bản người dùng đã gõ
    private String userTranscribedText;

    @Column(name = "actual_transcript_text", columnDefinition = "TEXT") // Văn bản chính xác
    private String actualTranscriptText;

    @Column(name = "accuracy_score") // Điểm chính xác (ví dụ: %)
    private Integer accuracyScore;

    @Column(name = "attempt_date", nullable = false)
    private LocalDateTime attemptDate = LocalDateTime.now();
}