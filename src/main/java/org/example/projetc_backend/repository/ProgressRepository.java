package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Thêm import này

import java.util.List;
import java.util.Optional;

@Repository // Thêm annotation này
public interface ProgressRepository extends JpaRepository<Progress, Integer> {

    /**
     * Tìm kiếm một bản ghi tiến độ cụ thể cho một người dùng, bài học và loại hoạt động.
     * Đây là phương thức chính để kiểm tra xem một hoạt động đã có tiến độ hay chưa.
     * @param userId ID của người dùng.
     * @param lessonId ID của bài học.
     * @param activityType Loại hoạt động (enum).
     * @return Optional chứa bản ghi Progress nếu tìm thấy, ngược lại là rỗng.
     */
    Optional<Progress> findByUserUserIdAndLessonLessonIdAndActivityType(Integer userId, Integer lessonId, Progress.ActivityType activityType);

    /**
     * Tìm kiếm tất cả các bản ghi tiến độ của một người dùng trong một bài học cụ thể.
     * Phương thức này được sử dụng để tính tổng phần trăm hoàn thành của bài học.
     * @param userId ID của người dùng.
     * @param lessonId ID của bài học.
     * @return Danh sách các bản ghi Progress cho người dùng và bài học đó.
     */
    List<Progress> findByUserUserIdAndLessonLessonId(Integer userId, Integer lessonId);

    /**
     * Tìm kiếm tất cả các bản ghi tiến độ của một người dùng.
     * @param userId ID của người dùng.
     * @return Danh sách các bản ghi Progress của người dùng.
     */
    List<Progress> findByUserUserId(Integer userId);

    // Phương thức này không còn cần thiết vì Skill đã được thay thế bằng ActivityType
    // List<Progress> findBySkill(Progress.Skill skill);

    /**
     * Tìm kiếm tất cả các bản ghi tiến độ theo trạng thái.
     * @param status Trạng thái (NOT_STARTED, IN_PROGRESS, COMPLETED).
     * @return Danh sách các bản ghi Progress có trạng thái tương ứng.
     */
    List<Progress> findByStatus(Progress.Status status);
}
