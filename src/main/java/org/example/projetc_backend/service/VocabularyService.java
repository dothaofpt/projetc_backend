package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.VocabularyRequest;
import org.example.projetc_backend.dto.VocabularyResponse;
import org.example.projetc_backend.entity.Vocabulary;
import org.example.projetc_backend.repository.VocabularyRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    public VocabularyService(VocabularyRepository vocabularyRepository) {
        this.vocabularyRepository = vocabularyRepository;
    }

    public VocabularyResponse createVocabulary(VocabularyRequest request) {
        if (request == null || request.word() == null || request.meaning() == null || request.difficultyLevel() == null) {
            throw new IllegalArgumentException("Request, word, meaning, hoặc difficultyLevel không được để trống");
        }
        if (vocabularyRepository.existsByWord(request.word())) {
            throw new IllegalArgumentException("Từ vựng đã tồn tại: " + request.word());
        }
        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setWord(request.word());
        vocabulary.setMeaning(request.meaning());
        vocabulary.setExampleSentence(request.exampleSentence() != null ? request.exampleSentence() : "");
        vocabulary.setPronunciation(request.pronunciation() != null ? request.pronunciation() : "");
        vocabulary.setAudioUrl(request.audioUrl() != null ? request.audioUrl() : "");
        vocabulary.setWritingPrompt(request.writingPrompt() != null ? request.writingPrompt() : "");
        vocabulary.setDifficultyLevel(Vocabulary.DifficultyLevel.valueOf(request.difficultyLevel()));
        vocabulary = vocabularyRepository.save(vocabulary);
        return mapToVocabularyResponse(vocabulary);
    }

    public VocabularyResponse getVocabularyById(Integer wordId) {
        if (wordId == null) {
            throw new IllegalArgumentException("Word ID không được để trống");
        }
        Vocabulary vocabulary = vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId));
        return mapToVocabularyResponse(vocabulary);
    }

    public List<VocabularyResponse> getAllVocabulary() {
        return vocabularyRepository.findAll().stream()
                .map(this::mapToVocabularyResponse)
                .collect(Collectors.toList());
    }

    public VocabularyResponse updateVocabulary(Integer wordId, VocabularyRequest request) {
        if (wordId == null || request == null || request.word() == null || request.meaning() == null || request.difficultyLevel() == null) {
            throw new IllegalArgumentException("Word ID, request, word, meaning, hoặc difficultyLevel không được để trống");
        }
        Vocabulary vocabulary = vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId));
        vocabularyRepository.findByWord(request.word())
                .filter(existing -> !existing.getWordId().equals(wordId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Từ vựng đã tồn tại: " + request.word());
                });
        vocabulary.setWord(request.word());
        vocabulary.setMeaning(request.meaning());
        vocabulary.setExampleSentence(request.exampleSentence() != null ? request.exampleSentence() : vocabulary.getExampleSentence());
        vocabulary.setPronunciation(request.pronunciation() != null ? request.pronunciation() : vocabulary.getPronunciation());
        vocabulary.setAudioUrl(request.audioUrl() != null ? request.audioUrl() : vocabulary.getAudioUrl());
        vocabulary.setWritingPrompt(request.writingPrompt() != null ? request.writingPrompt() : vocabulary.getWritingPrompt());
        vocabulary.setDifficultyLevel(Vocabulary.DifficultyLevel.valueOf(request.difficultyLevel()));
        vocabulary = vocabularyRepository.save(vocabulary);
        return mapToVocabularyResponse(vocabulary);
    }

    public void deleteVocabulary(Integer wordId) {
        if (wordId == null) {
            throw new IllegalArgumentException("Word ID không được để trống");
        }
        if (!vocabularyRepository.existsById(wordId)) {
            throw new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId);
        }
        vocabularyRepository.deleteById(wordId);
    }

    private VocabularyResponse mapToVocabularyResponse(Vocabulary vocabulary) {
        return new VocabularyResponse(
                vocabulary.getWordId(),
                vocabulary.getWord(),
                vocabulary.getMeaning(),
                vocabulary.getExampleSentence(),
                vocabulary.getPronunciation(),
                vocabulary.getAudioUrl(),
                vocabulary.getWritingPrompt(),
                vocabulary.getDifficultyLevel().toString()
        );
    }
}