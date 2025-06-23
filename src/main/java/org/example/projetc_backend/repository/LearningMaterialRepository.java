package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.LearningMaterial;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface LearningMaterialRepository extends JpaRepository<LearningMaterial, Integer> {
    List<LearningMaterial> findByLessonLessonId(Integer lessonId);
    List<LearningMaterial> findByMaterialType(LearningMaterial.MaterialType materialType);
    Optional<LearningMaterial> findByMaterialUrl(String materialUrl);

    @Query("SELECT m FROM LearningMaterial m WHERE " +
            "(:lessonId IS NULL OR m.lesson.lessonId = :lessonId) AND " +
            "(:materialType IS NULL OR m.materialType = :materialType) AND " +
            "(:description IS NULL OR LOWER(m.description) LIKE LOWER(CONCAT('%', :description, '%')))")
    Page<LearningMaterial> searchMaterials(
            @Param("lessonId") Integer lessonId,
            @Param("materialType") LearningMaterial.MaterialType materialType,
            @Param("description") String description,
            Pageable pageable);
}