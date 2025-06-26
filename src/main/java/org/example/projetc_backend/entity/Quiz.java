package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Quizzes")
@Data
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer quizId;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson; // Bài học mà quiz này thuộc về

    @Column(length = 255)
    private String title = "";

    @Column
    @Enumerated(EnumType.STRING)
    private Skill skill = Skill.VOCABULARY; // Kỹ năng chính mà quiz này kiểm tra

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum Skill {
        LISTENING, SPEAKING, READING, WRITING, VOCABULARY, GRAMMAR
    }
}