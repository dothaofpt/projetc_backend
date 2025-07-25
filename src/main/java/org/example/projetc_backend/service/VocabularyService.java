package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.VocabularyRequest;
import org.example.projetc_backend.dto.VocabularyResponse;
import org.example.projetc_backend.dto.VocabularySearchRequest;
import org.example.projetc_backend.entity.Vocabulary;
import org.example.projetc_backend.repository.VocabularyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
                request.difficultyLevel() == null) {
            throw new IllegalArgumentException("Các trường bắt buộc (word, meaning, difficultyLevel) không được để trống.");
        }
        // Kiểm tra trùng lặp từ vựng CHƯA BỊ XÓA MỀM
        // SỬA: Dùng findByWordIgnoreCaseAndIsDeletedFalse
        if (vocabularyRepository.findByWordIgnoreCaseAndIsDeletedFalse(request.word().trim()).isPresent()) {
            throw new IllegalArgumentException("Từ vựng đã tồn tại: " + request.word());
        }

        Vocabulary vocabulary = new Vocabulary();
        vocabulary.setWord(request.word().trim());
        vocabulary.setMeaning(request.meaning().trim());
        // Chuẩn hóa các trường tùy chọn thành null nếu rỗng
        vocabulary.setExampleSentence(request.exampleSentence() != null && !request.exampleSentence().trim().isEmpty() ? request.exampleSentence().trim() : null);
        vocabulary.setPronunciation(request.pronunciation() != null && !request.pronunciation().trim().isEmpty() ? request.pronunciation().trim() : null);
        vocabulary.setAudioUrl(request.audioUrl() != null && !request.audioUrl().trim().isEmpty() ? request.audioUrl().trim() : null);
        vocabulary.setImageUrl(request.imageUrl() != null && !request.imageUrl().trim().isEmpty() ? request.imageUrl().trim() : null);
        vocabulary.setWritingPrompt(request.writingPrompt() != null && !request.writingPrompt().trim().isEmpty() ? request.writingPrompt().trim() : null);
        vocabulary.setDifficultyLevel(request.difficultyLevel());
        vocabulary.setDeleted(false); // Đảm bảo khi tạo mới thì isDeleted là false

        // createdAt sẽ được tự động điền bởi @CreationTimestamp trong Entity, không cần set ở đây

        vocabulary = vocabularyRepository.save(vocabulary);
        return mapToVocabularyResponse(vocabulary);
    }

    @Transactional(readOnly = true)
    public VocabularyResponse getVocabularyById(Integer wordId) {
        if (wordId == null) {
            throw new IllegalArgumentException("Word ID không được để trống.");
        }
        // SỬA: Chỉ lấy từ vựng khi isDeleted là false
        Vocabulary vocabulary = vocabularyRepository.findById(wordId)
                .filter(v -> !v.isDeleted()) // Thêm điều kiện filter để chỉ trả về từ vựng CHƯA XÓA MỀM
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId));
        return mapToVocabularyResponse(vocabulary);
    }

    // Phương thức getAllVocabulary này hiện không được Controller gọi và searchVocabularies mạnh mẽ hơn.
    // Nếu không có mục đích cụ thể, có thể xóa.
    @Transactional(readOnly = true)
    public List<VocabularyResponse> getAllVocabulary() {
        // SỬA: Chỉ lấy các từ vựng chưa bị xóa mềm
        return vocabularyRepository.findAll().stream()
                .filter(v -> !v.isDeleted())
                .map(this::mapToVocabularyResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public VocabularyResponse updateVocabulary(Integer wordId, VocabularyRequest request) {
        if (wordId == null || request == null) {
            throw new IllegalArgumentException("Word ID và request không được để trống.");
        }

        Vocabulary vocabulary = vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId));

        // SỬA: Kiểm tra nếu từ vựng đã bị xóa mềm thì không cho cập nhật
        if (vocabulary.isDeleted()) {
            throw new IllegalArgumentException("Không thể cập nhật từ vựng đã bị xóa.");
        }

        // Kiểm tra trùng lặp từ vựng với các từ khác (trừ chính nó) và chỉ xét các từ CHƯA BỊ XÓA MỀM
        if (request.word() != null && !request.word().trim().isEmpty() && !request.word().trim().equalsIgnoreCase(vocabulary.getWord())) {
            // SỬA: Dùng findByWordIgnoreCaseAndIsDeletedFalse để khớp với Repository và logic tốt hơn
            vocabularyRepository.findByWordIgnoreCaseAndIsDeletedFalse(request.word().trim())
                    .filter(existing -> !existing.getWordId().equals(wordId)) // Đảm bảo không so sánh với chính từ vựng đang cập nhật
                    .ifPresent(existing -> {
                        throw new IllegalArgumentException("Từ vựng đã tồn tại: " + request.word());
                    });
            vocabulary.setWord(request.word().trim());
        } else if (request.word() != null && !request.word().trim().isEmpty()) {
            // Nếu từ vựng không thay đổi, hoặc chỉ thay đổi case, không cần kiểm tra trùng lặp với chính nó
            vocabulary.setWord(request.word().trim());
        }

        // Cập nhật các trường tùy chọn, chuẩn hóa null nếu rỗng
        if (request.meaning() != null) vocabulary.setMeaning(request.meaning().trim());
        if (request.exampleSentence() != null) vocabulary.setExampleSentence(request.exampleSentence().trim().isEmpty() ? null : request.exampleSentence().trim());
        if (request.pronunciation() != null) vocabulary.setPronunciation(request.pronunciation().trim().isEmpty() ? null : request.pronunciation().trim());
        if (request.audioUrl() != null) vocabulary.setAudioUrl(request.audioUrl().trim().isEmpty() ? null : request.audioUrl().trim());
        if (request.imageUrl() != null) vocabulary.setImageUrl(request.imageUrl().trim().isEmpty() ? null : request.imageUrl().trim());
        if (request.writingPrompt() != null) vocabulary.setWritingPrompt(request.writingPrompt().trim().isEmpty() ? null : request.writingPrompt().trim());
        if (request.difficultyLevel() != null) vocabulary.setDifficultyLevel(request.difficultyLevel());

        // updatedAt sẽ được tự động điền bởi @UpdateTimestamp trong Entity

        vocabulary = vocabularyRepository.save(vocabulary);
        return mapToVocabularyResponse(vocabulary);
    }

    @Transactional
    public void deleteVocabulary(Integer wordId) {
        if (wordId == null) {
            throw new IllegalArgumentException("Word ID không được để trống.");
        }
        // SỬA: Thực hiện xóa mềm
        Vocabulary vocabulary = vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId));

        if (vocabulary.isDeleted()) {
            throw new IllegalArgumentException("Từ vựng đã bị xóa."); // Hoặc có thể bỏ qua nếu đã xóa rồi
        }

        vocabulary.setDeleted(true); // Đánh dấu là đã xóa mềm
        vocabularyRepository.save(vocabulary); // Lưu lại trạng thái đã xóa mềm
    }

    @Transactional(readOnly = true)
    public Page<VocabularyResponse> searchVocabularies(VocabularySearchRequest searchRequest) {
        Sort sort = Sort.by(Sort.Direction.fromString(searchRequest.sortDir()), searchRequest.sortBy());
        Pageable pageable = PageRequest.of(searchRequest.page(), searchRequest.size(), sort);

        String wordParam = searchRequest.word() != null ? searchRequest.word().trim() : null;
        String meaningParam = searchRequest.meaning() != null ? searchRequest.meaning().trim() : null;

        // `searchVocabularies` trong Repository đã có điều kiện `isDeleted = false`
        Page<Vocabulary> vocabularyPage = vocabularyRepository.searchVocabularies(
                wordParam,
                meaningParam,
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
                vocabulary.getImageUrl(),
                vocabulary.getWritingPrompt(),
                vocabulary.getDifficultyLevel()
        );
    }
}