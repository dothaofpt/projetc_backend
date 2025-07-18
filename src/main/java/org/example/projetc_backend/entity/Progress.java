package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import lombok.NoArgsConstructor; // Đảm bảo có NoArgsConstructor và AllArgsConstructor nếu dùng lombok cho entities
import lombok.AllArgsConstructor;

import java.time.LocalDateTime;

@Entity
@Table(name = "Progress")
@Data
@NoArgsConstructor // Cần cho JPA
@AllArgsConstructor // Có thể hữu ích cho constructor đầy đủ
public class Progress {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer progressId;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm fetch type nếu cần tối ưu
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne(fetch = FetchType.LAZY) // Thêm fetch type nếu cần tối ưu
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "activity_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private ActivityType activityType;

    @Column(nullable = false)
    @Enumerated(EnumType.STRING)
    private Status status; // Bỏ = Status.NOT_STARTED; ở đây, để default trong constructor hoặc service

    @Column(name = "completion_percentage", nullable = false)
    private Integer completionPercentage; // Bỏ = 0; ở đây, để default trong constructor hoặc service

    @Column(name = "last_updated", nullable = false) // Đảm bảo tên cột khớp với DTO: lastUpdated
    private LocalDateTime lastUpdated;

    // --- Cập nhật ENUM ActivityType để khớp với Frontend ---
    public enum ActivityType {
        LISTENING_DICTATION,
        LISTENING_COMPREHENSION,
        SPEAKING_REPETITION,
        SPEAKING_ROLEPLAY,
        WRITING_ESSAY,
        WRITING_PARAGRAPH,
        VOCABULARY_MATCHING, // <--- THÊM NÀY
        GRAMMAR_FILL_IN_BLANK,
        READING_MATERIAL,
        LISTENING_PRACTICE // <--- THÊM NÀY (Nếu bạn có LISTENING_PRACTICE trong frontend mà chưa có ở backend)
    }

    // --- Cập nhật ENUM Status để khớp với Frontend ---
    public enum Status {
        NOT_STARTED,
        IN_PROGRESS,
        COMPLETED
    }

    @PrePersist
    protected void onCreate() {
        if (status == null) { // Đặt giá trị mặc định nếu chưa được set
            status = Status.NOT_STARTED;
        }
        if (completionPercentage == null) { // Đặt giá trị mặc định nếu chưa được set
            completionPercentage = 0;
        }
        lastUpdated = LocalDateTime.now();
    }

    @PreUpdate
    protected void onUpdate() {
        lastUpdated = LocalDateTime.now();
    }
}