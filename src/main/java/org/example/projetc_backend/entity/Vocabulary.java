package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp; // THÊM DÒNG IMPORT NÀY!
import org.hibernate.annotations.UpdateTimestamp; // THÊM DÒNG IMPORT NÀY CHO UPDATEDAT

@Entity
@Table(name = "vocabulary")
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_id")
    private Integer wordId;

    @Column(name = "word", nullable = false, unique = true, length = 255) // unique = true có thể gây lỗi nếu có từ đã xóa mềm
    private String word;

    @Column(name = "meaning", columnDefinition = "TEXT", nullable = false)
    private String meaning;

    @Column(name = "example_sentence", columnDefinition = "TEXT")
    private String exampleSentence;

    @Column(name = "pronunciation", length = 255)
    private String pronunciation;

    @Column(name = "audio_url", columnDefinition = "TEXT")
    private String audioUrl;

    @Column(name = "image_url", columnDefinition = "TEXT")
    private String imageUrl;

    @Column(name = "writing_prompt", columnDefinition = "TEXT")
    private String writingPrompt;

    @Enumerated(EnumType.STRING)
    @Column(name = "difficulty_level", nullable = false)
    private DifficultyLevel difficultyLevel;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp // THÊM ANNOTATION NÀY CHO UPDATEDAT
    @Column(name = "updated_at") // CÓ THỂ THÊM CỘT updated_at
    private LocalDateTime updatedAt;

    // THÊM TRƯỜNG NÀY CHO XÓA MỀM
    @Column(name = "is_deleted", nullable = false)
    private boolean isDeleted = false; // Mặc định là false (chưa xóa)

    // THUỘC TÍNH ÁNH XẠ MỐI QUAN HỆ TỚI FLASHCARD_SET_VOCABULARY
    @OneToMany(mappedBy = "vocabulary", cascade = CascadeType.ALL, orphanRemoval = true)
    private Set<FlashcardSetVocabulary> flashcardSetVocabularies = new HashSet<>();

    public Vocabulary() {
        // Constructor rỗng mặc định là cần thiết cho JPA
    }

    // --- Getters và Setters ---
    public Integer getWordId() {
        return wordId;
    }

    public void setWordId(Integer wordId) {
        this.wordId = wordId;
    }

    public String getWord() {
        return word;
    }

    public void setWord(String word) {
        this.word = word;
    }

    public String getMeaning() {
        return meaning;
    }

    public void setMeaning(String meaning) {
        this.meaning = meaning;
    }

    public String getExampleSentence() {
        return exampleSentence;
    }

    public void setExampleSentence(String exampleSentence) {
        this.exampleSentence = exampleSentence;
    }

    public String getPronunciation() {
        return pronunciation;
    }

    public void setPronunciation(String pronunciation) {
        this.pronunciation = pronunciation;
    }

    public String getAudioUrl() {
        return audioUrl;
    }

    public void setAudioUrl(String audioUrl) {
        this.audioUrl = audioUrl;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public String getWritingPrompt() {
        return writingPrompt;
    }

    public void setWritingPrompt(String writingPrompt) {
        this.writingPrompt = writingPrompt;
    }

    public DifficultyLevel getDifficultyLevel() {
        return difficultyLevel;
    }

    public void setDifficultyLevel(DifficultyLevel difficultyLevel) {
        this.difficultyLevel = difficultyLevel;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public void setUpdatedAt(LocalDateTime updatedAt) {
        this.updatedAt = updatedAt;
    }

    // Getter và Setter cho isDeleted
    public boolean isDeleted() {
        return isDeleted;
    }

    public void setDeleted(boolean deleted) {
        isDeleted = deleted;
    }

    public Set<FlashcardSetVocabulary> getFlashcardSetVocabularies() {
        return flashcardSetVocabularies;
    }

    public void setFlashcardSetVocabularies(Set<FlashcardSetVocabulary> flashcardSetVocabularies) {
        this.flashcardSetVocabularies = flashcardSetVocabularies;
    }

    // Enum DifficultyLevel
    public enum DifficultyLevel {
        EASY,
        MEDIUM,
        HARD
    }
}