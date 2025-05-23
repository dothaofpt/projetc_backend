package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Questions")
@Data
public class Question {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer questionId;

    @ManyToOne
    @JoinColumn(name = "quiz_id", nullable = false)
    private Quiz quiz;

    @Column(name = "question_text", nullable = false)
    private String questionText;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private QuestionType type;

    public enum QuestionType {
        multiple_choice, fill_in_the_blank
    }
}