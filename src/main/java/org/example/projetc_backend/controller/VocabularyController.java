package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.VocabularyRequest;
import org.example.projetc_backend.dto.VocabularyResponse;
import org.example.projetc_backend.service.VocabularyService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/vocabulary")
@CrossOrigin(origins = "*")
public class VocabularyController {

    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @PostMapping
    public ResponseEntity<VocabularyResponse> createVocabulary(@RequestBody VocabularyRequest request) {
        VocabularyResponse response = vocabularyService.createVocabulary(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/{wordId}")
    public ResponseEntity<VocabularyResponse> getVocabularyById(@PathVariable Integer wordId) {
        VocabularyResponse response = vocabularyService.getVocabularyById(wordId);
        return ResponseEntity.ok(response);
    }

    @GetMapping
    public ResponseEntity<List<VocabularyResponse>> getAllVocabulary() {
        List<VocabularyResponse> responses = vocabularyService.getAllVocabulary();
        return ResponseEntity.ok(responses);
    }

    @PutMapping("/{wordId}")
    public ResponseEntity<VocabularyResponse> updateVocabulary(@PathVariable Integer wordId,
                                                               @RequestBody VocabularyRequest request) {
        VocabularyResponse response = vocabularyService.updateVocabulary(wordId, request);
        return ResponseEntity.ok(response);
    }

    @DeleteMapping("/{wordId}")
    public ResponseEntity<Void> deleteVocabulary(@PathVariable Integer wordId) {
        vocabularyService.deleteVocabulary(wordId);
        return ResponseEntity.noContent().build();
    }
}
