package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.QuestionRequest;
import org.example.projetc_backend.dto.QuestionResponse;
import org.example.projetc_backend.dto.QuestionSearchRequest;
import org.example.projetc_backend.dto.QuestionPageResponse;
import org.example.projetc_backend.dto.AnswerResponse; // MỚI: Import AnswerResponse
import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.repository.QuestionRepository;
import org.example.projetc_backend.repository.QuizRepository;
import org.example.projetc_backend.repository.AnswerRepository; // MỚI: Import AnswerRepository
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;
    private final AnswerRepository answerRepository; // MỚI: Khai báo AnswerRepository

    // Cập nhật constructor để tiêm AnswerRepository
    public QuestionService(QuestionRepository questionRepository, QuizRepository quizRepository, AnswerRepository answerRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
        this.answerRepository = answerRepository; // Tiêm AnswerRepository
    }

    /**
     * Tạo một câu hỏi mới.
     *
     * @param request DTO chứa thông tin câu hỏi.
     * @return QuestionResponse của câu hỏi đã tạo.
     * @throws IllegalArgumentException Nếu dữ liệu request không hợp lệ hoặc không tìm thấy Quiz.
     */
    @Transactional
    public QuestionResponse createQuestion(QuestionRequest request) {
        if (request == null || request.quizId() == null || request.questionText() == null || request.questionType() == null) {
            throw new IllegalArgumentException("Quiz ID, question text và question type là bắt buộc.");
        }

        logger.info("Processing QuestionRequest for Quiz ID: {}, Type: {}", request.quizId(), request.questionType());

        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + request.quizId()));

        Question question = new Question();
        question.setQuiz(quiz);
        question.setQuestionText(request.questionText().trim());
        question.setQuestionType(request.questionType());

        question.setAudioUrl(request.audioUrl() != null ? request.audioUrl().trim() : null);
        question.setImageUrl(request.imageUrl() != null ? request.imageUrl().trim() : null);
        question.setCorrectAnswerText(request.correctAnswerText() != null ? request.correctAnswerText().trim() : null);

        question = questionRepository.save(question);

        return mapToQuestionResponse(question); // Vẫn dùng mapToQuestionResponse để trả về DTO đầy đủ
    }

    /**
     * Lấy thông tin chi tiết của một câu hỏi theo ID.
     *
     * @param questionId ID của câu hỏi.
     * @return QuestionResponse của câu hỏi.
     * @throws IllegalArgumentException Nếu Question ID trống hoặc không tìm thấy câu hỏi.
     */
    @Transactional(readOnly = true)
    public QuestionResponse getQuestionById(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống.");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId));

        return mapToQuestionResponse(question);
    }

    /**
     * Lấy danh sách các câu hỏi thuộc một bài kiểm tra cụ thể.
     *
     * @param quizId ID của bài kiểm tra.
     * @return Danh sách QuestionResponse của các câu hỏi thuộc quiz đó.
     * @throws IllegalArgumentException Nếu Quiz ID trống hoặc không tìm thấy bài kiểm tra.
     */
    @Transactional(readOnly = true)
    public List<QuestionResponse> getQuestionsByQuizId(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống.");
        }
        if (!quizRepository.existsById(quizId)) {
            throw new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId);
        }

        return questionRepository.findByQuizQuizId(quizId).stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }


    /**
     * Tìm kiếm và phân trang câu hỏi dựa trên các tiêu chí tùy chọn.
     *
     * @param request DTO chứa các tiêu chí tìm kiếm (quizId, questionText, questionType) và thông tin phân trang/sắp xếp.
     * @return Trang các QuestionResponse phù hợp với tiêu chí tìm kiếm.
     * @throws IllegalArgumentException Nếu Search request trống.
     */
    @Transactional(readOnly = true)
    public QuestionPageResponse searchQuestions(QuestionSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        // SỬA LỖI: Sử dụng accessor methods cho các trường của Record DTO
        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, request.sortBy());
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<Question> questionPage = questionRepository.searchQuestions(
                request.quizId(),
                request.questionText(),
                request.questionType(),
                pageable
        );

        List<QuestionResponse> content = questionPage.getContent().stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());

        return new QuestionPageResponse(
                content,
                questionPage.getTotalElements(),
                questionPage.getTotalPages(),
                questionPage.getNumber(),
                questionPage.getSize()
        );
    }


    /**
     * Cập nhật thông tin của một câu hỏi hiện có.
     *
     * @param questionId ID của câu hỏi cần cập nhật.
     * @param request DTO chứa thông tin cập nhật.
     * @return QuestionResponse của câu hỏi đã cập nhật.
     * @throws IllegalArgumentException Nếu dữ liệu request không hợp lệ, câu hỏi/quiz không tồn tại.
     */
    @Transactional
    public QuestionResponse updateQuestion(Integer questionId, QuestionRequest request) {
        if (questionId == null || request == null || request.quizId() == null || request.questionText() == null || request.questionType() == null) {
            throw new IllegalArgumentException("Question ID, Quiz ID, question text và question type là bắt buộc.");
        }

        logger.info("Updating Question with ID: {}, Quiz ID: {}, Type: {}", questionId, request.quizId(), request.questionType());

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId));

        Quiz newQuiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + request.quizId()));

        question.setQuiz(newQuiz);
        question.setQuestionText(request.questionText().trim());
        question.setQuestionType(request.questionType());

        question.setAudioUrl(request.audioUrl() != null ? request.audioUrl().trim() : null);
        question.setImageUrl(request.imageUrl() != null ? request.imageUrl().trim() : null);
        question.setCorrectAnswerText(request.correctAnswerText() != null ? request.correctAnswerText().trim() : null);

        question = questionRepository.save(question);

        return mapToQuestionResponse(question);
    }

    /**
     * Xóa một câu hỏi khỏi cơ sở dữ liệu.
     *
     * @param questionId ID của câu hỏi cần xóa.
     * @throws IllegalArgumentException Nếu Question ID trống hoặc không tìm thấy câu hỏi.
     */
    @Transactional
    public void deleteQuestion(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống.");
        }

        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId);
        }

        questionRepository.deleteById(questionId);
    }

    /**
     * Phương thức trợ giúp để ánh xạ đối tượng Question entity sang QuestionResponse DTO.
     * Bao gồm việc tải và ánh xạ các Answer liên quan.
     *
     * @param question Đối tượng Question entity.
     * @return Đối tượng QuestionResponse DTO tương ứng.
     */
    private QuestionResponse mapToQuestionResponse(Question question) {
        // MỚI: Lấy tất cả các Answer (lựa chọn) cho câu hỏi này
        // Lấy các câu trả lời chưa bị xóa mềm và đang hoạt động (isActive = true)
        // để hiển thị cho người dùng cuối cùng (multiple choice options)
        List<AnswerResponse> answers = answerRepository
                .findByQuestionQuestionIdAndIsActiveTrue(question.getQuestionId()) // Có thể dùng findByQuestionQuestionIdAndIsActiveTrueAndIsDeletedFalse nếu muốn chặt hơn
                .stream()
                .map(ans -> new AnswerResponse(
                        ans.getAnswerId(),
                        ans.getQuestion().getQuestionId(),
                        ans.getAnswerText(),
                        ans.isCorrect(),
                        ans.isActive(),
                        ans.isDeleted()
                ))
                .collect(Collectors.toList());

        return new QuestionResponse(
                question.getQuestionId(),
                question.getQuiz().getQuizId(),
                question.getQuestionText(),
                question.getQuestionType(),
                question.getAudioUrl(),
                question.getImageUrl(),
                question.getCorrectAnswerText(),
                answers // MỚI: Truyền danh sách answers vào DTO
        );
    }
}