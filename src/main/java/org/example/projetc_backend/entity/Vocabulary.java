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

    @Column(nullable = false)
    private String meaning;

    @Column(name = "example_sentence")
    private String exampleSentence;

    @Column(length = 100)
    private String pronunciation;

    @Column(name = "audio_url")
    private String audioUrl;

    @Column(name = "difficulty_level", nullable = false)
    private String difficultyLevel;
}