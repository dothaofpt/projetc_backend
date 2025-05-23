package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.Vocabulary;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VocabularyRepository extends JpaRepository<Vocabulary, Integer> {
}