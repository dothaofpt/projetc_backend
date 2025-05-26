package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "UserFlashcards")
@Data
public class UserFlashcard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(name = "user_id", nullable = false)
    private Integer userId;

    @Column(name = "word_id", nullable = false)
    private Integer wordId;

    @Column(name = "is_known")
    private boolean isKnown = false;

    public UserFlashcard() {}

    public UserFlashcard(Integer userId, Integer wordId, boolean isKnown) {
        this.userId = userId;
        this.wordId = wordId;
        this.isKnown = isKnown;
    }
}