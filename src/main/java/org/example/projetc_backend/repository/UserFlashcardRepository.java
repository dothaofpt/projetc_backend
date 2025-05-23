package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.UserFlashcard;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface UserFlashcardRepository extends JpaRepository<UserFlashcard, Integer> {
    Optional<UserFlashcard> findByUserIdAndWordId(Integer userId, Integer wordId);
}