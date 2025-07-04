package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.PracticeActivity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface PracticeActivityRepository extends JpaRepository<PracticeActivity, Integer> {
    // Tìm kiếm các hoạt động luyện tập theo ID bài học
    List<PracticeActivity> findByLessonLessonId(Integer lessonId);

    // Tìm kiếm các hoạt động luyện tập theo kỹ năng (LISTENING, SPEAKING, WRITING, v.v.)
    List<PracticeActivity> findBySkill(PracticeActivity.ActivitySkill skill);

    // Tìm kiếm một hoạt động luyện tập theo tiêu đề (có thể cần thiết cho việc duy nhất hoặc tìm kiếm chính xác)
    Optional<PracticeActivity> findByTitle(String title);

    // Tìm kiếm và phân trang các hoạt động luyện tập với các tiêu chí tùy chọn
    @Query("SELECT pa FROM PracticeActivity pa WHERE " +
            "(:lessonId IS NULL OR pa.lesson.lessonId = :lessonId) AND " +
            "(:title IS NULL OR LOWER(pa.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:skill IS NULL OR pa.skill = :skill) AND " +
            "(:activityType IS NULL OR pa.activityType = :activityType)")
    Page<PracticeActivity> searchPracticeActivities(
            @Param("lessonId") Integer lessonId,
            @Param("title") String title,
            @Param("skill") PracticeActivity.ActivitySkill skill,
            @Param("activityType") PracticeActivity.ActivityType activityType,
            Pageable pageable);
}