package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
// import org.example.projetc_backend.entity.Question; // Dòng này không cần thiết nếu đã import ở trên

@Entity
@Table(name = "Answers")
@Data
public class Answer {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer answerId;

    @ManyToOne
    @JoinColumn(name = "question_id", nullable = false)
    private Question question;

    @Column(name = "answer_text", nullable = false)
    private String answerText;

    @Column(name = "is_correct", nullable = false)
    private boolean isCorrect;

    // Trường isActive để quản lý trạng thái kích hoạt/vô hiệu hóa của câu trả lời
    @Column(name = "is_active", nullable = false)
    private boolean isActive = true; // Mặc định là true khi tạo mới

    // THAY ĐỔI MỚI: Trường isDeleted để quản lý trạng thái xóa mềm
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; // Mặc định là false (chưa xóa mềm)

    // Lombok @Data sẽ tự tạo getters/setters cho isDeleted, nhưng nếu không dùng Lombok:
    // public boolean isDeleted() {
    //     return isDeleted;
    // }
    // public void setDeleted(boolean deleted) {
    //     isDeleted = deleted;
    // }
}