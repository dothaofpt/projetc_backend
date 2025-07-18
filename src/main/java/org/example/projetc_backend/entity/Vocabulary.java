package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import org.hibernate.annotations.CreationTimestamp; // THÊM DÒNG IMPORT NÀY!

@Entity
@Table(name = "vocabulary")
public class Vocabulary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "word_id")
    private Integer wordId;

    @Column(name = "word", nullable = false, unique = true, length = 255)
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

    // THÊM ANNOTATION @CreationTimestamp VÀO ĐÂY!
    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false) // 'updatable = false' để cột này chỉ được set một lần khi tạo
    private LocalDateTime createdAt;

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

    // Getter cho createdAt
    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    // Setter cho createdAt (có thể không cần thiết nếu chỉ dùng @CreationTimestamp và updatable = false)
    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
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