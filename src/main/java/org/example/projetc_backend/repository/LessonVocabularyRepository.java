package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.LessonVocabulary;
import org.example.projetc_backend.entity.LessonVocabularyId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface LessonVocabularyRepository extends JpaRepository<LessonVocabulary, LessonVocabularyId> {
    List<LessonVocabulary> findByIdLessonId(Integer lessonId);
    List<LessonVocabulary> findByIdWordId(Integer wordId);
}