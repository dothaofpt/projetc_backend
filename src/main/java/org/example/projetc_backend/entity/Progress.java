package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Progress")
@Data
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer progressId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "activity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status = Status.NOT_STARTED;

    @Column(name = "completion_percentage", nullable = false)
    private Integer completionPercentage = 0;

    @Column(name = "last_updated", nullable = false)
    private LocalDateTime lastUpdated;

    // Enum mới cho ActivityType
    public enum ActivityType {
        READING_MATERIAL,
        FLASHCARDS,
        QUIZ,
        LISTENING_PRACTICE,
        SPEAKING_EXERCISE,
        WRITING_TASK,
        GRAMMAR_EXERCISE,
        VOCABULARY_BUILDER
    }

    public enum Status {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }

    @PrePersist
    protected void onCreate() {
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}