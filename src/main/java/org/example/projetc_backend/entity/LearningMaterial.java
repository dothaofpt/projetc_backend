package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;

@Entity
@Table(name = "LearningMaterials")
@Data
public class LearningMaterial {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer materialId;

    @ManyToOne
    @JoinColumn(name = "lesson_id", nullable = false)
    private Lesson lesson;

    @Column(name = "material_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private MaterialType materialType;

    @Column(name = "material_url", columnDefinition = "TEXT")
    private String materialUrl = ""; // URL tới file (audio, video, PDF)

    @Column(columnDefinition = "TEXT")
    private String description; // Mô tả tài liệu

    @Column(name = "transcript_text", columnDefinition = "TEXT") // Bổ sung: Lưu trữ bản text của audio/video
    private String transcriptText;

    public enum MaterialType {
        AUDIO, VIDEO, TEXT, IMAGE, PDF
    }
}