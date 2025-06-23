package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.QuestionRequest;
import org.example.projetc_backend.dto.QuestionResponse;
import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.repository.QuestionRepository;
import org.example.projetc_backend.repository.QuizRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.data.jpa.domain.Specification; // Import này là quan trọng
import org.springframework.stereotype.Service;

import jakarta.persistence.criteria.Predicate; // Import này là quan trọng

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuestionService {
    private static final Logger logger = LoggerFactory.getLogger(QuestionService.class);
    private final QuestionRepository questionRepository;
    private final QuizRepository quizRepository;

    public QuestionService(QuestionRepository questionRepository, QuizRepository quizRepository) {
        this.questionRepository = questionRepository;
        this.quizRepository = quizRepository;
    }

    public QuestionResponse createQuestion(QuestionRequest request) {
        if (request == null || request.quizId() == null || request.questionText() == null || request.skill() == null) {
            throw new IllegalArgumentException("Request, quizId, questionText, hoặc skill không được để trống");
        }

        logger.info("Processing QuestionRequest with skill: {}", request.skill());
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + request.quizId()));

        Question question = new Question();
        question.setQuiz(quiz);
        question.setQuestionText(request.questionText());
        try {
            question.setSkill(Quiz.Skill.valueOf(request.skill()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Skill không hợp lệ: " + request.skill());
        }
        question = questionRepository.save(question);

        return mapToQuestionResponse(question);
    }

    public QuestionResponse getQuestionById(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống");
        }

        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId));

        return mapToQuestionResponse(question);
    }

    public List<QuestionResponse> getQuestionsByQuizId(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống");
        }

        return questionRepository.findByQuizQuizId(quizId).stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm câu hỏi dựa trên các tiêu chí tùy chọn.
     * @param quizId ID của bài kiểm tra (tùy chọn)
     * @param skill Kỹ năng (LISTENING, SPEAKING, v.v.) (tùy chọn)
     * @param questionText Chuỗi văn bản câu hỏi để tìm kiếm (tùy chọn, tìm kiếm không phân biệt chữ hoa/thường)
     * @return Danh sách các QuestionResponse phù hợp với tiêu chí tìm kiếm.
     */
    public List<QuestionResponse> searchQuestions(Integer quizId, String skill, String questionText) {
        // Tạo một Specification động để xây dựng truy vấn.
        Specification<Question> spec = (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Thêm điều kiện lọc theo quizId nếu được cung cấp
            if (quizId != null) {
                predicates.add(criteriaBuilder.equal(root.get("quiz").get("quizId"), quizId));
            }

            // Thêm điều kiện lọc theo skill nếu được cung cấp
            if (skill != null && !skill.trim().isEmpty()) {
                try {
                    // Chuyển đổi chuỗi skill thành enum Quiz.Skill
                    Quiz.Skill enumSkill = Quiz.Skill.valueOf(skill.toUpperCase());
                    predicates.add(criteriaBuilder.equal(root.get("skill"), enumSkill));
                } catch (IllegalArgumentException e) {
                    logger.warn("Skill không hợp lệ trong yêu cầu tìm kiếm: {}", skill);
                    // Có thể chọn ném ngoại lệ hoặc bỏ qua điều kiện lọc skill nếu skill không hợp lệ
                    // Trong trường hợp này, chúng ta sẽ bỏ qua và không thêm điều kiện lọc skill
                }
            }

            // Thêm điều kiện lọc theo questionText nếu được cung cấp
            if (questionText != null && !questionText.trim().isEmpty()) {
                // Tìm kiếm không phân biệt chữ hoa/thường và theo chuỗi con
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("questionText")), "%" + questionText.toLowerCase() + "%"));
            }

            // Kết hợp tất cả các điều kiện bằng toán tử AND
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };

        // Thực hiện tìm kiếm bằng repository với Specification đã tạo
        return questionRepository.findAll(spec).stream()
                .map(this::mapToQuestionResponse)
                .collect(Collectors.toList());
    }

    public QuestionResponse updateQuestion(Integer questionId, QuestionRequest request) {
        if (questionId == null || request == null || request.quizId() == null || request.questionText() == null || request.skill() == null) {
            throw new IllegalArgumentException("Question ID, request, quizId, questionText, hoặc skill không được để trống");
        }

        logger.info("Updating Question with ID: {}, skill: {}", questionId, request.skill());
        Question question = questionRepository.findById(questionId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId));

        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + request.quizId()));

        question.setQuiz(quiz);
        question.setQuestionText(request.questionText());
        try {
            question.setSkill(Quiz.Skill.valueOf(request.skill()));
        } catch (IllegalArgumentException e) {
            throw new IllegalArgumentException("Skill không hợp lệ: " + request.skill());
        }
        question = questionRepository.save(question);

        return mapToQuestionResponse(question);
    }

    public void deleteQuestion(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống");
        }

        if (!questionRepository.existsById(questionId)) {
            throw new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + questionId);
        }

        questionRepository.deleteById(questionId);
    }

    private QuestionResponse mapToQuestionResponse(Question question) {
        return new QuestionResponse(
                question.getQuestionId(),
                question.getQuiz().getQuizId(),
                question.getQuestionText(),
                question.getSkill().toString()
        );
    }
}