package org.example.projetc_backend.controller;

import org.example.projetc_backend.dto.VocabularyRequest;
import org.example.projetc_backend.dto.VocabularyResponse;
import org.example.projetc_backend.dto.VocabularySearchRequest;
import org.example.projetc_backend.dto.VocabularyPageResponse;
import org.example.projetc_backend.service.VocabularyService;
import org.springframework.data.domain.Page;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import jakarta.validation.Valid;

import java.util.List;

@RestController
@RequestMapping("/api/vocabulary")
@CrossOrigin(origins = {"http://localhost:4200", "http://localhost:8000", "http://localhost:8080", "http://localhost:61299"})
public class VocabularyController {

    private final VocabularyService vocabularyService;

    public VocabularyController(VocabularyService vocabularyService) {
        this.vocabularyService = vocabularyService;
    }

    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VocabularyResponse> createVocabulary(@Valid @RequestBody VocabularyRequest request) {
        try {
            VocabularyResponse response = vocabularyService.createVocabulary(request);
            return new ResponseEntity<>(response, HttpStatus.CREATED);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new VocabularyResponse(null, null, e.getMessage(), null, null, null, null, null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new VocabularyResponse(null, null, "Đã xảy ra lỗi hệ thống.", null, null, null, null, null, null));
        }
    }

    @GetMapping("/{wordId}")
    public ResponseEntity<VocabularyResponse> getVocabularyById(@PathVariable Integer wordId) {
        try {
            VocabularyResponse response = vocabularyService.getVocabularyById(wordId);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }

    @PostMapping("/search")
    public ResponseEntity<VocabularyPageResponse> searchVocabulary(@RequestBody VocabularySearchRequest searchRequest) {
        try {
            Page<VocabularyResponse> responsePage = vocabularyService.searchVocabularies(searchRequest);
            VocabularyPageResponse customResponsePage = new VocabularyPageResponse(
                    responsePage.getContent(),
                    responsePage.getTotalElements(),
                    responsePage.getTotalPages(),
                    responsePage.getNumber(),
                    responsePage.getSize()
            );
            return new ResponseEntity<>(customResponsePage, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new VocabularyPageResponse(List.of(), 0, 0, 0, searchRequest.size()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new VocabularyPageResponse(List.of(), 0, 0, 0, searchRequest.size()));
        }
    }

    @PutMapping("/{wordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<VocabularyResponse> updateVocabulary(@PathVariable Integer wordId,
                                                               @Valid @RequestBody VocabularyRequest request) {
        try {
            VocabularyResponse response = vocabularyService.updateVocabulary(wordId, request);
            return new ResponseEntity<>(response, HttpStatus.OK);
        } catch (IllegalArgumentException e) {
            return ResponseEntity.badRequest().body(new VocabularyResponse(null, null, e.getMessage(), null, null, null, null, null, null));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(new VocabularyResponse(null, null, "Đã xảy ra lỗi hệ thống.", null, null, null, null, null, null));
        }
    }

    @DeleteMapping("/{wordId}")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<Void> deleteVocabulary(@PathVariable Integer wordId) {
        try {
            vocabularyService.deleteVocabulary(wordId);
            return new ResponseEntity<>(HttpStatus.NO_CONTENT);
        } catch (IllegalArgumentException e) {
            return new ResponseEntity<>(null, HttpStatus.NOT_FOUND);
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(null);
        }
    }
}