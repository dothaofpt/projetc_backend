package org.example.projetc_backend.repository;

import org.example.projetc_backend.entity.FlashcardSet;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface FlashcardSetRepository extends JpaRepository<FlashcardSet, Integer> {
    Optional<FlashcardSet> findByTitle(String title);
    List<FlashcardSet> findByCreatorUserId(Integer creatorUserId);
    List<FlashcardSet> findByIsSystemCreatedTrue();

    @Query("SELECT fs FROM FlashcardSet fs WHERE " +
            "(:title IS NULL OR LOWER(fs.title) LIKE LOWER(CONCAT('%', :title, '%'))) AND " +
            "(:isSystemCreated IS NULL OR fs.isSystemCreated = :isSystemCreated) AND " +
            "(:creatorUserId IS NULL OR fs.creator.userId = :creatorUserId)")
    Page<FlashcardSet> searchFlashcardSets(
            @Param("title") String title,
            @Param("isSystemCreated") Boolean isSystemCreated,
            @Param("creatorUserId") Integer creatorUserId,
            Pageable pageable);
}