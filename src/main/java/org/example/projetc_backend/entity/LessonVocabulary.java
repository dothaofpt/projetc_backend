package org.example.projetc_backend.entity;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.Data;

@Entity
@Table(name = "LessonVocabulary")
@Data
public class LessonVocabulary {

    @EmbeddedId
    private LessonVocabularyId id;
}