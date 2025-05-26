package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.AnswerRequest;
import org.example.projetc_backend.dto.AnswerResponse;
import org.example.projetc_backend.entity.Answer;
import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.repository.AnswerRepository;
import org.example.projetc_backend.repository.QuestionRepository;
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
        if (request.isCorrect() != null && request.isCorrect()) {
            long correctAnswersCount = answerRepository.findByQuestionQuestionId(request.questionId())
                    .stream().filter(Answer::isCorrect).count();
            if (correctAnswersCount >= 1) {
                throw new IllegalArgumentException("Một câu hỏi chỉ được có một câu trả lời đúng");
            }
        }
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAnswerText(request.answerText());
        answer.setCorrect(Boolean.TRUE.equals(request.isCorrect()));
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
        return answerRepository.findByQuestionQuestionId(questionId).stream()
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
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu hỏi với ID: " + request.questionId()));
        if (request.isCorrect() != null && request.isCorrect()) {
            long correctAnswersCount = answerRepository.findByQuestionQuestionId(request.questionId())
                    .stream().filter(a -> a.isCorrect() && !a.getAnswerId().equals(answerId)).count();
            if (correctAnswersCount >= 1) {
                throw new IllegalArgumentException("Một câu hỏi chỉ được có một câu trả lời đúng");
            }
        }
        answer.setQuestion(question);
        answer.setAnswerText(request.answerText());
        answer.setCorrect(Boolean.TRUE.equals(request.isCorrect()));
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    public void deleteAnswer(Integer answerId) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID không được để trống");
        }
        if (!answerRepository.existsById(answerId)) {
            throw new IllegalArgumentException("Không tìm thấy câu trả lời với ID: " + answerId);
        }
        answerRepository.deleteById(answerId);
    }

    private AnswerResponse mapToAnswerResponse(Answer answer) {
        return new AnswerResponse(
                answer.getAnswerId(),
                answer.getQuestion().getQuestionId(),
                answer.getAnswerText(),
                answer.isCorrect()
        );
    }
}