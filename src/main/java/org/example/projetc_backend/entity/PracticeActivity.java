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
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ActivitySkill skill; // Kỹ năng chính của bài luyện tập (LISTENING, SPEAKING, WRITING, VOCABULARY, GRAMMAR, READING)

    @Enumerated(EnumType.STRING)
    @Column(name = "activity_type", nullable = false)
    private ActivityType activityType; // Loại hoạt động cụ thể (ví dụ: Dictation, RolePlay, EssayPrompt, FillInTheBlank)

    @Column(name = "material_url", columnDefinition = "TEXT") // URL của tài liệu nguồn (audio cho Listening/Speaking, image cho Speaking/Writing prompt, text cho Reading)
    private String materialUrl;

    @Column(name = "transcript_text", columnDefinition = "TEXT") // Bản chép lời đúng (cho Listening), hoặc văn bản gốc (cho Reading), hoặc văn bản prompt cho Speaking (nếu cần chấm điểm STT)
    private String transcriptText;

    @Column(name = "prompt_text", columnDefinition = "TEXT") // Đề bài/gợi ý cho Speaking/Writing (ví dụ: "Describe the picture", "Write about...")
    private String promptText;

    @Column(name = "expected_output_text", columnDefinition = "TEXT") // Đáp án mẫu/đoạn văn mẫu (cho Writing, hoặc là lời giải cho các bài khác)
    private String expectedOutputText;

    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    public enum ActivityType {
        LISTENING_DICTATION,      // Nghe và gõ lại
        LISTENING_COMPREHENSION,  // Nghe hiểu (có thể có câu hỏi phụ)
        SPEAKING_REPETITION,      // Lặp lại câu/từ
        SPEAKING_ROLEPLAY,        // Đóng vai/đối thoại
        WRITING_ESSAY,            // Viết bài luận
        WRITING_PARAGRAPH,        // Viết đoạn văn
        WRITING_FILL_IN_BLANK_WITH_OPTIONS, // MỚI: Điền từ vào đoạn văn có gợi ý lựa chọn
        WRITING_FILL_IN_BLANK_FREE_TEXT, // MỚI: Điền từ vào đoạn văn tự do
        VOCABULARY_MATCHING,      // Ghép từ
        GRAMMAR_FILL_IN_BLANK,    // Điền vào chỗ trống ngữ pháp
        READING_COMPREHENSION     // Đọc hiểu
    }

    public enum ActivitySkill {
        LISTENING, SPEAKING, READING, WRITING, VOCABULARY, GRAMMAR
    }
}