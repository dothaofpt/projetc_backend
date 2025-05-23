package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.AnswerRequest;
import org.example.projetc_backend.dto.AnswerResponse;
import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.entity.Answer;
import org.example.projetc_backend.repository.QuestionRepository;
import org.example.projetc_backend.repository.AnswerRepository;
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
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAnswerText(request.answerText());
        answer.setCorrect(request.isCorrect());
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    public AnswerResponse getAnswerById(Integer answerId) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
        return mapToAnswerResponse(answer);
    }

    public List<AnswerResponse> getAnswersByQuestionId(Integer questionId) {
        return answerRepository.findByQuestionQuestionId(questionId).stream()
                .map(this::mapToAnswerResponse)
                .collect(Collectors.toList());
    }

    public AnswerResponse updateAnswer(Integer answerId, AnswerRequest request) {
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found"));
        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found"));
        answer.setQuestion(question);
        answer.setAnswerText(request.answerText());
        answer.setCorrect(request.isCorrect());
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    public void deleteAnswer(Integer answerId) {
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