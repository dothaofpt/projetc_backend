package org.example.projetc_backend.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.io.Serializable;
import java.util.Objects;

@Embeddable
@Data
@NoArgsConstructor
public class FlashcardSetVocabularyId implements Serializable {
    @Column(name = "set_id")
    private Integer setId;

    @Column(name = "word_id")
    private Integer wordId;

    public FlashcardSetVocabularyId(Integer setId, Integer wordId) {
        this.setId = setId;
        this.wordId = wordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof FlashcardSetVocabularyId)) return false;
        FlashcardSetVocabularyId that = (FlashcardSetVocabularyId) o;
        return Objects.equals(setId, that.setId) &&
                Objects.equals(wordId, that.wordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(setId, wordId);
    }
}