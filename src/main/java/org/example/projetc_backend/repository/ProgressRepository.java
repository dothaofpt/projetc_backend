package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Progress;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ProgressRepository extends JpaRepository<Progress, Integer> {
    Optional<Progress> findByUserUserIdAndLessonLessonId(Integer userId, Integer lessonId);
    List<Progress> findByUserUserId(Integer userId);
}