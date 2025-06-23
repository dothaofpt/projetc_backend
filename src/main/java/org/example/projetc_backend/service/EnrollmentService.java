package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.EnrollmentRequest;
import org.example.projetc_backend.dto.EnrollmentResponse;
import org.example.projetc_backend.dto.LessonResponse; // <--- Import LessonResponse
import org.example.projetc_backend.entity.Enrollment;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.repository.EnrollmentRepository;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
public class EnrollmentService {

    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;
    private final LessonRepository lessonRepository;
    private final LessonService lessonService; // <--- Inject LessonService vào đây

    private static final Map<Lesson.Level, Integer> LEVEL_DURATIONS = new HashMap<>();
    static {
        LEVEL_DURATIONS.put(Lesson.Level.BEGINNER, 6);
        LEVEL_DURATIONS.put(Lesson.Level.INTERMEDIATE, 8);
        LEVEL_DURATIONS.put(Lesson.Level.ADVANCED, 12);
    }

    // <--- Cập nhật Constructor để inject LessonService
    public EnrollmentService(EnrollmentRepository enrollmentRepository, UserRepository userRepository, LessonRepository lessonRepository, LessonService lessonService) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.lessonService = lessonService; // <--- Khởi tạo LessonService
    }

    public EnrollmentResponse enrollUserInLesson(EnrollmentRequest request) {
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.userId()));
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + request.lessonId()));

        if (enrollmentRepository.findByUserUserIdAndLessonLessonId(request.userId(), request.lessonId()).isPresent()) {
            throw new IllegalArgumentException("User is already enrolled in this lesson.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setLesson(lesson);
        enrollment = enrollmentRepository.save(enrollment);

        return mapToEnrollmentResponse(enrollment);
    }

    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponse> getEnrollmentsByUserId(Integer userId) {
        return enrollmentRepository.findByUserUserId(userId).stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponse> getExpiringOrExpiredEnrollments() {
        return enrollmentRepository.findAll().stream()
                .filter(enrollment -> {
                    LocalDateTime expiryDate = calculateExpiryDate(enrollment);
                    return expiryDate.isBefore(LocalDateTime.now()) ||
                            expiryDate.isBefore(LocalDateTime.now().plusDays(7));
                })
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    public void deleteEnrollment(Integer enrollmentId) {
        if (!enrollmentRepository.existsById(enrollmentId)) {
            throw new IllegalArgumentException("Enrollment not found with ID: " + enrollmentId);
        }
        enrollmentRepository.deleteById(enrollmentId);
    }

    private LocalDateTime calculateExpiryDate(Enrollment enrollment) {
        Integer durationMonths = LEVEL_DURATIONS.get(enrollment.getLesson().getLevel());
        if (durationMonths == null) {
            throw new IllegalStateException("Duration not defined for lesson level: " + enrollment.getLesson().getLevel());
        }
        return enrollment.getEnrollmentDate().plusMonths(durationMonths);
    }

    // <--- Cập nhật phương thức mapToEnrollmentResponse
    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        LocalDateTime expiryDate = calculateExpiryDate(enrollment);
        String status = expiryDate.isAfter(LocalDateTime.now()) ? "ACTIVE" : "EXPIRED";

        // Chuyển đổi Lesson entity sang LessonResponse DTO
        // Đảm bảo mapToLessonResponse trong LessonService là public hoặc có thể truy cập được
        LessonResponse lessonResponse = lessonService.mapToLessonResponse(enrollment.getLesson());

        return new EnrollmentResponse(
                enrollment.getEnrollmentId(),
                enrollment.getUser().getUserId(),
                enrollment.getUser().getUsername(),
                lessonResponse, // <--- TRUYỀN ĐỐI TƯỢNG LESSONRESPONSE VÀO ĐÂY
                enrollment.getEnrollmentDate(),
                expiryDate,
                status // <--- TRUYỀN STATUS ĐÃ TÍNH TOÁN VÀO ĐÂY
        );
    }
}