package org.example.projetc_backend.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.ManyToOne; // Bổ sung
import jakarta.persistence.MapsId;    // Bổ sung
import jakarta.persistence.JoinColumn; // Bổ sung
import lombok.Data;

@Entity
@Table(name = "LessonVocabulary")
@Data
public class LessonVocabulary {
    @EmbeddedId
    private LessonVocabularyId id;

    @ManyToOne
    @MapsId("lessonId") // Ánh xạ phần lessonId của khóa nhúng
    @JoinColumn(name = "lesson_id")
    private Lesson lesson;

    @ManyToOne
    @MapsId("wordId") // Ánh xạ phần wordId của khóa nhúng
    @JoinColumn(name = "word_id")
    private Vocabulary vocabulary;
}