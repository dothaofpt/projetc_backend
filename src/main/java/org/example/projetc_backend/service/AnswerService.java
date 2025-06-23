package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.AnswerRequest;
import org.example.projetc_backend.dto.AnswerResponse;
import org.example.projetc_backend.dto.AnswerSearchRequest;
import org.example.projetc_backend.entity.Answer;
import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.repository.AnswerRepository;
import org.example.projetc_backend.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    public AnswerResponse createAnswer(AnswerRequest request) {
        if (request == null || request.questionId() == null) {
            throw new IllegalArgumentException("AnswerRequest hoặc questionId không được để trống");
        }
        if (request.answerText() == null || request.answerText().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung câu trả lời không được để trống hoặc chỉ chứa khoảng trắng");
        }

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + request.questionId()));

        if (Boolean.TRUE.equals(request.isCorrect())) {
            long correctAnswersCount = answerRepository.findByQuestionQuestionIdAndIsActiveTrue(request.questionId())
                    .stream().filter(Answer::isCorrect).count();
            if (correctAnswersCount >= 1) {
                throw new IllegalArgumentException("Một câu hỏi chỉ được có một câu trả lời đúng (trong số các câu trả lời đang hoạt động)");
            }
        }

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAnswerText(request.answerText());
        answer.setCorrect(Boolean.TRUE.equals(request.isCorrect()));
        answer.setActive(false);
        answer.setDeleted(false);
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    public AnswerResponse getAnswerById(Integer answerId) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID không được để trống");
        }
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu trả lời với ID: " + answerId));
        return mapToAnswerResponse(answer);
    }

    public List<AnswerResponse> getAnswersByQuestionId(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống");
        }
        return answerRepository.findByQuestionQuestionIdAndIsActiveTrue(questionId).stream()
                .map(this::mapToAnswerResponse)
                .collect(Collectors.toList());
    }

    public List<AnswerResponse> getAllAnswersForAdminByQuestionId(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống");
        }
        return answerRepository.findByQuestionQuestionIdAndIsDeletedFalse(questionId).stream()
                .map(this::mapToAnswerResponse)
                .collect(Collectors.toList());
    }

    public AnswerResponse updateAnswer(Integer answerId, AnswerRequest request) {
        if (answerId == null || request == null || request.questionId() == null) {
            throw new IllegalArgumentException("Answer ID, request, hoặc questionId không được để trống");
        }
        if (request.answerText() == null || request.answerText().trim().isEmpty()) {
            throw new IllegalArgumentException("Nội dung câu trả lời không được để trống hoặc chỉ chứa khoảng trắng");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu trả lời với ID: " + answerId));

        if (Boolean.TRUE.equals(request.isCorrect())) {
            long correctAnswersCount = answerRepository.findByQuestionQuestionIdAndIsActiveTrue(request.questionId())
                    .stream().filter(a -> a.isCorrect() && !a.getAnswerId().equals(answerId)).count();
            if (correctAnswersCount >= 1) {
                throw new IllegalArgumentException("Một câu hỏi chỉ được có một câu trả lời đúng (trong số các câu trả lời đang hoạt động)");
            }
        }

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + request.questionId()));

        answer.setQuestion(question);
        answer.setAnswerText(request.answerText());
        answer.setCorrect(Boolean.TRUE.equals(request.isCorrect()));
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    public AnswerResponse toggleAnswerStatus(Integer answerId, boolean newStatus) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID không được để trống");
        }
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu trả lời với ID: " + answerId));

        if (answer.isDeleted()) {
            throw new IllegalArgumentException("Không thể thay đổi trạng thái của câu trả lời đã bị xóa mềm.");
        }

        if (newStatus && answer.isCorrect()) {
            long currentActiveCorrectAnswers = answerRepository.findByQuestionQuestionIdAndIsActiveTrue(answer.getQuestion().getQuestionId())
                    .stream().filter(a -> a.isCorrect() && !a.getAnswerId().equals(answerId)).count();
            if (currentActiveCorrectAnswers >= 1) {
                throw new IllegalArgumentException("Không thể kích hoạt câu trả lời này vì đã có một câu trả lời đúng khác đang hoạt động cho câu hỏi này.");
            }
        }

        answer.setActive(newStatus);
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    public void softDeleteAnswer(Integer answerId) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID không được để trống");
        }
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu trả lời với ID: " + answerId));

        answer.setDeleted(true);
        answer.setActive(false);
        answerRepository.save(answer);
    }

    public Page<AnswerResponse> searchAnswers(AnswerSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống");
        }

        // Validate sortBy field
        String sortBy = request.sortBy();
        if (!List.of("answerId", "answerText", "isCorrect", "isActive").contains(sortBy)) {
            sortBy = "answerId"; // Default to answerId if invalid
        }

        // Create Pageable with sort
        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        // Execute search
        Page<Answer> answers = answerRepository.searchAnswers(
                request.questionId(),
                request.isCorrect(),
                request.isActive(),
                request.answerText(),
                pageable
        );

        // Map to response
        return answers.map(this::mapToAnswerResponse);
    }

    private AnswerResponse mapToAnswerResponse(Answer answer) {
        return new AnswerResponse(
                answer.getAnswerId(),
                answer.getQuestion().getQuestionId(),
                answer.getAnswerText(),
                answer.isCorrect(),
                answer.isActive(),
                answer.isDeleted()
        );
    }
}