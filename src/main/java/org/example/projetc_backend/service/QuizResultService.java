package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.QuizResultRequest;
import org.example.projetc_backend.dto.QuizResultResponse;
import org.example.projetc_backend.dto.QuizResultSearchRequest;
import org.example.projetc_backend.dto.QuizResultPageResponse;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.entity.QuizResult;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.repository.QuizRepository;
import org.example.projetc_backend.repository.QuizResultRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class QuizResultService {

    private final QuizResultRepository quizResultRepository;
    private final UserRepository userRepository;
    private final QuizRepository quizRepository;

    public QuizResultService(QuizResultRepository quizResultRepository, UserRepository userRepository, QuizRepository quizRepository) {
        this.quizResultRepository = quizResultRepository;
        this.userRepository = userRepository;
        this.quizRepository = quizRepository;
    }

    /**
     * Lưu kết quả bài kiểm tra mới.
     *
     * @param request DTO chứa thông tin kết quả.
     * @return QuizResultResponse của kết quả đã lưu.
     * @throws IllegalArgumentException nếu dữ liệu request không hợp lệ hoặc không tìm thấy User/Quiz.
     */
    @Transactional
    public QuizResultResponse saveQuizResult(QuizResultRequest request) {
        if (request == null || request.userId() == null || request.quizId() == null || request.score() == null) {
            throw new IllegalArgumentException("User ID, Quiz ID và điểm số là bắt buộc.");
        }
        if (request.score() < 0) {
            throw new IllegalArgumentException("Điểm số không được nhỏ hơn 0.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        Quiz quiz = quizRepository.findById(request.quizId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + request.quizId()));

        QuizResult quizResult = new QuizResult();
        quizResult.setUser(user);
        quizResult.setQuiz(quiz);
        quizResult.setScore(request.score());
        quizResult.setCompletedAt(LocalDateTime.now());
        quizResult.setDurationSeconds(request.durationSeconds());

        quizResult = quizResultRepository.save(quizResult);
        return mapToQuizResultResponse(quizResult);
    }

    /**
     * Lấy kết quả bài kiểm tra của một người dùng cho một bài quiz cụ thể.
     *
     * @param userId ID của người dùng.
     * @param quizId ID của bài quiz.
     * @return QuizResultResponse của kết quả.
     * @throws IllegalArgumentException nếu ID trống hoặc không tìm thấy kết quả/User/Quiz.
     */
    @Transactional(readOnly = true)
    public QuizResultResponse getQuizResultByUserAndQuiz(Integer userId, Integer quizId) {
        if (userId == null || quizId == null) {
            throw new IllegalArgumentException("User ID và Quiz ID không được để trống.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
        }
        if (!quizRepository.existsById(quizId)) {
            throw new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId);
        }

        QuizResult quizResult = quizResultRepository.findByUserUserIdAndQuizQuizId(userId, quizId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy kết quả cho user ID: " + userId + " và quiz ID: " + quizId));
        return mapToQuizResultResponse(quizResult);
    }

    /**
     * Lấy tất cả các kết quả bài kiểm tra của một người dùng.
     *
     * @param userId ID của người dùng.
     * @return Danh sách QuizResultResponse.
     * @throws IllegalArgumentException nếu User ID trống hoặc không tìm thấy User.
     */
    @Transactional(readOnly = true)
    public List<QuizResultResponse> getQuizResultsByUser(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        if (!userRepository.existsById(userId)) {
            throw new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId);
        }

        return quizResultRepository.findByUserUserId(userId).stream()
                .map(this::mapToQuizResultResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy tất cả các kết quả bài kiểm tra cho một bài quiz cụ thể.
     *
     * @param quizId ID của bài quiz.
     * @return Danh sách QuizResultResponse.
     * @throws IllegalArgumentException nếu Quiz ID trống hoặc không tìm thấy Quiz.
     */
    @Transactional(readOnly = true)
    public List<QuizResultResponse> findQuizResultsByQuiz(Integer quizId) {
        if (quizId == null) {
            throw new IllegalArgumentException("Quiz ID không được để trống.");
        }
        if (!quizRepository.existsById(quizId)) {
            throw new IllegalArgumentException("Không tìm thấy bài kiểm tra với ID: " + quizId);
        }

        return quizResultRepository.findByQuizQuizId(quizId).stream()
                .map(this::mapToQuizResultResponse)
                .collect(Collectors.toList());
    }

    /**
     * Tìm kiếm và phân trang kết quả bài kiểm tra dựa trên các tiêu chí tùy chọn.
     *
     * @param request DTO chứa các tiêu chí tìm kiếm (userId, quizId, minScore, maxScore) và thông tin phân trang/sắp xếp.
     * @return Trang các QuizResultResponse phù hợp với tiêu chí tìm kiếm.
     * @throws IllegalArgumentException Nếu Search request trống.
     */
    @Transactional(readOnly = true)
    public QuizResultPageResponse searchQuizResults(QuizResultSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        // SỬA LỖI: Sử dụng accessor methods cho các trường của Record DTO
        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, request.sortBy());
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<QuizResult> resultPage = quizResultRepository.searchQuizResults(
                request.userId(),
                request.quizId(),
                request.minScore(),
                request.maxScore(),
                pageable
        );

        List<QuizResultResponse> content = resultPage.getContent().stream()
                .map(this::mapToQuizResultResponse)
                .collect(Collectors.toList());

        return new QuizResultPageResponse(
                content,
                resultPage.getTotalElements(),
                resultPage.getTotalPages(),
                resultPage.getNumber(),
                resultPage.getSize()
        );
    }

    /**
     * Xóa một kết quả bài kiểm tra khỏi cơ sở dữ liệu.
     *
     * @param resultId ID của kết quả cần xóa.
     * @throws IllegalArgumentException Nếu Result ID trống hoặc không tìm thấy kết quả.
     */
    @Transactional
    public void deleteQuizResult(Integer resultId) {
        if (resultId == null) {
            throw new IllegalArgumentException("Result ID không được để trống.");
        }
        if (!quizResultRepository.existsById(resultId)) {
            throw new IllegalArgumentException("Không tìm thấy kết quả bài kiểm tra với ID: " + resultId);
        }
        quizResultRepository.deleteById(resultId);
    }

    /**
     * Phương thức trợ giúp để ánh xạ đối tượng QuizResult entity sang QuizResultResponse DTO.
     *
     * @param quizResult Đối tượng QuizResult entity.
     * @return Đối tượng QuizResultResponse DTO tương ứng.
     */
    private QuizResultResponse mapToQuizResultResponse(QuizResult quizResult) {
        return new QuizResultResponse(
                quizResult.getResultId(),
                quizResult.getUser().getUserId(),
                quizResult.getQuiz().getQuizId(),
                quizResult.getScore(),
                quizResult.getCompletedAt(),
                quizResult.getDurationSeconds()
        );
    }
}