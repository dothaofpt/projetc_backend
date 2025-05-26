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

    @Column(name = "question_text")
    private String questionText = "";

    @Column
    @Enumerated(EnumType.STRING)
    private QuestionType type = QuestionType.MULTIPLE_CHOICE;

    public enum QuestionType {
        MULTIPLE_CHOICE, FILL_IN_THE_BLANK, LISTENING_COMPREHENSION, SPEAKING_PRONUNCIATION, READING_COMPREHENSION, WRITING_ESSAY
    }
}