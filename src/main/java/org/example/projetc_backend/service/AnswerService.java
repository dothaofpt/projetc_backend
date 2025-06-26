package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.AnswerRequest;
import org.example.projetc_backend.dto.AnswerResponse;
import org.example.projetc_backend.dto.AnswerSearchRequest; // Đảm bảo đã có AnswerSearchRequest DTO
import org.example.projetc_backend.entity.Answer;
import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.repository.AnswerRepository;
import org.example.projetc_backend.repository.QuestionRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class AnswerService {

    private final AnswerRepository answerRepository;
    private final QuestionRepository questionRepository;

    public AnswerService(AnswerRepository answerRepository, QuestionRepository questionRepository) {
        this.answerRepository = answerRepository;
        this.questionRepository = questionRepository;
    }

    /**
     * Creates a new answer for a given question.
     * Throws IllegalArgumentException if input is invalid or if a duplicate correct active answer exists.
     * @param request The AnswerRequest containing answer details.
     * @return AnswerResponse of the created answer.
     */
    @Transactional
    public AnswerResponse createAnswer(AnswerRequest request) {
        if (request == null || request.questionId() == null) {
            throw new IllegalArgumentException("Answer request or question ID cannot be null.");
        }
        if (request.answerText() == null || request.answerText().trim().isEmpty()) {
            throw new IllegalArgumentException("Answer content cannot be empty or just whitespace.");
        }

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + request.questionId()));

        // Validate that there's only one correct active answer per question
        if (Boolean.TRUE.equals(request.isCorrect()) && Boolean.TRUE.equals(request.isActive())) {
            validateSingleCorrectActiveAnswer(request.questionId(), null); // null for new answer
        }

        Answer answer = new Answer();
        answer.setQuestion(question);
        answer.setAnswerText(request.answerText());
        answer.setCorrect(Boolean.TRUE.equals(request.isCorrect()));
        answer.setActive(Boolean.TRUE.equals(request.isActive()));
        answer.setDeleted(false); // New answers are never soft-deleted

        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    /**
     * Retrieves an answer by its ID.
     * @param answerId The ID of the answer to retrieve.
     * @return AnswerResponse of the found answer.
     * @throws IllegalArgumentException if answer ID is null or answer not found.
     */
    public AnswerResponse getAnswerById(Integer answerId) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID cannot be null.");
        }
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found with ID: " + answerId));
        return mapToAnswerResponse(answer);
    }

    /**
     * Retrieves all active and non-soft-deleted answers for a specific question,
     * typically for general users.
     * @param questionId The ID of the question.
     * @return A list of AnswerResponse.
     * @throws IllegalArgumentException if question ID is null.
     */
    public List<AnswerResponse> getAnswersByQuestionIdForUser(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID cannot be null.");
        }
        // For users, we return only active and not soft-deleted answers
        return answerRepository.findByQuestionQuestionIdAndIsActiveTrue(questionId).stream()
                .filter(a -> !a.isDeleted()) // Explicitly filter out soft-deleted ones
                .map(this::mapToAnswerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Retrieves all non-soft-deleted answers for a specific question,
     * typically for admin users (can see inactive but not deleted).
     * @param questionId The ID of the question.
     * @return A list of AnswerResponse.
     * @throws IllegalArgumentException if question ID is null.
     */
    public List<AnswerResponse> getAllAnswersForAdminByQuestionId(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID cannot be null.");
        }
        // Admins can see all answers that are not soft-deleted
        return answerRepository.findByQuestionQuestionIdAndIsDeletedFalse(questionId).stream()
                .map(this::mapToAnswerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Updates an existing answer.
     * Throws IllegalArgumentException if input is invalid, answer/question not found,
     * or if the answer is soft-deleted, or if it violates the single correct active answer rule.
     * @param answerId The ID of the answer to update.
     * @param request The AnswerRequest containing updated details.
     * @return AnswerResponse of the updated answer.
     */
    @Transactional
    public AnswerResponse updateAnswer(Integer answerId, AnswerRequest request) {
        if (answerId == null || request == null || request.questionId() == null) {
            throw new IllegalArgumentException("Answer ID, request, or question ID cannot be null.");
        }
        if (request.answerText() == null || request.answerText().trim().isEmpty()) {
            throw new IllegalArgumentException("Answer content cannot be empty or just whitespace.");
        }

        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found with ID: " + answerId));

        if (answer.isDeleted()) {
            throw new IllegalArgumentException("Cannot update a soft-deleted answer.");
        }

        Question question = questionRepository.findById(request.questionId())
                .orElseThrow(() -> new IllegalArgumentException("Question not found with ID: " + request.questionId()));

        // Validate for single correct active answer only if it's becoming correct and active
        if (Boolean.TRUE.equals(request.isCorrect()) && Boolean.TRUE.equals(request.isActive())) {
            validateSingleCorrectActiveAnswer(request.questionId(), answerId);
        }

        answer.setQuestion(question); // Question can be changed
        answer.setAnswerText(request.answerText());
        answer.setCorrect(Boolean.TRUE.equals(request.isCorrect()));
        answer.setActive(Boolean.TRUE.equals(request.isActive()));

        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    /**
     * Toggles the active status of an answer.
     * Throws IllegalArgumentException if answer not found, or if it's soft-deleted,
     * or if activating a correct answer violates the single correct active answer rule.
     * @param answerId The ID of the answer.
     * @param newStatus The new active status (true/false).
     * @return AnswerResponse of the updated answer.
     */
    @Transactional
    public AnswerResponse toggleAnswerStatus(Integer answerId, boolean newStatus) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID cannot be null.");
        }
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found with ID: " + answerId));

        if (answer.isDeleted()) {
            throw new IllegalArgumentException("Cannot change status of a soft-deleted answer.");
        }

        // If activating the answer AND it's a correct answer, validate
        if (newStatus && answer.isCorrect()) {
            validateSingleCorrectActiveAnswer(answer.getQuestion().getQuestionId(), answerId);
        }

        answer.setActive(newStatus);
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    /**
     * Performs a soft delete on an answer (sets isDeleted to true and isActive to false).
     * Throws IllegalArgumentException if answer not found or already soft-deleted.
     * @param answerId The ID of the answer to soft delete.
     */
    @Transactional
    public void softDeleteAnswer(Integer answerId) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID cannot be null.");
        }
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found with ID: " + answerId));

        if (answer.isDeleted()) {
            throw new IllegalArgumentException("Answer is already soft-deleted.");
        }

        answer.setDeleted(true);
        answer.setActive(false); // Deactivating when soft-deleting is a common practice
        answerRepository.save(answer);
    }

    /**
     * Restores a soft-deleted answer (sets isDeleted to false).
     * The isActive status is not automatically changed upon restoration, allowing manual re-activation.
     * Throws IllegalArgumentException if answer not found or not soft-deleted.
     * @param answerId The ID of the answer to restore.
     * @return AnswerResponse of the restored answer.
     */
    @Transactional
    public AnswerResponse restoreAnswer(Integer answerId) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID cannot be null.");
        }
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Answer not found with ID: " + answerId));

        if (!answer.isDeleted()) {
            throw new IllegalArgumentException("Answer is not soft-deleted, no need to restore.");
        }

        answer.setDeleted(false);
        // isActive is not automatically set to true. It can be manually re-activated if needed.
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    /**
     * Searches for answers based on provided criteria with pagination and sorting.
     * @param request The AnswerSearchRequest containing search filters, pagination, and sorting info.
     * @return A Page of AnswerResponse.
     * @throws IllegalArgumentException if search request is null.
     */
    public Page<AnswerResponse> searchAnswers(AnswerSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request cannot be null.");
        }

        String sortBy = Optional.ofNullable(request.sortBy())
                .filter(s -> List.of("answerId", "answerText", "isCorrect", "isActive", "isDeleted").contains(s))
                .orElse("answerId"); // Default sort by answerId

        Sort.Direction sortDirection = "DESC".equalsIgnoreCase(request.sortDir()) ? Sort.Direction.DESC : Sort.Direction.ASC;
        Sort sort = Sort.by(sortDirection, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<Answer> answers = answerRepository.searchAnswers(
                request.questionId(),
                request.isCorrect(),
                request.isActive(),
                request.isDeleted(), // Pass isDeleted to the repository search method
                request.answerText(),
                pageable
        );

        return answers.map(this::mapToAnswerResponse);
    }

    /**
     * Helper method to ensure only one active and correct answer exists for a given question.
     * Throws IllegalArgumentException if violation is detected.
     * @param questionId The ID of the question.
     * @param currentAnswerId The ID of the answer being created/updated/toggled (null for new answer).
     */
    private void validateSingleCorrectActiveAnswer(Integer questionId, Integer currentAnswerId) {
        // Find all active answers for the question, then filter for correct ones, excluding the current one if it exists
        long correctActiveAnswersCount = answerRepository.findByQuestionQuestionIdAndIsActiveTrue(questionId)
                .stream()
                .filter(Answer::isCorrect)
                .filter(a -> currentAnswerId == null || !a.getAnswerId().equals(currentAnswerId))
                .count();

        if (correctActiveAnswersCount >= 1) {
            throw new IllegalArgumentException("A question can only have one correct and active answer.");
        }
    }

    /**
     * Maps an Answer entity to an AnswerResponse DTO.
     * @param answer The Answer entity to map.
     * @return AnswerResponse DTO.
     */
    private AnswerResponse mapToAnswerResponse(Answer answer) {
        if (answer == null) {
            return null;
        }
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