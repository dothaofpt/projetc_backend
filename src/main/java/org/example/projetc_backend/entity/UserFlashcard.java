package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "UserFlashcards")
@Data
public class UserFlashcard {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @ManyToOne // Bổ sung quan hệ ManyToOne đến User
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne // Bổ sung quan hệ ManyToOne đến Vocabulary
    @JoinColumn(name = "word_id", nullable = false)
    private Vocabulary vocabulary;

    @Column(name = "is_known")
    private boolean isKnown = false; // Đã biết từ này chưa

    @Column(name = "last_reviewed_at") // Bổ sung: Thời gian lần cuối ôn tập
    private LocalDateTime lastReviewedAt;

    @Column(name = "next_review_at") // Bổ sung: Thời gian ôn tập tiếp theo (cho SRS)
    private LocalDateTime nextReviewAt;

    @Column(name = "review_interval_days") // Bổ sung: Khoảng thời gian ôn tập (cho SRS)
    private Integer reviewIntervalDays = 0;

    @Column(name = "ease_factor") // Bổ sung: Hệ số dễ nhớ (cho SRS)
    private Double easeFactor = 2.5;

    public UserFlashcard() {}

    public UserFlashcard(User user, Vocabulary vocabulary, boolean isKnown) {
        this.user = user;
        this.vocabulary = vocabulary;
        this.isKnown = isKnown;
        this.lastReviewedAt = LocalDateTime.now();
        this.nextReviewAt = LocalDateTime.now(); // Ban đầu có thể xem xét ngay
    }

    // Constructor với tất cả các trường
    public UserFlashcard(User user, Vocabulary vocabulary, boolean isKnown, LocalDateTime lastReviewedAt, LocalDateTime nextReviewAt, Integer reviewIntervalDays, Double easeFactor) {
        this.user = user;
        this.vocabulary = vocabulary;
        this.isKnown = isKnown;
        this.lastReviewedAt = lastReviewedAt;
        this.nextReviewAt = nextReviewAt;
        this.reviewIntervalDays = reviewIntervalDays;
        this.easeFactor = easeFactor;
    }
}