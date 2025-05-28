package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.math.BigDecimal; // Import này là cần thiết

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
    private Skill skill;

    // THAY ĐỔI MỚI: Thêm trường price
    @Column(name = "price", nullable = false, precision = 10, scale = 2) // precision và scale cho số thập phân
    private BigDecimal price;

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

    public Lesson(String title, String description, Level level, Skill skill, BigDecimal price) { // THAY ĐỔI: Thêm price vào constructor
        this.title = title;
        this.description = description;
        this.level = level;
        this.skill = skill;
        this.price = price; // Gán price
    }
}