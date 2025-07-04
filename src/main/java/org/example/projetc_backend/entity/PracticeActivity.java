package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "PracticeActivities")
@Data
public class PracticeActivity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer activityId;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false) // Liên kết trực tiếp với Lesson
    private Lesson lesson;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivitySkill skill; // Kỹ năng chính của bài luyện tập (LISTENING, SPEAKING, WRITING, VOCABULARY, GRAMMAR)

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType; // Loại hoạt động cụ thể (ví dụ: Dictation, RolePlay, EssayPrompt)

    @Column(name = "material_url", columnDefinition = "TEXT") // URL của tài liệu (audio/video/text) cho bài luyện tập
    private String materialUrl;

    @Column(name = "transcript_text", columnDefinition = "TEXT") // Bản chép lời (cho bài nghe) hoặc văn bản gốc (cho bài đọc)
    private String transcriptText;

    @Column(name = "prompt_text", columnDefinition = "TEXT") // Gợi ý/đề bài cho bài nói/viết
    private String promptText;

    @Column(name = "expected_output_text", columnDefinition = "TEXT") // Đáp án/văn bản mẫu (nếu có)
    private String expectedOutputText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ActivityType {
        LISTENING_DICTATION,
        LISTENING_COMPREHENSION,
        SPEAKING_REPETITION,
        SPEAKING_ROLEPLAY,
        WRITING_ESSAY,
        WRITING_PARAGRAPH,
        VOCABULARY_MATCHING,
        GRAMMAR_FILL_IN_BLANK,
        READING_COMPREHENSION // Có thể thêm nếu bạn có các bài luyện đọc riêng
    }

    // Đã tách riêng enum skill để không phụ thuộc vào Lesson.Skill
    public enum ActivitySkill {
        LISTENING, SPEAKING, READING, WRITING, VOCABULARY, GRAMMAR
    }
}