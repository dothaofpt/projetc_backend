package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.FlashcardPageResponse;
import org.example.projetc_backend.dto.FlashcardResponse;
import org.example.projetc_backend.dto.FlashcardSearchRequest;
import org.example.projetc_backend.dto.UserFlashcardRequest;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.UserFlashcard;
import org.example.projetc_backend.entity.Vocabulary;
import org.example.projetc_backend.repository.UserFlashcardRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.VocabularyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class FlashcardService {

    private final UserFlashcardRepository userFlashcardRepository;
    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository;

    public FlashcardService(UserFlashcardRepository userFlashcardRepository,
                            UserRepository userRepository,
                            VocabularyRepository vocabularyRepository) {
        this.userFlashcardRepository = userFlashcardRepository;
        this.userRepository = userRepository;
        this.vocabularyRepository = vocabularyRepository;
    }

    @Transactional
    public FlashcardResponse createUserFlashcard(UserFlashcardRequest request) {
        if (request == null || request.userId() == null || request.wordId() == null) {
            throw new IllegalArgumentException("User ID và Word ID không được để trống.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        Vocabulary vocabulary = vocabularyRepository.findById(request.wordId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + request.wordId()));

        UserFlashcard userFlashcard = userFlashcardRepository
                .findByUserUserIdAndVocabularyWordId(request.userId(), request.wordId())
                .orElseGet(() -> {
                    UserFlashcard newFlashcard = new UserFlashcard();
                    newFlashcard.setUser(user);
                    newFlashcard.setVocabulary(vocabulary);
                    newFlashcard.setKnown(false);
                    newFlashcard.setEaseFactor(2.5);
                    newFlashcard.setReviewIntervalDays(0);
                    newFlashcard.setLastReviewedAt(LocalDateTime.now());
                    newFlashcard.setNextReviewAt(LocalDateTime.now());
                    return newFlashcard;
                });

        userFlashcard.setKnown(request.isKnown());

        userFlashcard = userFlashcardRepository.save(userFlashcard);
        return mapToFlashcardResponse(userFlashcard);
    }

    @Transactional(readOnly = true)
    public FlashcardResponse getUserFlashcardById(Integer userFlashcardId) {
        if (userFlashcardId == null) {
            throw new IllegalArgumentException("User Flashcard ID không được để trống.");
        }
        UserFlashcard userFlashcard = userFlashcardRepository.findById(userFlashcardId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy flashcard người dùng với ID: " + userFlashcardId));
        return mapToFlashcardResponse(userFlashcard);
    }

    @Transactional(readOnly = true)
    public FlashcardPageResponse searchUserFlashcards(FlashcardSearchRequest request) {
        if (request == null || request.userId() == null) {
            throw new IllegalArgumentException("Search request và User ID không được để trống.");
        }

        userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        String sortBy = request.sortBy();
        if (!List.of("id", "vocabulary.word", "vocabulary.meaning", "vocabulary.difficultyLevel", "isKnown", "lastReviewedAt", "nextReviewAt").contains(sortBy)) {
            sortBy = "id";
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<UserFlashcard> flashcardPage = userFlashcardRepository.searchUserFlashcards(
                request.userId(),
                request.setId(),
                request.wordId(), // Đây là dòng đã sửa để khớp với chữ ký mới trong Repository
                request.word(),
                request.meaning(),
                request.isKnown(),
                request.difficultyLevel(),
                null, // minReviewIntervalDays
                null, // maxReviewIntervalDays
                null, // minEaseFactor
                null, // maxEaseFactor
                pageable
        );

        List<FlashcardResponse> content = flashcardPage.getContent().stream()
                .map(this::mapToFlashcardResponse)
                .collect(Collectors.toList());

        return new FlashcardPageResponse(
                content,
                flashcardPage.getTotalElements(),
                flashcardPage.getTotalPages(),
                flashcardPage.getNumber(),
                flashcardPage.getSize()
        );
    }

    @Transactional
    public void deleteUserFlashcard(Integer userFlashcardId) {
        if (userFlashcardId == null) {
            throw new IllegalArgumentException("User Flashcard ID không được để trống.");
        }
        UserFlashcard flashcard = userFlashcardRepository.findById(userFlashcardId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy flashcard người dùng với ID: " + userFlashcardId));
        userFlashcardRepository.delete(flashcard);
    }

    public FlashcardResponse mapToFlashcardResponse(UserFlashcard userFlashcard) {
        if (userFlashcard == null || userFlashcard.getVocabulary() == null || userFlashcard.getUser() == null) {
            return null;
        }
        Vocabulary vocab = userFlashcard.getVocabulary();
        return new FlashcardResponse(
                userFlashcard.getId(),
                userFlashcard.getUser().getUserId(),
                vocab.getWordId(),
                vocab.getWord(),
                vocab.getMeaning(),
                vocab.getExampleSentence(),
                vocab.getPronunciation(),
                vocab.getAudioUrl(),
                vocab.getImageUrl(),
                vocab.getWritingPrompt(),
                vocab.getDifficultyLevel(),
                userFlashcard.isKnown(),
                userFlashcard.getLastReviewedAt(),
                userFlashcard.getNextReviewAt(),
                userFlashcard.getReviewIntervalDays(),
                userFlashcard.getEaseFactor()
        );
    }
}