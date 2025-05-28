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

        // Logic kiểm tra câu trả lời đúng chỉ áp dụng cho các câu trả lời ĐANG HOẠT ĐỘNG.
        // Khi tạo mới, câu trả lời sẽ mặc định là inactive, nên nó sẽ không được tính
        // vào giới hạn một câu trả lời đúng đang hoạt động cho đến khi được kích hoạt.
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
        // Mặc định isActive là false khi tạo mới theo yêu cầu cũ
        answer.setActive(false);
        // THAY ĐỔI MỚI: Mặc định isDeleted là false khi tạo mới
        answer.setDeleted(false);
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    /**
     * Lấy thông tin chi tiết một câu trả lời theo ID.
     * Phương thức này sẽ trả về cả câu trả lời đang hoạt động, đã bị vô hiệu hóa hoặc đã bị xóa mềm,
     * để frontend có thể quyết định cách hiển thị (ví dụ: cho chức năng khôi phục).
     *
     * @param answerId ID của câu trả lời.
     * @return AnswerResponse của câu trả lời.
     * @throws IllegalArgumentException nếu không tìm thấy câu trả lời.
     */
    public AnswerResponse getAnswerById(Integer answerId) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID không được để trống");
        }
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu trả lời với ID: " + answerId));
        return mapToAnswerResponse(answer);
    }

    /**
     * Lấy danh sách các câu trả lời ĐANG HOẠT ĐỘNG cho một câu hỏi cụ thể.
     *
     * @param questionId ID của câu hỏi.
     * @return Danh sách AnswerResponse của các câu trả lời active.
     */
    public List<AnswerResponse> getAnswersByQuestionId(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống");
        }
        // Chỉ trả về các câu trả lời đang active cho câu hỏi này
        return answerRepository.findByQuestionQuestionIdAndIsActiveTrue(questionId).stream()
                .map(this::mapToAnswerResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy TẤT CẢ các câu trả lời (active và inactive) cho một câu hỏi cụ thể,
     * NHƯNG KHÔNG BAO GỒM CÁC CÂU ĐÃ BỊ XÓA MỀM (isDeleted = true).
     * Dùng cho trang quản trị viên hiển thị đầy đủ trạng thái hoạt động/vô hiệu hóa.
     *
     * @param questionId ID của câu hỏi.
     * @return Danh sách AnswerResponse của tất cả các câu trả lời chưa bị xóa mềm.
     */
    public List<AnswerResponse> getAllAnswersForAdminByQuestionId(Integer questionId) {
        if (questionId == null) {
            throw new IllegalArgumentException("Question ID không được để trống");
        }
        // THAY ĐỔI MỚI: Chỉ lấy những câu trả lời chưa bị xóa mềm
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

        // Logic kiểm tra câu trả lời đúng chỉ tính những câu trả lời active và không phải chính câu trả lời đang được cập nhật
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
        // Trạng thái isActive và isDeleted không bị thay đổi khi update thông thường.
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    /**
     * Bật hoặc tắt trạng thái isActive của một câu trả lời.
     *
     * @param answerId  ID của câu trả lời.
     * @param newStatus Trạng thái mới (true = active, false = inactive).
     * @return AnswerResponse của câu trả lời với trạng thái mới.
     * @throws IllegalArgumentException nếu không tìm thấy câu trả lời.
     */
    public AnswerResponse toggleAnswerStatus(Integer answerId, boolean newStatus) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID không được để trống");
        }
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu trả lời với ID: " + answerId));

        // Nếu câu trả lời đã bị xóa mềm, không cho phép thay đổi trạng thái active
        if (answer.isDeleted()) {
            throw new IllegalArgumentException("Không thể thay đổi trạng thái của câu trả lời đã bị xóa mềm.");
        }

        // Nếu bạn đang cố gắng kích hoạt một câu trả lời đúng và đã có một câu trả lời đúng khác đang hoạt động,
        // bạn có thể thêm logic ở đây để xử lý (ví dụ: vô hiệu hóa câu trả lời đúng cũ, hoặc báo lỗi).
        if (newStatus && answer.isCorrect()) {
            long currentActiveCorrectAnswers = answerRepository.findByQuestionQuestionIdAndIsActiveTrue(answer.getQuestion().getQuestionId())
                    .stream().filter(a -> a.isCorrect() && !a.getAnswerId().equals(answerId)).count();
            if (currentActiveCorrectAnswers >= 1) {
                throw new IllegalArgumentException("Không thể kích hoạt câu trả lời này vì đã có một câu trả lời đúng khác đang hoạt động cho câu hỏi này.");
            }
        }

        answer.setActive(newStatus); // Đặt trạng thái mới
        answer = answerRepository.save(answer);
        return mapToAnswerResponse(answer);
    }

    /**
     * "Xóa mềm" một câu trả lời bằng cách đặt trạng thái isDeleted thành true.
     * Câu trả lời cũng sẽ được đặt isActive thành false.
     *
     * @param answerId ID của câu trả lời cần xóa mềm.
     * @throws IllegalArgumentException nếu không tìm thấy câu trả lời.
     */
    public void softDeleteAnswer(Integer answerId) {
        if (answerId == null) {
            throw new IllegalArgumentException("Answer ID không được để trống");
        }
        Answer answer = answerRepository.findById(answerId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy câu trả lời với ID: " + answerId));

        // THAY ĐỔI MỚI: Đặt isDeleted thành true
        answer.setDeleted(true);
        // Đồng thời đặt isActive thành false khi xóa mềm để nó không hiển thị ở bất kỳ đâu
        answer.setActive(false);
        answerRepository.save(answer); // Lưu thay đổi
    }

    private AnswerResponse mapToAnswerResponse(Answer answer) {
        return new AnswerResponse(
                answer.getAnswerId(),
                answer.getQuestion().getQuestionId(),
                answer.getAnswerText(),
                answer.isCorrect(),
                answer.isActive(),
                answer.isDeleted() // THAY ĐỔI MỚI: Map trường isDeleted
        );
    }
}