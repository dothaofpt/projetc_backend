package org.example.projetc_backend.controller;

import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.QuizRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize; // Import PreAuthorize
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class StatsController {
    @Autowired
    private UserRepository userRepo;
    @Autowired private VocabularyRepository vocabRepo;
    @Autowired private LessonRepository lessonRepo;
    @Autowired private QuizRepository quizRepo;

    /**
     * Lấy các số liệu thống kê cơ bản về số lượng người dùng, từ vựng, bài học và quiz.
     * Chỉ ADMIN mới có quyền truy cập.
     * @return Map chứa các số liệu thống kê.
     */
    @GetMapping("/stats")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Long>> getStats() {
        try {
            Map<String, Long> stats = new HashMap<>();
            stats.put("userCount", userRepo.count());
            stats.put("vocabularyCount", vocabRepo.count());
            stats.put("lessonCount", lessonRepo.count());
            stats.put("quizCount", quizRepo.count());
            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR); // Xử lý lỗi chung
        }
    }
}