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
    @JoinColumn(name = "practice_activity_id", nullable = false)
    private PracticeActivity practiceActivity;

    @Column(name = "user_written_text", columnDefinition = "TEXT")
    private String userWrittenText;

    @Column(name = "grammar_feedback", columnDefinition = "TEXT")
    private String grammarFeedback;

    @Column(name = "spelling_feedback", columnDefinition = "TEXT")
    private String spellingFeedback;

    @Column(name = "cohesion_feedback", columnDefinition = "TEXT")
    private String cohesionFeedback;

    @Column(name = "overall_score")
    private Integer overallScore;

    @Column(name = "attempt_date", nullable = false)
    private LocalDateTime attemptDate = LocalDateTime.now();
}