package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "lessons") // Chữ thường để khớp với chuẩn SQL
@Data
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lessonId;

    @Column(nullable = false, length = 255, unique = true) // Thêm unique nếu cần
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Skill skill;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @PrePersist
    protected void onCreate() {
        if (createdAt == null) {
            createdAt = LocalDateTime.now();
        }
    }

    public enum Level {
        BEGINNER, INTERMEDIATE, ADVANCED
    }

    public enum Skill {
        LISTENING, SPEAKING, READING, WRITING, VOCABULARY, GRAMMAR
    }

    // Constructors
    public Lesson() {}

    public Lesson(String title, String description, Level level, Skill skill) {
        this.title = title;
        this.description = description;
        this.level = level;
        this.skill = skill;
    }
}