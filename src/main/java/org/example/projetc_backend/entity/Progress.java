package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Progress")
@Data
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer progressId;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    // THAY ĐỔI LỚN: Thay thế Skill bằng ActivityType
    @Column(name = "activity_type", nullable = false) // Đảm bảo cột có tên rõ ràng trong DB
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    @Column(nullable = false) // Đặt nullable = false để đảm bảo trạng thái luôn có
    @Enumerated(EnumType.STRING)
    private Status status = Status.NOT_STARTED; // Mặc định là NOT_STARTED khi tạo mới

    @Column(name = "completion_percentage", nullable = false) // Đặt nullable = false
    private Integer completionPercentage = 0; // Mặc định là 0% khi tạo mới

    @Column(name = "last_updated", nullable = false) // Đặt nullable = false
    private LocalDateTime lastUpdated; // Không khởi tạo ở đây, sẽ được set bởi @PrePersist và @PreUpdate

    // Enum mới cho ActivityType
    public enum ActivityType {
        READING_MATERIAL, // Đọc tài liệu
        FLASHCARDS,      // Học Flashcard
        QUIZ,            // Làm bài Quiz
        LISTENING_PRACTICE, // Luyện nghe
        SPEAKING_EXERCISE,  // Bài tập nói
        WRITING_TASK,       // Bài tập viết
        GRAMMAR_EXERCISE,   // Bài tập ngữ pháp
        VOCABULARY_BUILDER // Xây dựng từ vựng (từ vựng chung, không riêng flashcard)
        // Thêm các loại hoạt động khác nếu cần
    }

    // Enum cho Status (giữ nguyên)
    public enum Status {
        NOT_STARTED, IN_PROGRESS, COMPLETED
    }

    // Tự động set lastUpdated khi tạo mới (persist)
    @PrePersist
    protected void onCreate() {
        lastUpdated = LocalDateTime.now();
    }

    // Tự động set lastUpdated khi cập nhật (update)
    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}
