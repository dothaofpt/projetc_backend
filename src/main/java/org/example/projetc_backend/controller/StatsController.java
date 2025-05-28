package org.example.projetc_backend.controller;

import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.QuizRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.VocabularyRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class StatsController {
    @Autowired
    private UserRepository userRepo;
    @Autowired private VocabularyRepository vocabRepo;
    @Autowired private LessonRepository lessonRepo;
    @Autowired private QuizRepository quizRepo;

    @GetMapping("/stats")
    public Map<String, Long> getStats() {
        Map<String, Long> stats = new HashMap<>();
        stats.put("userCount", userRepo.count());
        stats.put("vocabularyCount", vocabRepo.count());
        stats.put("lessonCount", lessonRepo.count());
        stats.put("quizCount", quizRepo.count());
        return stats;
    }
}