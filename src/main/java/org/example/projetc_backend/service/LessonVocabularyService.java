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
import org.springframework.transaction.annotation.Transactional;

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

    /**
     * Tạo một liên kết mới giữa một bài học và một từ vựng.
     * Đảm bảo rằng bài học và từ vựng tồn tại, và liên kết này chưa được tạo trước đó.
     *
     * @param request DTO chứa lessonId và wordId.
     * @return LessonVocabularyResponse của liên kết đã tạo.
     * @throws IllegalArgumentException Nếu dữ liệu request không hợp lệ, bài học/từ vựng không tồn tại, hoặc liên kết đã tồn tại.
     */
    @Transactional
    public LessonVocabularyResponse createLessonVocabulary(LessonVocabularyRequest request) {
        // 1. Xác thực đầu vào cơ bản
        if (request == null || request.lessonId() == null || request.wordId() == null) {
            throw new IllegalArgumentException("Lesson ID và Word ID là bắt buộc.");
        }

        // 2. Kiểm tra sự tồn tại của Lesson và Vocabulary
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        Vocabulary vocabulary = vocabularyRepository.findById(request.wordId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + request.wordId()));

        // 3. Kiểm tra xem liên kết đã tồn tại chưa để tránh trùng lặp
        if (lessonVocabularyRepository.existsByIdLessonIdAndIdWordId(request.lessonId(), request.wordId())) {
            throw new IllegalArgumentException("Liên kết giữa bài học ID " + request.lessonId() + " và từ vựng ID " + request.wordId() + " đã tồn tại.");
        }

        // 4. Tạo đối tượng LessonVocabulary
        LessonVocabulary lessonVocabulary = new LessonVocabulary();
        // Tạo EmbeddedId từ lessonId và wordId
        lessonVocabulary.setId(new LessonVocabularyId(request.lessonId(), request.wordId()));
        // Gán các đối tượng Lesson và Vocabulary để JPA quản lý mối quan hệ
        lessonVocabulary.setLesson(lesson);
        lessonVocabulary.setVocabulary(vocabulary);

        // 5. Lưu liên kết vào cơ sở dữ liệu
        lessonVocabulary = lessonVocabularyRepository.save(lessonVocabulary);

        // 6. Trả về Response DTO
        return mapToLessonVocabularyResponse(lessonVocabulary);
    }

    /**
     * Lấy tất cả các từ vựng (dưới dạng liên kết) của một bài học cụ thể.
     *
     * @param lessonId ID của bài học.
     * @return Danh sách LessonVocabularyResponse chứa các cặp lessonId-wordId.
     * @throws IllegalArgumentException Nếu Lesson ID trống hoặc bài học không tồn tại.
     */
    @Transactional(readOnly = true) // Chỉ đọc
    public List<LessonVocabularyResponse> getLessonVocabulariesByLessonId(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        // Kiểm tra xem Lesson có tồn tại không trước khi truy vấn các liên kết
        // Điều này giúp tránh trả về danh sách rỗng mà không biết rằng LessonId không hợp lệ.
        lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));

        // Lấy tất cả các liên kết từ Repository
        return lessonVocabularyRepository.findByIdLessonId(lessonId).stream()
                .map(this::mapToLessonVocabularyResponse) // Sử dụng phương thức ánh xạ
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả các bài học (dưới dạng liên kết) mà một từ vựng cụ thể thuộc về.
     *
     * @param wordId ID của từ vựng.
     * @return Danh sách LessonVocabularyResponse chứa các cặp lessonId-wordId.
     * @throws IllegalArgumentException Nếu Word ID trống hoặc từ vựng không tồn tại.
     */
    @Transactional(readOnly = true)
    public List<LessonVocabularyResponse> getLessonVocabulariesByWordId(Integer wordId) {
        if (wordId == null) {
            throw new IllegalArgumentException("Word ID không được để trống.");
        }
        // Kiểm tra xem Vocabulary có tồn tại không
        vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId));

        return lessonVocabularyRepository.findByIdWordId(wordId).stream()
                .map(this::mapToLessonVocabularyResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xóa một liên kết cụ thể giữa một bài học và một từ vựng.
     *
     * @param lessonId ID của bài học.
     * @param wordId ID của từ vựng.
     * @throws IllegalArgumentException Nếu Lesson ID/Word ID trống hoặc liên kết không tồn tại.
     */
    @Transactional // Đảm bảo giao dịch
    public void deleteLessonVocabulary(Integer lessonId, Integer wordId) {
        if (lessonId == null || wordId == null) {
            throw new IllegalArgumentException("Lesson ID và Word ID không được để trống.");
        }

        // Tạo khóa nhúng để kiểm tra và xóa
        LessonVocabularyId id = new LessonVocabularyId(lessonId, wordId);

        // Kiểm tra sự tồn tại của liên kết trước khi xóa để đưa ra lỗi rõ ràng hơn
        if (!lessonVocabularyRepository.existsById(id)) {
            throw new IllegalArgumentException("Không tìm thấy liên kết giữa bài học ID " + lessonId + " và từ vựng ID " + wordId + ".");
        }

        lessonVocabularyRepository.deleteById(id);
    }

    /**
     * Phương thức ánh xạ từ LessonVocabulary Entity sang LessonVocabularyResponse DTO.
     * Giúp tái sử dụng logic ánh xạ.
     *
     * @param lessonVocabulary Entity LessonVocabulary.
     * @return LessonVocabularyResponse DTO.
     */
    private LessonVocabularyResponse mapToLessonVocabularyResponse(LessonVocabulary lessonVocabulary) {
        if (lessonVocabulary == null) {
            return null;
        }
        return new LessonVocabularyResponse(
                lessonVocabulary.getId().getLessonId(),
                lessonVocabulary.getId().getWordId()
        );
    }
}