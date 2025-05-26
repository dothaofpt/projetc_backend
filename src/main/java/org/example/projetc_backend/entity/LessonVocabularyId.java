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
public class LessonVocabularyId implements Serializable {

    @Column(name = "lesson_id")
    private Integer lessonId;

    @Column(name = "word_id")
    private Integer wordId;

    // ✅ Constructor để sử dụng: new LessonVocabularyId(lessonId, wordId)
    public LessonVocabularyId(Integer lessonId, Integer wordId) {
        this.lessonId = lessonId;
        this.wordId = wordId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof LessonVocabularyId)) return false;
        LessonVocabularyId that = (LessonVocabularyId) o;
        return Objects.equals(lessonId, that.lessonId) &&
                Objects.equals(wordId, that.wordId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(lessonId, wordId);
    }
}
