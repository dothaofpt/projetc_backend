package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.QuizRequest;
import org.example.projetc_backend.dto.QuizResponse;
import org.example.projetc_backend.dto.QuizSearchRequest;
import org.example.projetc_backend.dto.QuizPageResponse;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class QuizService {
    private static final Logger logger = LoggerFactory.getLogger(QuizService.class);
    private final QuizRepository quizRepository;
    private final LessonRepository lessonRepository;

    public QuizService(QuizRepository quizRepository, LessonRepository lessonRepository) {
        this.quizRepository = quizRepository;
        this.lessonRepository = lessonRepository;
    }

    /**
     * Tạo một bài kiểm tra (Quiz) mới dựa trên dữ liệu từ QuizRequest.
     * Kiểm tra tính hợp lệ của request, sự tồn tại của bài học (Lesson),
     * và trùng lặp tiêu đề trước khi lưu.
     * @param request Dữ liệu của bài kiểm tra cần tạo.
     * @return QuizResponse chứa thông tin của bài kiểm tra đã tạo.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ hoặc không tìm thấy Lesson.
     */
    public QuizResponse createQuiz(QuizRequest request) {
        if (request == null || request.lessonId() == null || request.title() == null || request.quizType() == null) {
            throw new IllegalArgumentException("Lesson ID, tiêu đề và loại bài kiểm tra là bắt buộc.");
        }

        logger.info("Đang xử lý yêu cầu tạo Quiz với loại: {}", request.quizType());

        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        quizRepository.findByTitle(request.title().trim())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài kiểm tra '" + request.title() + "' đã tồn tại.");
                });

        Quiz quiz = new Quiz();
        quiz.setLesson(lesson);
        quiz.setTitle(request.title().trim());
        quiz.setQuizType(request.quizType());
        quiz.setCreatedAt(LocalDateTime.now());

        quiz = quizRepository.save(quiz);

        return mapToQuizResponse(quiz);
    }

    /**
     * Lấy thông tin bài kiểm tra theo ID.
     * @param quizId ID của bài kiểm tra.
     * @return QuizResponse chứa thông tin bài kiểm tra.
     * @throws IllegalArgumentException nếu quizId trống hoặc không tìm thấy bài kiểm tra.
     */
    @Transactional(readOnly = true)
    public QuizResponse getQuizById(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống.");
        }
        // Đảm bảo Lesson được fetch cùng để lấy tên
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId));
        return mapToQuizResponse(quiz);
    }

    /**
     * Lấy danh sách các bài kiểm tra theo ID bài học (Lesson ID).
     * @param lessonId ID của bài học.
     * @return Danh sách QuizResponse của các bài kiểm tra thuộc bài học đó.
     * @throws IllegalArgumentException nếu lessonId trống hoặc không tìm thấy Lesson.
     */
    @Transactional(readOnly = true)
    public List<QuizResponse> getQuizzesByLessonId(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId);
        }
        // Giả sử findByLessonLessonId đã fetch Lesson hoặc Lesson được tải qua @ManyToOne mặc định
        return quizRepository.findByLessonLessonId(lessonId).stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả các bài kiểm tra hiện có trong hệ thống.
     * @return Danh sách QuizResponse của tất cả các bài kiểm tra.
     */
    @Transactional(readOnly = true)
    public List<QuizResponse> getAllQuizzes() {
        logger.info("Đang lấy tất cả quizzes.");
        // Đảm bảo Lesson được fetch cùng để lấy tên
        return quizRepository.findAll().stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm và phân trang các bài kiểm tra.
     *
     * @param request DTO chứa các tiêu chí tìm kiếm (lessonId, title, quizType) và thông tin phân trang/sắp xếp.
     * @return Trang các QuizResponse.
     * @throws IllegalArgumentException Nếu Search request trống.
     */
    @Transactional(readOnly = true)
    public QuizPageResponse searchQuizzes(QuizSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, request.sortBy());
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        // Đây là nơi cần đảm bảo Lesson được JOIN FETCH nếu nó là LAZY
        // Ví dụ: Tạo một phương thức searchWithLesson trong QuizRepository và gọi ở đây
        Page<Quiz> quizPage = quizRepository.searchQuizzes(
                request.lessonId(),
                request.title(),
                request.quizType(),
                pageable
        );

        List<QuizResponse> content = quizPage.getContent().stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());

        return new QuizPageResponse(
                content,
                quizPage.getTotalElements(),
                quizPage.getTotalPages(),
                quizPage.getNumber(),
                quizPage.getSize()
        );
    }

    /**
     * Cập nhật thông tin của một bài kiểm tra hiện có.
     * @param quizId ID của bài kiểm tra cần cập nhật.
     * @param request Dữ liệu mới để cập nhật bài kiểm tra.
     * @return QuizResponse chứa thông tin bài kiểm tra đã cập nhật.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ, không tìm thấy Quiz/Lesson, hoặc tiêu đề trùng lặp.
     */
    @Transactional
    public QuizResponse updateQuiz(Integer quizId, QuizRequest request) {
        if (quizId == null || request == null || request.lessonId() == null || request.title() == null || request.quizType() == null) {
            throw new IllegalArgumentException("Quiz ID, Lesson ID, tiêu đề và loại bài kiểm tra là bắt buộc.");
        }

        logger.info("Đang cập nhật Quiz với ID: {}, loại: {}", quizId, request.quizType());

        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId));

        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        quizRepository.findByTitle(request.title().trim())
                .filter(existing -> !existing.getQuizId().equals(quizId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài kiểm tra '" + request.title() + "' đã tồn tại.");
                });

        quiz.setLesson(lesson);
        quiz.setTitle(request.title().trim());
        quiz.setQuizType(request.quizType());

        quiz = quizRepository.save(quiz);

        return mapToQuizResponse(quiz);
    }

    /**
     * Xóa một bài kiểm tra khỏi cơ sở dữ liệu.
     * Khi xóa Quiz, các Question và QuizResult liên quan (nếu có cascade cấu hình đúng) cũng sẽ bị ảnh hưởng.
     * @param quizId ID của bài kiểm tra cần xóa.
     * @throws IllegalArgumentException nếu quizId trống hoặc không tìm thấy bài kiểm tra.
     */
    @Transactional
    public void deleteQuiz(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống.");
        }
        if (!quizRepository.existsById(quizId)) {
            throw new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId);
        }
        quizRepository.deleteById(quizId);
    }

    /**
     * Phương thức trợ giúp để ánh xạ đối tượng Quiz entity sang QuizResponse DTO.
     * @param quiz Đối tượng Quiz entity.
     * @return Đối tượng QuizResponse DTO tương ứng.
     */
    private QuizResponse mapToQuizResponse(Quiz quiz) {
        // Lấy tên bài học từ Lesson entity liên kết.
        // Đảm bảo Lesson đã được tải (fetched) trước khi truy cập quiz.getLesson().getTitle()
        String lessonTitle = null;
        if (quiz.getLesson() != null) {
            // Tránh LazyInitializationException nếu Lesson được fetch LAZY
            // Đây là điểm quan trọng: nếu Lesson là LAZY, bạn cần đảm bảo nó đã được tải.
            // Ví dụ, nếu bạn dùng findAll(), findByLessonLessonId(), searchQuizzes()
            // thì phải JOIN FETCH lesson trong truy vấn hoặc đặt @ManyToOne là EAGER (không khuyến nghị EAGER)
            try {
                lessonTitle = quiz.getLesson().getTitle();
            } catch (Exception e) {
                logger.error("Could not fetch lesson title for quizId {}: {}", quiz.getQuizId(), e.getMessage());
                // Xử lý lỗi, có thể trả về null hoặc một thông báo mặc định
            }
        }

        return new QuizResponse(
                quiz.getQuizId(),
                quiz.getLesson() != null ? quiz.getLesson().getLessonId() : null,
                quiz.getTitle(),
                quiz.getQuizType(),
                quiz.getCreatedAt(),
                lessonTitle // <-- Truyền tên bài học vào DTO
        );
    }
}