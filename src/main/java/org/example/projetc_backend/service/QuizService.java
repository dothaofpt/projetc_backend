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

    @Transactional // Nên thêm @Transactional cho các thao tác ghi dữ liệu (create, update, delete)
    public QuizResponse createQuiz(QuizRequest request) {
        if (request == null || request.lessonId() == null || request.title() == null || request.skill() == null) {
            throw new IllegalArgumentException("Request, lessonId, title, hoặc skill không được để trống");
        }

        logger.info("Processing QuizRequest with skill: {}", request.skill());

        // Cần kiểm tra xem lessonId có tồn tại hay không
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        // Kiểm tra trùng lặp tiêu đề
        quizRepository.findByTitle(request.title())
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài kiểm tra đã tồn tại: " + request.title());
                });

        Quiz quiz = new Quiz();
        quiz.setLesson(lesson);
        quiz.setTitle(request.title());

        try {
            // Đảm bảo Quiz.Skill là enum và Skill trong request là String tương ứng
            quiz.setSkill(Quiz.Skill.valueOf(request.skill()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Skill không hợp lệ: " + request.skill() + ". Lỗi: " + e.getMessage());
        }

        quiz.setCreatedAt(LocalDateTime.now()); // Thêm dòng này nếu Quiz entity có trường createdAt

        quiz = quizRepository.save(quiz);
        return mapToQuizResponse(quiz);
    }

    public QuizResponse getQuizById(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống");
        }
        //findById của JpaRepository nhận Long, cần cast nếu quizId của entity là Long.
        //Dựa vào code của bạn, có vẻ Quiz entity có ID là Integer, nên giữ nguyên.
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId));
        return mapToQuizResponse(quiz);
    }

    public List<QuizResponse> getQuizzesByLessonId(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống");
        }
        //findByLessonLessonId cũng cần khớp kiểu ID của Lesson entity.
        return quizRepository.findByLessonLessonId(lessonId).stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    // PHƯƠNG THỨC MỚI CẦN THÊM VÀO ĐÂY ĐỂ XỬ LÝ GET TẤT CẢ QUIZ
    public List<QuizResponse> getAllQuizzes() {
        logger.info("Fetching all quizzes.");
        return quizRepository.findAll().stream()
                .map(this::mapToQuizResponse)
                .collect(Collectors.toList());
    }

    @Transactional // Nên thêm @Transactional
    public QuizResponse updateQuiz(Integer quizId, QuizRequest request) {
        if (quizId == null || request == null || request.lessonId() == null || request.title() == null || request.skill() == null) {
            throw new IllegalArgumentException("Quiz ID, request, lessonId, title, hoặc skill không được để trống");
        }

        logger.info("Updating Quiz with ID: {}, skill: {}", quizId, request.skill());

        // Tìm quiz hiện có
        Quiz quiz = quizRepository.findById(quizId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId));

        // Tìm lesson mới (nếu có thay đổi)
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        // Kiểm tra trùng lặp tiêu đề, nhưng cho phép tiêu đề hiện tại của chính quiz đó
        quizRepository.findByTitle(request.title())
                .filter(existing -> !existing.getQuizId().equals(quizId)) // Bỏ qua chính quiz đang cập nhật
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("Tiêu đề bài kiểm tra đã tồn tại: " + request.title());
                });

        quiz.setLesson(lesson);
        quiz.setTitle(request.title());
        try {
            quiz.setSkill(Quiz.Skill.valueOf(request.skill()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Skill không hợp lệ: " + request.skill() + ". Lỗi: " + e.getMessage());
        }
        // Không cập nhật createdAt khi update

        quiz = quizRepository.save(quiz);
        return mapToQuizResponse(quiz);
    }

    @Transactional // Nên thêm @Transactional
    public void deleteQuiz(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống");
        }
        if (!quizRepository.existsById(quizId)) {
            throw new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId);
        }
        quizRepository.deleteById(quizId);
    }

    // Helper method để ánh xạ Quiz entity sang QuizResponse DTO
    private QuizResponse mapToQuizResponse(Quiz quiz) {
        // Đảm bảo QuizResponse record/class có constructor phù hợp
        // và các getter của Quiz entity trả về đúng kiểu dữ liệu.
        return new QuizResponse(
                quiz.getQuizId(), // Giả định quizId là Integer
                quiz.getLesson() != null ? quiz.getLesson().getLessonId() : null, // Lấy Lesson ID
                quiz.getTitle(),
                quiz.getSkill().toString(), // Chuyển Enum sang String
                quiz.getCreatedAt() // Bao gồm createdAt nếu có
        );
    }
}