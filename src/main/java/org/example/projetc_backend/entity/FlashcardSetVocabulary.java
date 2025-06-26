package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;

@Entity
@Table(name = "FlashcardSetVocabulary")
@Data
@NoArgsConstructor
public class FlashcardSetVocabulary {

    @EmbeddedId
    private FlashcardSetVocabularyId id;

    @ManyToOne
    @MapsId("setId") // Ánh xạ phần setId của khóa nhúng
    @JoinColumn(name = "set_id")
    private FlashcardSet flashcardSet;

    @ManyToOne
    @MapsId("wordId") // Ánh xạ phần wordId của khóa nhúng
    @JoinColumn(name = "word_id")
    private Vocabulary vocabulary;

    public FlashcardSetVocabulary(FlashcardSet flashcardSet, Vocabulary vocabulary) {
        this.flashcardSet = flashcardSet;
        this.vocabulary = vocabulary;
        this.id = new FlashcardSetVocabularyId(flashcardSet.getSetId(), vocabulary.getWordId());
    }
}