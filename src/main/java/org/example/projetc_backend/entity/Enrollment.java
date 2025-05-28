package org.example.projetc_backend.entity;

import jakarta.persistence.*;
import lombok.Data;
import java.time.LocalDateTime;

@Entity
@Table(name = "Enrollments") // Đảm bảo tên bảng là "Enrollments"
@Data // Lombok sẽ tự động tạo getters, setters, toString, equals, hashCode
public class Enrollment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer enrollmentId;

    @ManyToOne // Mối quan hệ nhiều Enrollment tới một User
    @JoinColumn(name = "user_id", nullable = false) // Tên cột khóa ngoại trong bảng Enrollments
    private User user; // Tham chiếu đến Entity User

    @ManyToOne // Mối quan hệ nhiều Enrollment tới một Lesson
    @JoinColumn(name = "lesson_id", nullable = false) // Tên cột khóa ngoại trong bảng Enrollments
    private Lesson lesson; // Tham chiếu đến Entity Lesson

    @Column(name = "enrollment_date", nullable = false)
    private LocalDateTime enrollmentDate; // Ngày đăng ký khóa học

    // Sử dụng @PrePersist để tự động đặt enrollmentDate khi một Enrollment mới được lưu
    @PrePersist
    protected void onCreate() {
        if (enrollmentDate == null) {
            enrollmentDate = LocalDateTime.now();
        }
    }
}