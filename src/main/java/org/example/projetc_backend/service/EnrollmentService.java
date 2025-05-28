package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.EnrollmentRequest;
import org.example.projetc_backend.dto.EnrollmentResponse;
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

    // Ánh xạ thời hạn cho mỗi cấp độ bài học, giống như trong LessonService
    // Điều này đảm bảo tính nhất quán trong việc tính toán thời hạn
    private static final Map<Lesson.Level, Integer> LEVEL_DURATIONS = new HashMap<>();
    static {
        LEVEL_DURATIONS.put(Lesson.Level.BEGINNER, 6);   // 6 tháng
        LEVEL_DURATIONS.put(Lesson.Level.INTERMEDIATE, 8); // 8 tháng
        LEVEL_DURATIONS.put(Lesson.Level.ADVANCED, 12);  // 12 tháng
    }

    public EnrollmentService(EnrollmentRepository enrollmentRepository, UserRepository userRepository, LessonRepository lessonRepository) {
        this.enrollmentRepository = enrollmentRepository;
        this.userRepository = userRepository;
        this.lessonRepository = lessonRepository;
    }

    /**
     * Đăng ký một người dùng vào một khóa học.
     * @param request EnrollmentRequest chứa userId và lessonId.
     * @return EnrollmentResponse của đăng ký vừa tạo.
     * @throws IllegalArgumentException nếu user, lesson không tồn tại hoặc user đã đăng ký khóa học.
     */
    public EnrollmentResponse enrollUserInLesson(EnrollmentRequest request) {
        // Tìm User
        User user = userRepository.findById(request.userId())
                .orElseThrow(() -> new IllegalArgumentException("User not found with ID: " + request.userId()));
        // Tìm Lesson
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Lesson not found with ID: " + request.lessonId()));

        // Kiểm tra xem user đã đăng ký khóa học này chưa
        if (enrollmentRepository.findByUserUserIdAndLessonLessonId(request.userId(), request.lessonId()).isPresent()) {
            throw new IllegalArgumentException("User is already enrolled in this lesson.");
        }

        // Tạo Enrollment mới
        Enrollment enrollment = new Enrollment();
        enrollment.setUser(user);
        enrollment.setLesson(lesson);
        // enrollment.setEnrollmentDate(LocalDateTime.now()); // @PrePersist đã xử lý
        enrollment = enrollmentRepository.save(enrollment); // Lưu vào DB

        return mapToEnrollmentResponse(enrollment); // Chuyển đổi sang DTO và trả về
    }

    /**
     * Lấy tất cả các đăng ký khóa học. Chỉ dành cho ADMIN.
     * @return Danh sách EnrollmentResponse.
     */
    public List<EnrollmentResponse> getAllEnrollments() {
        return enrollmentRepository.findAll().stream()
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Lấy danh sách các đăng ký khóa học sắp hết hạn hoặc đã hết hạn. Chỉ dành cho ADMIN.
     * Định nghĩa "sắp hết hạn": là trong vòng 7 ngày tới (có thể điều chỉnh).
     * @return Danh sách EnrollmentResponse của các khóa học sắp hết hạn/đã hết hạn.
     */
    public List<EnrollmentResponse> getExpiringOrExpiredEnrollments() {
        return enrollmentRepository.findAll().stream()
                .filter(enrollment -> {
                    LocalDateTime expiryDate = calculateExpiryDate(enrollment);
                    // Kiểm tra nếu ngày hết hạn đã qua hoặc trong vòng 7 ngày tới
                    return expiryDate.isBefore(LocalDateTime.now()) || // Đã hết hạn
                            expiryDate.isBefore(LocalDateTime.now().plusDays(7)); // Sắp hết hạn trong 7 ngày
                })
                .map(this::mapToEnrollmentResponse)
                .collect(Collectors.toList());
    }

    /**
     * Xóa một đăng ký khóa học. Chỉ dành cho ADMIN.
     * @param enrollmentId ID của đăng ký cần xóa.
     * @throws IllegalArgumentException nếu không tìm thấy đăng ký.
     */
    public void deleteEnrollment(Integer enrollmentId) {
        if (!enrollmentRepository.existsById(enrollmentId)) {
            throw new IllegalArgumentException("Enrollment not found with ID: " + enrollmentId);
        }
        enrollmentRepository.deleteById(enrollmentId);
    }

    /**
     * Tính toán ngày hết hạn của khóa học dựa trên ngày đăng ký và thời hạn của Lesson.
     * @param enrollment Đối tượng Enrollment.
     * @return LocalDateTime là ngày hết hạn.
     * @throws IllegalStateException nếu không tìm thấy thời hạn cho Level của Lesson.
     */
    private LocalDateTime calculateExpiryDate(Enrollment enrollment) {
        Integer durationMonths = LEVEL_DURATIONS.get(enrollment.getLesson().getLevel());
        if (durationMonths == null) {
            // Đây là trường hợp không mong muốn, nên xử lý một cách cẩn thận
            throw new IllegalStateException("Duration not defined for lesson level: " + enrollment.getLesson().getLevel());
        }
        return enrollment.getEnrollmentDate().plusMonths(durationMonths);
    }

    /**
     * Chuyển đổi Enrollment Entity sang EnrollmentResponse DTO.
     * @param enrollment Enrollment Entity.
     * @return EnrollmentResponse DTO.
     */
    private EnrollmentResponse mapToEnrollmentResponse(Enrollment enrollment) {
        LocalDateTime expiryDate = calculateExpiryDate(enrollment);
        return new EnrollmentResponse(
                enrollment.getEnrollmentId(),
                enrollment.getUser().getUserId(),
                enrollment.getUser().getUsername(), // Lấy username từ User entity
                enrollment.getLesson().getLessonId(),
                enrollment.getLesson().getTitle(), // Lấy title từ Lesson entity
                enrollment.getEnrollmentDate(),
                expiryDate
        );
    }
}