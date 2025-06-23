package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.QuizRequest;
import org.example.projetc_backend.dto.QuizResponse;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Thêm import này cho @Transactional

import java.time.LocalDateTime; // Đảm bảo import này nếu Quiz có trường createdAt
import java.util.List;
import java.util.stream.Collectors;

@Service
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
    @Transactional // Đảm bảo toàn vẹn dữ liệu cho thao tác ghi
    public QuizResponse createQuiz(QuizRequest request) {
        // 1. Kiểm tra tính hợp lệ cơ bản của request
        if (request == null || request.lessonId() == null || request.title() == null || request.skill() == null) {
            throw new IllegalArgumentException("Request, lessonId, title, hoặc skill không được để trống.");
        }

        logger.info("Processing createQuiz request for skill: {}", request.skill());

        // 2. Tìm bài học (Lesson) liên quan. Nếu không tìm thấy, ném ngoại lệ.
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        // 3. Kiểm tra trùng lặp tiêu đề bài kiểm tra
        quizRepository.findByTitle(request.title())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài kiểm tra đã tồn tại: " + request.title());
                });

        // 4. Tạo đối tượng Quiz và gán các giá trị từ request
        Quiz quiz = new Quiz();
        quiz.setLesson(lesson);
        quiz.setTitle(request.title());

        // 5. Chuyển đổi và gán Skill (Enum)
        try {
            quiz.setSkill(Quiz.Skill.valueOf(request.skill()));
        } catch (IllegalArgumentException e) {
            // Xử lý khi skill không hợp lệ (ví dụ: chuỗi không khớp với enum nào)
            throw new IllegalArgumentException("Skill không hợp lệ: '" + request.skill() + "'. Vui lòng kiểm tra lại. Chi tiết lỗi: " + e.getMessage());
        }

        // 6. Gán thời gian tạo. Đảm bảo Quiz entity có trường 'createdAt' và kiểu LocalDateTime.
        quiz.setCreatedAt(LocalDateTime.now());

        // 7. Lưu Quiz vào cơ sở dữ liệu
        quiz = quizRepository.save(quiz);

        // 8. Ánh xạ Quiz entity sang QuizResponse DTO và trả về
        return mapToQuizResponse(quiz);
    }

    /**
     * Lấy thông tin bài kiểm tra theo ID.
     * @param quizId ID của bài kiểm tra.
     * @return QuizResponse chứa thông tin bài kiểm tra.
     * @throws IllegalArgumentException nếu quizId trống hoặc không tìm thấy bài kiểm tra.
     */
    public QuizResponse getQuizById(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống.");
        }
        // findById của JpaRepository nhận kiểu ID của entity.
        // Dựa vào code của bạn, có vẻ Quiz entity có ID là Integer, nên giữ nguyên.
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId));
        return mapToQuizResponse(quiz);
    }

    /**
     * Lấy danh sách các bài kiểm tra theo ID bài học (Lesson ID).
     * @param lessonId ID của bài học.
     * @return Danh sách QuizResponse của các bài kiểm tra thuộc bài học đó.
     * @throws IllegalArgumentException nếu lessonId trống.
     */
    public List<QuizResponse> getQuizzesByLessonId(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        // findByLessonLessonId cần khớp với tên phương thức trong QuizRepository
        // và kiểu ID của Lesson entity.
        return quizRepository.findByLessonLessonId(lessonId).stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả các bài kiểm tra hiện có trong hệ thống.
     * @return Danh sách QuizResponse của tất cả các bài kiểm tra.
     */
    public List<QuizResponse> getAllQuizzes() {
        logger.info("Fetching all quizzes.");
        return quizRepository.findAll().stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin của một bài kiểm tra hiện có.
     * @param quizId ID của bài kiểm tra cần cập nhật.
     * @param request Dữ liệu mới để cập nhật bài kiểm tra.
     * @return QuizResponse chứa thông tin bài kiểm tra đã cập nhật.
     * @throws IllegalArgumentException nếu dữ liệu không hợp lệ, không tìm thấy Quiz/Lesson, hoặc tiêu đề trùng lặp.
     */
    @Transactional // Đảm bảo toàn vẹn dữ liệu cho thao tác ghi
    public QuizResponse updateQuiz(Integer quizId, QuizRequest request) {
        // 1. Kiểm tra tính hợp lệ cơ bản của request và quizId
        if (quizId == null || request == null || request.lessonId() == null || request.title() == null || request.skill() == null) {
            throw new IllegalArgumentException("Quiz ID, request, lessonId, title, hoặc skill không được để trống.");
        }

        logger.info("Updating Quiz with ID: {}, skill: {}", quizId, request.skill());

        // 2. Tìm quiz hiện có. Nếu không tìm thấy, ném ngoại lệ.
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId));

        // 3. Tìm lesson mới (nếu có thay đổi hoặc muốn xác nhận lessonId hợp lệ).
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        // 4. Kiểm tra trùng lặp tiêu đề, nhưng cho phép chính quiz đang cập nhật
        quizRepository.findByTitle(request.title())
                .filter(existing -> !existing.getQuizId().equals(quizId)) // Bỏ qua nếu tiêu đề thuộc về chính quiz đang cập nhật
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài kiểm tra đã tồn tại: " + request.title());
                });

        // 5. Cập nhật các trường thông tin của Quiz
        quiz.setLesson(lesson);
        quiz.setTitle(request.title());

        // 6. Chuyển đổi và gán Skill (Enum)
        try {
            quiz.setSkill(Quiz.Skill.valueOf(request.skill()));
        } catch (IllegalArgumentException e) {
            // Xử lý khi skill không hợp lệ
            throw new IllegalArgumentException("Skill không hợp lệ: '" + request.skill() + "'. Vui lòng kiểm tra lại. Chi tiết lỗi: " + e.getMessage());
        }

        // Không cập nhật createdAt khi update (nếu bạn muốn createdAt chỉ được set 1 lần lúc tạo)

        // 7. Lưu Quiz đã cập nhật vào cơ sở dữ liệu
        quiz = quizRepository.save(quiz);

        // 8. Ánh xạ Quiz entity sang QuizResponse DTO và trả về
        return mapToQuizResponse(quiz);
    }

    /**
     * Xóa một bài kiểm tra khỏi cơ sở dữ liệu.
     * @param quizId ID của bài kiểm tra cần xóa.
     * @throws IllegalArgumentException nếu quizId trống hoặc không tìm thấy bài kiểm tra.
     */
    @Transactional // Đảm bảo toàn vẹn dữ liệu cho thao tác xóa
    public void deleteQuiz(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống.");
        }
        // Kiểm tra sự tồn tại trước khi xóa để ném ngoại lệ rõ ràng hơn
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
        // Đảm bảo QuizResponse record/class có constructor phù hợp
        // và các getter của Quiz entity trả về đúng kiểu dữ liệu.
        return new QuizResponse(
                quiz.getQuizId(),
                // Kiểm tra null cho getLesson() để tránh NullPointerException nếu lesson chưa được gán
                quiz.getLesson() != null ? quiz.getLesson().getLessonId() : null,
                quiz.getTitle(),
                quiz.getSkill().toString(), // Chuyển Enum sang String
                quiz.getCreatedAt() // Bao gồm createdAt nếu có
        );
    }
}