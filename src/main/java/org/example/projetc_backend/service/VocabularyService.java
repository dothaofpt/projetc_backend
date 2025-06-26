package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.VocabularyRequest;
import org.example.projetc_backend.dto.VocabularyResponse;
import org.example.projetc_backend.dto.VocabularySearchRequest; // Thêm import này
import org.example.projetc_backend.entity.Vocabulary;
import org.example.projetc_backend.repository.VocabularyRepository;
import org.springframework.data.domain.Page; // Thêm import này
import org.springframework.data.domain.PageRequest; // Thêm import này
import org.springframework.data.domain.Pageable; // Thêm import này
import org.springframework.data.domain.Sort; // Thêm import này
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class VocabularyService {

    private final VocabularyRepository vocabularyRepository;

    public VocabularyService(VocabularyRepository vocabularyRepository) {
        this.vocabularyRepository = vocabularyRepository;
    }

    @Transactional
    public VocabularyResponse createVocabulary(VocabularyRequest request) {
        if (request == null || request.word() == null || request.word().trim().isEmpty() ||
                request.meaning() == null || request.meaning().trim().isEmpty() ||
                request.difficultyLevel() == null) { // Kiểm tra trực tiếp enum
            throw new IllegalArgumentException("Các trường bắt buộc (word, meaning, difficultyLevel) không được để trống.");
        }
        // Kiểm tra trùng lặp từ vựng (không phân biệt chữ hoa/thường)
        if (vocabularyRepository.findByWord(request.word().trim()).isPresent()) { // Sử dụng findByWord và Optional
            throw new IllegalArgumentException("Từ vựng đã tồn tại: " + request.word());
        }

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setWord(request.word().trim());
        vocabulary.setMeaning(request.meaning().trim());
        vocabulary.setExampleSentence(request.exampleSentence() != null ? request.exampleSentence().trim() : null); // null thay vì ""
        vocabulary.setPronunciation(request.pronunciation() != null ? request.pronunciation().trim() : null); // null thay vì ""
        vocabulary.setAudioUrl(request.audioUrl() != null ? request.audioUrl().trim() : null);
        vocabulary.setImageUrl(request.imageUrl() != null ? request.imageUrl().trim() : null); // Thêm imageUrl
        vocabulary.setWritingPrompt(request.writingPrompt() != null ? request.writingPrompt().trim() : null);
        vocabulary.setDifficultyLevel(request.difficultyLevel()); // Sử dụng trực tiếp enum từ request

        vocabulary = vocabularyRepository.save(vocabulary);
        return mapToVocabularyResponse(vocabulary);
    }

    @Transactional(readOnly = true)
    public VocabularyResponse getVocabularyById(Integer wordId) {
        if (wordId == null) {
            throw new IllegalArgumentException("Word ID không được để trống.");
        }
        Vocabulary vocabulary = vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId));
        return mapToVocabularyResponse(vocabulary);
    }

    @Transactional(readOnly = true)
    public List<VocabularyResponse> getAllVocabulary() {
        return vocabularyRepository.findAll().stream()
                .map(this::mapToVocabularyResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VocabularyResponse updateVocabulary(Integer wordId, VocabularyRequest request) {
        if (wordId == null || request == null || request.word() == null || request.word().trim().isEmpty() ||
                request.meaning() == null || request.meaning().trim().isEmpty() ||
                request.difficultyLevel() == null) { // Kiểm tra trực tiếp enum
            throw new IllegalArgumentException("Word ID, request, word, meaning, hoặc difficultyLevel không được để trống.");
        }

        Vocabulary vocabulary = vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId));

        // Kiểm tra trùng lặp từ vựng với các từ khác (trừ chính nó)
        vocabularyRepository.findByWord(request.word().trim())
                .filter(existing -> !existing.getWordId().equals(wordId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Từ vựng đã tồn tại: " + request.word());
                });

        vocabulary.setWord(request.word().trim());
        vocabulary.setMeaning(request.meaning().trim());

        // Cập nhật các trường tùy chọn, cho phép null nếu được truyền vào
        vocabulary.setExampleSentence(request.exampleSentence() != null ? request.exampleSentence().trim() : null);
        vocabulary.setPronunciation(request.pronunciation() != null ? request.pronunciation().trim() : null);
        vocabulary.setAudioUrl(request.audioUrl() != null ? request.audioUrl().trim() : null);
        vocabulary.setImageUrl(request.imageUrl() != null ? request.imageUrl().trim() : null); // Cập nhật imageUrl
        vocabulary.setWritingPrompt(request.writingPrompt() != null ? request.writingPrompt().trim() : null);
        vocabulary.setDifficultyLevel(request.difficultyLevel()); // Sử dụng trực tiếp enum từ request

        vocabulary = vocabularyRepository.save(vocabulary);
        return mapToVocabularyResponse(vocabulary);
    }

    @Transactional
    public void deleteVocabulary(Integer wordId) {
        if (wordId == null) {
            throw new IllegalArgumentException("Word ID không được để trống.");
        }
        if (!vocabularyRepository.existsById(wordId)) {
            throw new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId);
        }
        vocabularyRepository.deleteById(wordId);
    }

    /**
     * Tìm kiếm và phân trang từ vựng.
     * @param searchRequest DTO chứa các tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return Page<VocabularyResponse> chứa danh sách từ vựng và thông tin phân trang.
     */
    @Transactional(readOnly = true)
    public Page<VocabularyResponse> searchVocabularies(VocabularySearchRequest searchRequest) {
        Sort sort = Sort.by(Sort.Direction.fromString(searchRequest.sortDir()), searchRequest.sortBy());
        Pageable pageable = PageRequest.of(searchRequest.page(), searchRequest.size(), sort);

        Page<Vocabulary> vocabularyPage = vocabularyRepository.searchVocabularies(
                searchRequest.word() != null ? searchRequest.word().trim() : null,
                searchRequest.meaning() != null ? searchRequest.meaning().trim() : null,
                searchRequest.difficultyLevel(),
                pageable
        );

        return vocabularyPage.map(this::mapToVocabularyResponse);
    }


    private VocabularyResponse mapToVocabularyResponse(Vocabulary vocabulary) {
        return new VocabularyResponse(
                vocabulary.getWordId(),
                vocabulary.getWord(),
                vocabulary.getMeaning(),
                vocabulary.getExampleSentence(),
                vocabulary.getPronunciation(),
                vocabulary.getAudioUrl(),
                vocabulary.getImageUrl(), // Thêm imageUrl vào response
                vocabulary.getWritingPrompt(),
                vocabulary.getDifficultyLevel() // Trả về enum trực tiếp
        );
    }
}