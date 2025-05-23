package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.FlashcardResponse;
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

    public FlashcardService(
            LessonVocabularyRepository lessonVocabularyRepository,
            VocabularyRepository vocabularyRepository,
            UserFlashcardRepository userFlashcardRepository) {
        this.lessonVocabularyRepository = lessonVocabularyRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.userFlashcardRepository = userFlashcardRepository;
    }

    public List<FlashcardResponse> getFlashcardsByLesson(Integer lessonId, Integer userId) {
        List<LessonVocabulary> lessonVocabularies = lessonVocabularyRepository.findByIdLessonId(lessonId);
        if (lessonVocabularies.isEmpty()) {
            throw new IllegalArgumentException("No vocabulary found for this lesson");
        }

        return lessonVocabularies.stream()
                .map(lv -> {
                    Vocabulary vocab = vocabularyRepository.findById(lv.getId().getWordId())
                            .orElseThrow(() -> new IllegalArgumentException("Vocabulary not found"));
                    boolean isKnown = userFlashcardRepository
                            .findByUserIdAndWordId(userId, lv.getId().getWordId())
                            .map(UserFlashcard::isKnown)
                            .orElse(false);
                    return FlashcardResponse.fromVocabulary(vocab, isKnown);
                })
                .collect(Collectors.toList());
    }

    public void markFlashcard(Integer userId, Integer wordId, boolean isKnown) {
        vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Word not found"));

        UserFlashcard flashcard = userFlashcardRepository
                .findByUserIdAndWordId(userId, wordId)
                .orElse(new UserFlashcard(userId, wordId, isKnown));

        flashcard.setKnown(isKnown);
        userFlashcardRepository.save(flashcard);
    }
}