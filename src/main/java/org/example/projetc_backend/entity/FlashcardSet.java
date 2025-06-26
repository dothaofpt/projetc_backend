package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;
import java.util.List;

@Entity
@Table(name = "FlashcardSets")
@Data
public class FlashcardSet {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer setId;

    @Column(nullable = false, length = 255)
    private String title;

    @Column(columnDefinition = "TEXT")
    private String description;

    @ManyToOne // Nếu bộ flashcard do người dùng tạo
    @JoinColumn(name = "creator_user_id")
    private User creator;

    @Column(name = "is_system_created", nullable = false)
    private boolean isSystemCreated = false; // true nếu bộ flashcard do admin/hệ thống tạo

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    // Quan hệ Many-to-Many với Vocabulary thông qua FlashcardSetVocabulary
    // Không cần @OneToMany trực tiếp ở đây, vì sẽ quản lý thông qua bảng nối
    // private List<FlashcardSetVocabulary> flashcardVocabularies;
}