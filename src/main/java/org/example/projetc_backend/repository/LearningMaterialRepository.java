package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.LearningMaterial;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Integer> {
    List<LearningMaterial> findByLessonLessonId(Integer lessonId);
    List<LearningMaterial> findByMaterialType(LearningMaterial.MaterialType materialType);
    Optional<LearningMaterial> findByMaterialUrl(String materialUrl);
}