package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.EnrollmentRequest;
import org.example.projetc_backend.dto.EnrollmentResponse;
import org.example.projetc_backend.dto.EnrollmentSearchRequest;
import org.example.projetc_backend.dto.LessonResponse;
import org.example.projetc_backend.entity.Enrollment;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.repository.EnrollmentRepository;
import org.example.projetc_backend.repository.LessonRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
    private final LessonService lessonService;

    private static final Map<Lesson.Level, Integer> LEVEL_DURATIONS = new HashMap<>();
    static {
        LEVEL_DURATIONS.put(Lesson.Level.BEGINNER, 6);
        LEVEL_DURATIONS.put(Lesson.Level.INTERMEDIATE, 8);
        LEVEL_DURATIONS.put(Lesson.Level.ADVANCED, 12);
    }

    public EnrollmentService(EnrollmentRepository enrollmentRepository, UserRepository userRepository, LessonRepository lessonRepository, LessonService lessonService) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
        this.lessonService = lessonService;
    }

    // Phương thức này CẦN ĐƯỢC THÊM VÀO EnrollmentService.java
    /**
     * Lấy tất cả các đăng ký hiện có trong hệ thống.
     * @return Danh sách EnrollmentResponse của tất cả các đăng ký.
     */
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    // ... (Các phương thức khác của EnrollmentService mà bạn đã có) ...
    // Ví dụ: enrollUserInLesson, getEnrollmentById, getEnrollmentsByUserId, etc.

    public EnrollmentResponse enrollUserInLesson(EnrollmentRequest request) {
        if (request == null || request.userId() == null || request.lessonId() == null) {
            throw new IllegalArgumentException("User ID và Lesson ID là bắt buộc để đăng ký.");
        }

        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + request.userId()));
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        if (enrollmentRepository.findByUserUserIdAndLessonLessonId(request.userId(), request.lessonId()).isPresent()) {
            throw new IllegalArgumentException("Người dùng đã đăng ký bài học này rồi.");
        }

        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setLesson(lesson);
        enrollment.setEnrollmentDate(LocalDateTime.now());

        enrollment = enrollmentRepository.save(enrollment);

        return mapToEnrollmentResponse(enrollment);
    }

    public EnrollmentResponse getEnrollmentById(Integer enrollmentId) {
        if (enrollmentId == null) {
            throw new IllegalArgumentException("Enrollment ID không được để trống.");
        }
        Enrollment enrollment = enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy đăng ký với ID: " + enrollmentId));
        return mapToEnrollmentResponse(enrollment);
    }

    public List<EnrollmentResponse> getEnrollmentsByUserId(Integer userId) {
        if (userId == null) {
            throw new IllegalArgumentException("User ID không được để trống.");
        }
        userRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người dùng với ID: " + userId));
        return enrollmentRepository.findByUserUserId(userId).stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    public List<EnrollmentResponse> getEnrollmentsByLessonId(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        lessonRepository.findById(lessonId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId));
        return enrollmentRepository.findByLessonLessonId(lessonId).stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }


    public Page<EnrollmentResponse> searchEnrollments(EnrollmentSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        String sortBy = request.sortBy();
        if (!List.of("enrollmentId", "user.userId", "lesson.lessonId", "enrollmentDate").contains(sortBy)) {
            sortBy = "enrollmentId";
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<Enrollment> enrollments = enrollmentRepository.searchEnrollments(
                request.userId(),
                request.lessonId(),
                pageable
        );
        return enrollments.map(this::mapToEnrollmentResponse);
    }

    public void deleteEnrollment(Integer enrollmentId) {
        if (enrollmentId == null) {
            throw new IllegalArgumentException("Enrollment ID không được để trống.");
        }
        if (!enrollmentRepository.existsById(enrollmentId)) {
            throw new IllegalArgumentException("Không tìm thấy đăng ký với ID: " + enrollmentId);
        }
        enrollmentRepository.deleteById(enrollmentId);
    }

    private LocalDateTime calculateExpiryDate(Enrollment enrollment) {
        Integer durationMonths = LEVEL_DURATIONS.get(enrollment.getLesson().getLevel());
        if (durationMonths == null) {
            throw new IllegalStateException("Thời gian không được định nghĩa cho cấp độ bài học: " + enrollment.getLesson().getLevel());
        }
        return enrollment.getEnrollmentDate().plusMonths(durationMonths);
    }

    // Phương thức chuyển đổi Entity sang Response DTO
    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        if (enrollment == null) {
            return null;
        }
        LocalDateTime expiryDate = calculateExpiryDate(enrollment);
        String status = expiryDate.isAfter(LocalDateTime.now()) ? "ACTIVE" : "EXPIRED";

        LessonResponse lessonResponse = lessonService.mapToLessonResponse(enrollment.getLesson());

        return new EnrollmentResponse(
                enrollment.getEnrollmentId(),
                enrollment.getUser().getUserId(),
                enrollment.getUser().getUsername(),
                lessonResponse,
                enrollment.getEnrollmentDate(),
                expiryDate,
                status
        );
    }
}