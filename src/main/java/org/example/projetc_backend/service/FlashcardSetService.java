package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.FlashcardSetRequest;
import org.example.projetc_backend.dto.FlashcardSetResponse;
import org.example.projetc_backend.dto.FlashcardSetSearchRequest;
import org.example.projetc_backend.dto.VocabularyResponse;
import org.example.projetc_backend.entity.FlashcardSet;
import org.example.projetc_backend.entity.FlashcardSetVocabulary;
import org.example.projetc_backend.entity.FlashcardSetVocabularyId;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Vocabulary;
import org.example.projetc_backend.repository.FlashcardSetRepository;
import org.example.projetc_backend.repository.FlashcardSetVocabularyRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.VocabularyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlashcardSetService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository;
    private final FlashcardSetVocabularyRepository flashcardSetVocabularyRepository;

    public FlashcardSetService(FlashcardSetRepository flashcardSetRepository,
                               UserRepository userRepository,
                               VocabularyRepository vocabularyRepository,
                               FlashcardSetVocabularyRepository flashcardSetVocabularyRepository) {
        this.flashcardSetRepository = flashcardSetRepository;
        this.userRepository = userRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.flashcardSetVocabularyRepository = flashcardSetVocabularyRepository;
    }

    /**
     * Tạo một bộ flashcard mới.
     *
     * @param request Dữ liệu yêu cầu tạo bộ flashcard.
     * @return FlashcardSetResponse của bộ flashcard đã tạo.
     * @throws IllegalArgumentException Nếu tiêu đề trống, creatorUserId không hợp lệ, hoặc bộ flashcard đã tồn tại.
     */
    @Transactional
    public FlashcardSetResponse createFlashcardSet(FlashcardSetRequest request) {
        if (request == null || request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề của bộ flashcard là bắt buộc.");
        }
        if (request.creatorUserId() == null && !Boolean.TRUE.equals(request.isSystemCreated())) {
            throw new IllegalArgumentException("Người tạo bộ flashcard (creatorUserId) là bắt buộc nếu không phải là bộ do hệ thống tạo.");
        }

        User creator = null;
        if (request.creatorUserId() != null) {
            creator = userRepository.findById(request.creatorUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người tạo với ID: " + request.creatorUserId()));
        }

        // SỬA: Sử dụng findByTitle vì không có findByTitleAndIsDeletedFalse trong Repository
        Optional<FlashcardSet> existingSet = flashcardSetRepository.findByTitle(request.title());
        if (existingSet.isPresent()) {
            throw new IllegalArgumentException("Bộ flashcard với tiêu đề '" + request.title() + "' đã tồn tại.");
        }

        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setTitle(request.title().trim());
        flashcardSet.setDescription(request.description() != null ? request.description().trim() : null);
        flashcardSet.setCreator(creator);
        flashcardSet.setSystemCreated(Boolean.TRUE.equals(request.isSystemCreated()));
        flashcardSet.setCreatedAt(LocalDateTime.now());
        // BỎ QUA: flashcardSet.setDeleted(false); vì không có trường isDeleted trong entity
        flashcardSet = flashcardSetRepository.save(flashcardSet);

        // Thêm từ vựng vào bộ nếu có danh sách wordIds được cung cấp
        if (request.wordIds() != null && !request.wordIds().isEmpty()) {
            for (Integer wordId : request.wordIds()) {
                // Kiểm tra xem flashcardSet.getSetId() có trả về giá trị null không
                if (flashcardSet.getSetId() != null) {
                    addVocabularyToSet(flashcardSet.getSetId(), wordId);
                } else {
                    // Xử lý trường hợp setId là null nếu save không gán ID ngay lập tức
                    // Trong trường hợp dùng IDENTITY, ID sẽ có sau khi save.
                    throw new IllegalStateException("Set ID không được tạo sau khi lưu FlashcardSet.");
                }
            }
        }

        return mapToFlashcardSetResponse(flashcardSet);
    }

    /**
     * Lấy thông tin chi tiết của một bộ flashcard theo ID.
     *
     * @param setId ID của bộ flashcard.
     * @return FlashcardSetResponse của bộ flashcard.
     * @throws IllegalArgumentException Nếu Set ID trống hoặc không tìm thấy bộ flashcard.
     */
    @Transactional(readOnly = true)
    public FlashcardSetResponse getFlashcardSetById(Integer setId) {
        if (setId == null) {
            throw new IllegalArgumentException("Set ID không được để trống.");
        }
        // SỬA: Sử dụng findById vì không có findBySetIdAndIsDeletedFalse trong Repository
        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ flashcard với ID: " + setId));
        return mapToFlashcardSetResponse(flashcardSet);
    }

    /**
     * Tìm kiếm và phân trang các bộ flashcard.
     *
     * @param request Các tiêu chí tìm kiếm và phân trang.
     * @return Page chứa danh sách các FlashcardSetResponse.
     * @throws IllegalArgumentException Nếu Search request trống.
     */
    @Transactional(readOnly = true)
    public Page<FlashcardSetResponse> searchFlashcardSets(FlashcardSetSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        String sortBy = request.sortBy();
        if (!List.of("setId", "title", "createdAt", "creator.userId", "isSystemCreated").contains(sortBy)) {
            sortBy = "setId"; // Mặc định sắp xếp theo ID
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        // Giả định FlashcardSetRepository có phương thức search mạnh mẽ:
        // Page<FlashcardSet> searchFlashcardSets(String title, Boolean isSystemCreated, Integer creatorUserId, Pageable pageable);
        // Phương thức này không bao gồm điều kiện `isDeleted = false` vì Entity không có trường đó.
        Page<FlashcardSet> sets = flashcardSetRepository.searchFlashcardSets(
                request.title(),
                request.isSystemCreated(),
                request.creatorUserId(),
                pageable
        );

        return sets.map(this::mapToFlashcardSetResponse);
    }

    /**
     * Cập nhật thông tin của một bộ flashcard.
     *
     * @param setId ID của bộ flashcard cần cập nhật.
     * @param request Dữ liệu yêu cầu cập nhật.
     * @return FlashcardSetResponse của bộ flashcard đã cập nhật.
     * @throws IllegalArgumentException Nếu Set ID, tiêu đề trống, không tìm thấy bộ flashcard, hoặc tiêu đề trùng lặp.
     */
    @Transactional
    public FlashcardSetResponse updateFlashcardSet(Integer setId, FlashcardSetRequest request) {
        if (setId == null || request == null || request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Set ID và tiêu đề của bộ flashcard là bắt buộc.");
        }

        // SỬA: Sử dụng findById vì không có findBySetIdAndIsDeletedFalse trong Repository
        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ flashcard với ID: " + setId));

        // Kiểm tra tiêu đề trùng lặp với các bộ khác
        // SỬA: Sử dụng findByTitle vì không có findByTitleAndIsDeletedFalse
        flashcardSetRepository.findByTitle(request.title())
                .filter(fs -> !fs.getSetId().equals(setId)) // Đảm bảo không phải chính nó
                .ifPresent(fs -> {
                    throw new IllegalArgumentException("Bộ flashcard với tiêu đề '" + request.title() + "' đã tồn tại.");
                });

        flashcardSet.setTitle(request.title().trim());
        flashcardSet.setDescription(request.description() != null ? request.description().trim() : null);
        if (request.isSystemCreated() != null) {
            flashcardSet.setSystemCreated(request.isSystemCreated());
        }

        // Cập nhật người tạo nếu có sự thay đổi
        if (request.creatorUserId() != null) {
            if (flashcardSet.getCreator() == null || !flashcardSet.getCreator().getUserId().equals(request.creatorUserId())) {
                User newCreator = userRepository.findById(request.creatorUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người tạo mới với ID: " + request.creatorUserId()));
                flashcardSet.setCreator(newCreator);
            }
        } else {
            // Nếu creatorUserId là null trong request, và bộ không phải do hệ thống tạo, đặt creator về null.
            if (!flashcardSet.isSystemCreated()) {
                flashcardSet.setCreator(null);
            }
        }

        flashcardSet = flashcardSetRepository.save(flashcardSet);

        // Cập nhật danh sách từ vựng trong bộ
        if (request.wordIds() != null) {
            List<Integer> currentWordIds = flashcardSetVocabularyRepository.findByFlashcardSetSetId(setId).stream()
                    .map(fsv -> fsv.getVocabulary().getWordId())
                    .collect(Collectors.toList());

            List<Integer> wordsToAdd = request.wordIds().stream()
                    .filter(wordId -> !currentWordIds.contains(wordId))
                    .collect(Collectors.toList());

            List<Integer> wordsToRemove = currentWordIds.stream()
                    .filter(wordId -> !request.wordIds().contains(wordId))
                    .collect(Collectors.toList());

            // Thêm các từ vựng mới
            wordsToAdd.forEach(wordId -> addVocabularyToSet(setId, wordId));
            // Xóa các từ vựng không còn trong danh sách
            wordsToRemove.forEach(wordId -> removeVocabularyFromSet(setId, wordId));
        }

        return mapToFlashcardSetResponse(flashcardSet);
    }

    /**
     * Xóa một bộ flashcard (hard delete).
     *
     * @param setId ID của bộ flashcard cần xóa.
     * @throws IllegalArgumentException Nếu Set ID trống hoặc không tìm thấy bộ flashcard.
     */
    @Transactional
    public void deleteFlashcardSet(Integer setId) {
        if (setId == null) {
            throw new IllegalArgumentException("Set ID không được để trống.");
        }
        // SỬA: Sử dụng findById vì không có findBySetIdAndIsDeletedFalse trong Repository
        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ flashcard với ID: " + setId));

        // BỎ QUA: flashcardSet.setDeleted(true); vì không có trường isDeleted trong entity
        flashcardSetRepository.delete(flashcardSet); // Thực hiện hard delete
    }

    /**
     * Thêm một từ vựng vào một bộ flashcard.
     *
     * @param setId ID của bộ flashcard.
     * @param wordId ID của từ vựng.
     * @throws IllegalArgumentException Nếu Set ID hoặc Word ID trống, không tìm thấy bộ flashcard/từ vựng, hoặc từ vựng đã tồn tại trong bộ.
     */
    @Transactional
    public void addVocabularyToSet(Integer setId, Integer wordId) {
        if (setId == null || wordId == null) {
            throw new IllegalArgumentException("Set ID và Word ID không được để trống.");
        }

        // SỬA: Sử dụng findById vì không có findBySetIdAndIsDeletedFalse trong Repository
        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ flashcard với ID: " + setId));
        // SỬA: Sử dụng findById vì không có findByWordIdAndIsDeletedFalse trong Repository
        Vocabulary vocabulary = vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId));

        if (flashcardSetVocabularyRepository.existsByFlashcardSetSetIdAndVocabularyWordId(setId, wordId)) {
            throw new IllegalArgumentException("Từ vựng ID " + wordId + " đã tồn tại trong bộ flashcard ID " + setId + ".");
        }

        FlashcardSetVocabulary fsv = new FlashcardSetVocabulary(flashcardSet, vocabulary);
        flashcardSetVocabularyRepository.save(fsv);
    }

    /**
     * Xóa một từ vựng khỏi một bộ flashcard.
     *
     * @param setId ID của bộ flashcard.
     * @param wordId ID của từ vựng.
     * @throws IllegalArgumentException Nếu Set ID hoặc Word ID trống, hoặc không tìm thấy liên kết.
     */
    @Transactional
    public void removeVocabularyFromSet(Integer setId, Integer wordId) {
        if (setId == null || wordId == null) {
            throw new IllegalArgumentException("Set ID và Word ID không được để trống.");
        }
        FlashcardSetVocabulary fsv = flashcardSetVocabularyRepository.findById(new FlashcardSetVocabularyId(setId, wordId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy liên kết từ vựng ID " + wordId + " trong bộ flashcard ID " + setId + "."));

        flashcardSetVocabularyRepository.delete(fsv);
    }

    // --- Private mapping methods ---

    /**
     * Ánh xạ một Entity FlashcardSet sang FlashcardSetResponse DTO, bao gồm danh sách từ vựng.
     *
     * @param flashcardSet Entity FlashcardSet.
     * @return FlashcardSetResponse DTO.
     */
    private FlashcardSetResponse mapToFlashcardSetResponse(FlashcardSet flashcardSet) {
        if (flashcardSet == null) {
            return null;
        }

        List<VocabularyResponse> vocabulariesInSet = flashcardSetVocabularyRepository.findByFlashcardSetSetId(flashcardSet.getSetId()).stream()
                .map(FlashcardSetVocabulary::getVocabulary)
                .filter(v -> v != null) // BỎ QUA: !v.isDeleted() vì Vocabulary không có isDeleted
                .map(this::mapVocabularyToResponse)
                .collect(Collectors.toList());

        return new FlashcardSetResponse(
                flashcardSet.getSetId(),
                flashcardSet.getTitle(),
                flashcardSet.getDescription(),
                flashcardSet.getCreator() != null ? flashcardSet.getCreator().getUserId() : null,
                flashcardSet.isSystemCreated(),
                flashcardSet.getCreatedAt(),
                vocabulariesInSet
        );
    }

    /**
     * Ánh xạ một Entity Vocabulary sang VocabularyResponse DTO.
     *
     * @param vocabulary Entity Vocabulary.
     * @return VocabularyResponse DTO.
     */
    private VocabularyResponse mapVocabularyToResponse(Vocabulary vocabulary) {
        if (vocabulary == null) {
            return null;
        }
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