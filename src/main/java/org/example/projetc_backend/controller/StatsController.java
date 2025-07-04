package org.example.projetc_backend.controller;

import org.example.projetc_backend.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

// Import các entity để sử dụng enum hoặc truy vấn group by
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.Vocabulary;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.Quiz;
import org.example.projetc_backend.entity.Question;
import org.example.projetc_backend.entity.QuizResult;


@RestController
@RequestMapping("/api/stats") // Đổi mapping thành /api/stats để rõ ràng hơn
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class StatsController {
    @Autowired
    private UserRepository userRepo;
    @Autowired
    private VocabularyRepository vocabRepo;
    @Autowired
    private LessonRepository lessonRepo;
    @Autowired
    private QuizRepository quizRepo;
    @Autowired
    private QuestionRepository questionRepo; // Inject QuestionRepository
    @Autowired
    private QuizResultRepository quizResultRepo; // Inject QuizResultRepository


    /**
     * Lấy các số liệu thống kê toàn diện về hệ thống.
     * Chỉ ADMIN mới có quyền truy cập.
     * @return Map chứa các số liệu thống kê.
     */
    @GetMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Map<String, Object>> getStats() { // Thay đổi return type thành Map<String, Object> để chứa cả Long và Map
        try {
            Map<String, Object> stats = new HashMap<>();

            // 1. Các số lượng cơ bản
            stats.put("userCount", userRepo.count());
            stats.put("vocabularyCount", vocabRepo.count());
            stats.put("lessonCount", lessonRepo.count());
            stats.put("quizCount", quizRepo.count());
            stats.put("questionCount", questionRepo.count()); // Tổng số câu hỏi
            stats.put("quizResultCount", quizResultRepo.count()); // Tổng số kết quả quiz

            // 2. Phân phối vai trò người dùng (Giả định các method countBy... đã có trong repo hoặc dùng stream)
            // CÁCH 1: Dùng stream (ít hiệu quả cho dữ liệu lớn)
            Map<String, Long> userRoleDistribution = userRepo.findAll().stream()
                    .collect(Collectors.groupingBy(user -> user.getRole().name(), Collectors.counting()));
            stats.put("userRoleDistribution", userRoleDistribution);
            // CÁCH 2 (Tốt hơn): Nếu bạn có custom query trong UserRepository:
            // @Query("SELECT u.role, COUNT(u) FROM User u GROUP BY u.role")
            // List<Object[]> countUsersByRole();
            // Map<String, Long> userRoleDistribution = userRepo.countUsersByRole().stream().collect(
            //     Collectors.toMap(
            //         arr -> ((User.Role) arr[0]).name(),
            //         arr -> (Long) arr[1]
            //     )
            // );
            // stats.put("userRoleDistribution", userRoleDistribution);


            // 3. Phân phối độ khó từ vựng
            Map<String, Long> vocabularyDifficultyDistribution = vocabRepo.findAll().stream()
                    .collect(Collectors.groupingBy(vocab -> vocab.getDifficultyLevel().name(), Collectors.counting()));
            stats.put("vocabularyDifficultyDistribution", vocabularyDifficultyDistribution);


            // 4. Phân phối cấp độ bài học
            Map<String, Long> lessonLevelDistribution = lessonRepo.findAll().stream()
                    .collect(Collectors.groupingBy(lesson -> lesson.getLevel().name(), Collectors.counting()));
            stats.put("lessonLevelDistribution", lessonLevelDistribution);


            // 5. Phân phối kỹ năng bài học
            Map<String, Long> lessonSkillDistribution = lessonRepo.findAll().stream()
                    .collect(Collectors.groupingBy(lesson -> lesson.getSkill().name(), Collectors.counting()));
            stats.put("lessonSkillDistribution", lessonSkillDistribution);


            return new ResponseEntity<>(stats, HttpStatus.OK);
        } catch (Exception e) {
            e.printStackTrace(); // Log lỗi chi tiết hơn trong ứng dụng thực tế
            return new ResponseEntity<>(null, HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
