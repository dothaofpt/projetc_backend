package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Quizzes")
@Data
public class Quiz {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer quizId;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(length = 255)
    private String title = "";

    // Đã bỏ trường 'Skill' ở đây để tránh trùng lặp và dựa vào QuizType để phân loại bài test
    // Nếu bạn vẫn muốn có trường Skill ở đây, hãy đổi tên enum để tránh nhầm lẫn với Lesson.Skill hoặc PracticeActivity.ActivitySkill

    @Column(name = "quiz_type", nullable = false) // ĐÃ THÊM: Trường để phân loại bài kiểm tra
    @Enumerated(EnumType.STRING)
    private QuizType quizType;

    @Column(name = "created_at")
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum QuizType {
        LISTENING_TEST,
        SPEAKING_TEST,
        READING_TEST,
        WRITING_TEST,
        GRAMMAR_TEST,
        VOCABULARY_TEST,
        COMPREHENSIVE_TEST // Test tổng hợp nhiều kỹ năng
    }
}