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

    @Column
    @Enumerated(EnumType.STRING)
    private Skill skill = Skill.VOCABULARY;

    @Column
    @Enumerated(EnumType.STRING)
    private Status status = Status.IN_PROGRESS;

    @Column(name = "completion_percentage")
    private Integer completionPercentage = 0;

    @Column(name = "last_updated")
    private LocalDateTime lastUpdated = LocalDateTime.now();

    public enum Skill {
        LISTENING, SPEAKING, READING, WRITING, VOCABULARY, GRAMMAR
    }

    public enum Status {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }
}