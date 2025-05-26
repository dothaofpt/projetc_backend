package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "Vocabulary")
@Data
public class Vocabulary {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer wordId;

    @Column(nullable = false, length = 100)
    private String word;

    @Column(columnDefinition = "TEXT")
    private String meaning = "";

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence;

    @Column(length = 100)
    private String pronunciation;

    @Column(name = "audio_url", columnDefinition = "TEXT")
    private String audioUrl;

    @Column(name = "writing_prompt", columnDefinition = "TEXT")
    private String writingPrompt;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level")
    private DifficultyLevel difficultyLevel = DifficultyLevel.EASY;

    public enum DifficultyLevel {
        EASY, MEDIUM, HARD
    }
}