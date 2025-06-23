package org.example.projetc_backend.repository;// package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Enrollment;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface EnrollmentRepository extends JpaRepository<Enrollment, Integer> {
    // Tìm kiếm một đăng ký cụ thể của một user cho một lesson
    Optional<Enrollment> findByUserUserIdAndLessonLessonId(Integer userId, Integer lessonId);

    // Tìm kiếm tất cả các đăng ký cho một lesson cụ thể
    List<Enrollment> findByLessonLessonId(Integer lessonId);

    // THÊM DÒNG NÀY: Tìm kiếm tất cả các đăng ký của một người dùng cụ thể
    List<Enrollment> findByUserUserId(Integer userId);
}