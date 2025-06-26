package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "QuizResults")
@Data
public class QuizResult {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer resultId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column
    private Integer score = 0; // Điểm số của bài quiz (từ 0-100 hoặc số câu đúng)

    @Column(name = "completed_at")
    private LocalDateTime completedAt = LocalDateTime.now();

    @Column(name = "duration_seconds") // Bổ sung: Thời gian làm bài
    private Integer durationSeconds;
}