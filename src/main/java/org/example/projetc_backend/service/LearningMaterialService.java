package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.LearningMaterialRequest;
import org.example.projetc_backend.dto.LearningMaterialResponse;
import org.example.projetc_backend.entity.LearningMaterial;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.repository.LearningMaterialRepository;
import org.example.projetc_backend.repository.LessonRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class LearningMaterialService {

    private final LearningMaterialRepository learningMaterialRepository;
    private final LessonRepository lessonRepository;

    public LearningMaterialService(LearningMaterialRepository learningMaterialRepository, LessonRepository lessonRepository) {
        this.learningMaterialRepository = learningMaterialRepository;
        this.lessonRepository = lessonRepository;
    }

    public LearningMaterialResponse createLearningMaterial(LearningMaterialRequest request) {
        if (request == null || request.lessonId() == null || request.materialType() == null || request.materialUrl() == null) {
            throw new IllegalArgumentException("Request, lessonId, materialType, hoặc materialUrl không được để trống");
        }
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));
        if (learningMaterialRepository.findByMaterialUrl(request.materialUrl()).isPresent()) {
            throw new IllegalArgumentException("URL tài liệu đã tồn tại: " + request.materialUrl());
        }
        LearningMaterial material = new LearningMaterial();
        material.setLesson(lesson);
        material.setMaterialType(LearningMaterial.MaterialType.valueOf(request.materialType()));
        material.setMaterialUrl(request.materialUrl());
        material.setDescription(request.description() != null ? request.description() : "");
        material = learningMaterialRepository.save(material);
        return mapToLearningMaterialResponse(material);
    }

    public LearningMaterialResponse getLearningMaterialById(Integer materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID không được để trống");
        }
        LearningMaterial material = learningMaterialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu với ID: " + materialId));
        return mapToLearningMaterialResponse(material);
    }

    public List<LearningMaterialResponse> getLearningMaterialsByLessonId(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống");
        }
        return learningMaterialRepository.findByLessonLessonId(lessonId).stream()
                .map(this::mapToLearningMaterialResponse)
                .collect(Collectors.toList());
    }

    public LearningMaterialResponse updateLearningMaterial(Integer materialId, LearningMaterialRequest request) {
        if (materialId == null || request == null || request.lessonId() == null || request.materialType() == null || request.materialUrl() == null) {
            throw new IllegalArgumentException("Material ID, request, lessonId, materialType, hoặc materialUrl không được để trống");
        }
        LearningMaterial material = learningMaterialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu với ID: " + materialId));
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));
        learningMaterialRepository.findByMaterialUrl(request.materialUrl())
                .filter(existing -> !existing.getMaterialId().equals(materialId))
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("URL tài liệu đã tồn tại: " + request.materialUrl());
                });
        material.setLesson(lesson);
        material.setMaterialType(LearningMaterial.MaterialType.valueOf(request.materialType()));
        material.setMaterialUrl(request.materialUrl());
        material.setDescription(request.description() != null ? request.description() : material.getDescription());
        material = learningMaterialRepository.save(material);
        return mapToLearningMaterialResponse(material);
    }

    public void deleteLearningMaterial(Integer materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID không được để trống");
        }
        if (!learningMaterialRepository.existsById(materialId)) {
            throw new IllegalArgumentException("Không tìm thấy tài liệu với ID: " + materialId);
        }
        learningMaterialRepository.deleteById(materialId);
    }

    private LearningMaterialResponse mapToLearningMaterialResponse(LearningMaterial material) {
        return new LearningMaterialResponse(
                material.getMaterialId(),
                material.getLesson().getLessonId(),
                material.getMaterialType().toString(),
                material.getMaterialUrl(),
                material.getDescription()
        );
    }
}