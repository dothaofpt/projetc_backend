package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.LearningMaterialRequest;
import org.example.projetc_backend.dto.LearningMaterialResponse;
import org.example.projetc_backend.service.LearningMaterialService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/learning-materials")
@CrossOrigin(origins = "*")
public class LearningMaterialController {

    private final LearningMaterialService learningMaterialService;

    public LearningMaterialController(LearningMaterialService learningMaterialService) {
        this.learningMaterialService = learningMaterialService;
    }

    @PostMapping
    public ResponseEntity<LearningMaterialResponse> createLearningMaterial(@RequestBody LearningMaterialRequest request) {
        LearningMaterialResponse response = learningMaterialService.createLearningMaterial(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{materialId}")
    public ResponseEntity<LearningMaterialResponse> getLearningMaterialById(@PathVariable Integer materialId) {
        LearningMaterialResponse response = learningMaterialService.getLearningMaterialById(materialId);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/lesson/{lessonId}")
    public ResponseEntity<List<LearningMaterialResponse>> getLearningMaterialsByLessonId(@PathVariable Integer lessonId) {
        List<LearningMaterialResponse> responses = learningMaterialService.getLearningMaterialsByLessonId(lessonId);
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{materialId}")
    public ResponseEntity<LearningMaterialResponse> updateLearningMaterial(@PathVariable Integer materialId,
                                                                           @RequestBody LearningMaterialRequest request) {
        LearningMaterialResponse response = learningMaterialService.updateLearningMaterial(materialId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{materialId}")
    public ResponseEntity<Void> deleteLearningMaterial(@PathVariable Integer materialId) {
        learningMaterialService.deleteLearningMaterial(materialId);
        return ResponseEntity.noContent().build();
    }
}
