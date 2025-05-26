
package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.LessonVocabularyRequest;
import org.example.projetc_backend.dto.LessonVocabularyResponse;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.LessonVocabulary;
import org.example.projetc_backend.entity.LessonVocabularyId;
import org.example.projetc_backend.entity.Vocabulary;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.LessonVocabularyRepository;
import org.example.projetc_backend.repository.VocabularyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LessonVocabularyService {

    private final LessonVocabularyRepository lessonVocabularyRepository;
    private final LessonRepository lessonRepository;
    private final VocabularyRepository vocabularyRepository;

    public LessonVocabularyService(LessonVocabularyRepository lessonVocabularyRepository,
                                   LessonRepository lessonRepository,
                                   VocabularyRepository vocabularyRepository) {
        this.lessonVocabularyRepository = lessonVocabularyRepository;
        this.lessonRepository = lessonRepository;
        this.vocabularyRepository = vocabularyRepository;
    }

    public LessonVocabularyResponse createLessonVocabulary(LessonVocabularyRequest request) {
        if (request == null || request.lessonId() == null || request.wordId() == null) {
            throw new IllegalArgumentException("Request, lessonId, hoặc wordId không được để trống");
        }

        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        Vocabulary vocabulary = vocabularyRepository.findById(request.wordId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + request.wordId()));

        LessonVocabulary lessonVocabulary = new LessonVocabulary();
        lessonVocabulary.setId(new LessonVocabularyId(request.lessonId(), request.wordId()));

        lessonVocabulary = lessonVocabularyRepository.save(lessonVocabulary);

        return new LessonVocabularyResponse(
                lessonVocabulary.getId().getLessonId(),
                lessonVocabulary.getId().getWordId()
        );
    }

    public List<LessonVocabularyResponse> getLessonVocabulariesByLessonId(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống");
        }

        return lessonVocabularyRepository.findByIdLessonId(lessonId).stream()
                .map(lv -> new LessonVocabularyResponse(
                        lv.getId().getLessonId(),
                        lv.getId().getWordId()))
                .collect(Collectors.toList());
    }

    public void deleteLessonVocabulary(Integer lessonId, Integer wordId) {
        if (lessonId == null || wordId == null) {
            throw new IllegalArgumentException("Lesson ID và Word ID không được để trống");
        }

        LessonVocabularyId id = new LessonVocabularyId(lessonId, wordId);
        if (!lessonVocabularyRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy liên kết với lessonId: " + lessonId + " và wordId: " + wordId);
        }

        lessonVocabularyRepository.deleteById(id);
    }
}
