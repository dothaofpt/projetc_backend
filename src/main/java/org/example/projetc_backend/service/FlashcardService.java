package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.FlashcardResponse;
import org.example.projetc_backend.dto.UserFlashcardRequest;
import org.example.projetc_backend.dto.UserFlashcardResponse;
import org.example.projetc_backend.entity.LessonVocabulary;
import org.example.projetc_backend.entity.UserFlashcard;
import org.example.projetc_backend.entity.Vocabulary;
import org.example.projetc_backend.repository.LessonVocabularyRepository;
import org.example.projetc_backend.repository.UserFlashcardRepository;
import org.example.projetc_backend.repository.VocabularyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlashcardService {

    private final LessonVocabularyRepository lessonVocabularyRepository;
    private final VocabularyRepository vocabularyRepository;
    private final UserFlashcardRepository userFlashcardRepository;

    public FlashcardService(LessonVocabularyRepository lessonVocabularyRepository,
                            VocabularyRepository vocabularyRepository,
                            UserFlashcardRepository userFlashcardRepository) {
        this.lessonVocabularyRepository = lessonVocabularyRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.userFlashcardRepository = userFlashcardRepository;
    }

    public List<FlashcardResponse> getFlashcardsByLesson(Integer lessonId, Integer userId) {
        if (lessonId == null || userId == null) {
            throw new IllegalArgumentException("Lesson ID và User ID không được để trống");
        }
        List<LessonVocabulary> lessonVocabularies = lessonVocabularyRepository.findByIdLessonId(lessonId);
        if (lessonVocabularies.isEmpty()) {
            throw new IllegalArgumentException("Không tìm thấy từ vựng cho bài học ID: " + lessonId);
        }
        return lessonVocabularies.stream()
                .map(lv -> {
                    Vocabulary vocab = vocabularyRepository.findById(lv.getId().getWordId())
                            .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + lv.getId().getWordId()));
                    boolean isKnown = userFlashcardRepository
                            .findByUserIdAndWordId(userId, lv.getId().getWordId())
                            .map(UserFlashcard::isKnown)
                            .orElse(false);
                    return FlashcardResponse.fromVocabulary(vocab, isKnown);
                })
                .collect(Collectors.toList());
    }

    public UserFlashcardResponse markFlashcard(UserFlashcardRequest request) {
        if (request == null || request.userId() == null || request.wordId() == null) {
            throw new IllegalArgumentException("User ID và Word ID không được để trống");
        }
        vocabularyRepository.findById(request.wordId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + request.wordId()));
        UserFlashcard flashcard = userFlashcardRepository
                .findByUserIdAndWordId(request.userId(), request.wordId())
                .orElse(new UserFlashcard(request.userId(), request.wordId(), request.isKnown()));
        flashcard.setKnown(request.isKnown());
        flashcard = userFlashcardRepository.save(flashcard);
        return new UserFlashcardResponse(
                flashcard.getId(),
                flashcard.getUserId(),
                flashcard.getWordId(),
                flashcard.isKnown()
        );
    }
}