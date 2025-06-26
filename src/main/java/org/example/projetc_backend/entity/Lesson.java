package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "lessons")
@Data
public class Lesson {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer lessonId;

    @Column(nullable = false, length = 255, unique = true)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Level level;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Skill skill; // Kỹ năng chính của bài học (có thể có nhiều quiz với skill khác nhau)

    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false;

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
        LISTENING, SPEAKING, READING, WRITING, VOCABULARY, GRAMMAR, GENERAL // GENERAL cho các bài học tổng hợp
    }

    // Constructors
    public Lesson() {}

    public Lesson(String title, String description, Level level, Skill skill, BigDecimal price) {
        this.title = title;
        this.description = description;
        this.level = level;
        this.skill = skill;
        this.price = price;
    }
}