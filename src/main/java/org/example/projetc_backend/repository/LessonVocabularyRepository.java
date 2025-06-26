package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.LessonVocabulary;
import org.example.projetc_backend.entity.LessonVocabularyId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository; // Thêm import này

import java.util.List;

@Repository // Thêm annotation này
public interface LessonVocabularyRepository extends JpaRepository<LessonVocabulary, LessonVocabularyId> {
    List<LessonVocabulary> findByIdLessonId(Integer lessonId);
    List<LessonVocabulary> findByIdWordId(Integer wordId);

    // Bổ sung phương thức kiểm tra sự tồn tại
    boolean existsByIdLessonIdAndIdWordId(Integer lessonId, Integer wordId);

    // Bổ sung phương thức xóa theo lessonId và wordId
    void deleteByIdLessonIdAndIdWordId(Integer lessonId, Integer wordId);
}