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

    /**
     * Tạo hoặc cập nhật một UserFlashcard.
     * Phương thức này sẽ quản lý các thuộc tính liên quan đến quá trình học tập (spaced repetition).
     *
     * @param request DTO UserFlashcardRequest chứa thông tin cần thiết.
     * @return FlashcardResponse của UserFlashcard đã được tạo/cập nhật.
     * @throws IllegalArgumentException Nếu User ID hoặc Word ID trống, hoặc không tìm thấy người dùng/từ vựng.
     */
    @Transactional
    public FlashcardResponse createUserFlashcard(UserFlashcardRequest request) {
        if (request == null || request.userId() == null || request.wordId() == null) {
            throw new IllegalArgumentException("User ID và Word ID không được để trống.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        // SỬA: Sử dụng findById vì Vocabulary không có isDeleted trong entity cung cấp
        Vocabulary vocabulary = vocabularyRepository.findById(request.wordId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + request.wordId()));

        UserFlashcard userFlashcard = userFlashcardRepository
                .findByUserUserIdAndVocabularyWordId(request.userId(), request.wordId())
                .orElseGet(() -> {
                    // Nếu chưa tồn tại, tạo mới UserFlashcard với các giá trị mặc định cho spaced repetition
                    UserFlashcard newFlashcard = new UserFlashcard();
                    newFlashcard.setUser(user);
                    newFlashcard.setVocabulary(vocabulary);
                    newFlashcard.setKnown(false); // Mặc định là chưa biết
                    newFlashcard.setEaseFactor(2.5); // Giá trị mặc định cho SM-2 algorithm
                    newFlashcard.setReviewIntervalDays(0); // Bắt đầu từ 0 ngày
                    newFlashcard.setLastReviewedAt(LocalDateTime.now()); // Lần đầu tạo coi như đã xem xét
                    newFlashcard.setNextReviewAt(LocalDateTime.now()); // Có thể xem xét ngay
                    return newFlashcard;
                });

        // Cập nhật trạng thái isKnown từ request
        userFlashcard.setKnown(request.isKnown());

        userFlashcard = userFlashcardRepository.save(userFlashcard);
        return mapToFlashcardResponse(userFlashcard);
    }

    /**
     * Lấy một UserFlashcard cụ thể của người dùng.
     *
     * @param userFlashcardId ID của UserFlashcard.
     * @return FlashcardResponse.
     * @throws IllegalArgumentException Nếu không tìm thấy flashcard người dùng.
     */
    @Transactional(readOnly = true)
    public FlashcardResponse getUserFlashcardById(Integer userFlashcardId) {
        if (userFlashcardId == null) {
            throw new IllegalArgumentException("User Flashcard ID không được để trống.");
        }
        UserFlashcard userFlashcard = userFlashcardRepository.findById(userFlashcardId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy flashcard người dùng với ID: " + userFlashcardId));
        return mapToFlashcardResponse(userFlashcard);
    }

    /**
     * Tìm kiếm và phân trang các flashcard của người dùng.
     * Cho phép lọc theo User ID, Word ID, Set ID, từ khóa, nghĩa, trạng thái biết/chưa biết và mức độ khó.
     *
     * @param request Các tiêu chí tìm kiếm và phân trang.
     * @return FlashcardPageResponse chứa danh sách các FlashcardResponse.
     * @throws IllegalArgumentException Nếu User ID trong request trống hoặc không tìm thấy người dùng.
     */
    @Transactional(readOnly = true)
    public FlashcardPageResponse searchUserFlashcards(FlashcardSearchRequest request) {
        if (request == null || request.userId() == null) {
            throw new IllegalArgumentException("Search request và User ID không được để trống.");
        }

        userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));

        String sortBy = request.sortBy();
        // Cần đảm bảo khớp với entity hoặc alias được sử dụng trong truy vấn Repository
        if (!List.of("id", "vocabulary.word", "vocabulary.meaning", "vocabulary.difficultyLevel", "isKnown", "lastReviewedAt", "nextReviewAt").contains(sortBy)) {
            sortBy = "id"; // Mặc định sắp xếp theo ID của UserFlashcard
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        // SỬA: Cập nhật lời gọi phương thức searchUserFlashcards để khớp với chữ ký của UserFlashcardRepository
        // Các tham số `minReviewIntervalDays`, `maxReviewIntervalDays`, `minEaseFactor`, `maxEaseFactor`
        // không có trong FlashcardSearchRequest DTO của bạn. Để tránh lỗi, tôi sẽ truyền null cho các tham số này.
        Page<UserFlashcard> flashcardPage = userFlashcardRepository.searchUserFlashcards(
                request.userId(),
                request.setId(),
                request.wordId(), // Thêm wordId
                request.word(),
                request.meaning(),
                request.isKnown(),
                request.difficultyLevel(),
                null, // minReviewIntervalDays (không có trong DTO)
                null, // maxReviewIntervalDays (không có trong DTO)
                null, // minEaseFactor (không có trong DTO)
                null, // maxEaseFactor (không có trong DTO)
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

    /**
     * Xóa một UserFlashcard.
     *
     * @param userFlashcardId ID của UserFlashcard cần xóa.
     * @throws IllegalArgumentException Nếu User Flashcard ID trống hoặc không tìm thấy flashcard.
     */
    @Transactional
    public void deleteUserFlashcard(Integer userFlashcardId) {
        if (userFlashcardId == null) {
            throw new IllegalArgumentException("User Flashcard ID không được để trống.");
        }
        UserFlashcard flashcard = userFlashcardRepository.findById(userFlashcardId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy flashcard người dùng với ID: " + userFlashcardId));
        userFlashcardRepository.delete(flashcard);
    }

    // --- Private mapping methods ---

    /**
     * Ánh xạ một Entity UserFlashcard sang FlashcardResponse DTO.
     *
     * @param userFlashcard Entity UserFlashcard.
     * @return FlashcardResponse DTO.
     */
    private FlashcardResponse mapToFlashcardResponse(UserFlashcard userFlashcard) {
        if (userFlashcard == null || userFlashcard.getVocabulary() == null || userFlashcard.getUser() == null) {
            return null; // Hoặc throw exception nếu dữ liệu không hợp lệ
        }
        Vocabulary vocab = userFlashcard.getVocabulary();
        return new FlashcardResponse(
                userFlashcard.getId(), // Sử dụng ID của UserFlashcard
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