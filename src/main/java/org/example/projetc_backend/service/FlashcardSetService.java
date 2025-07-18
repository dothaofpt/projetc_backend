package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.FlashcardResponse;
import org.example.projetc_backend.dto.FlashcardSetRequest;
import org.example.projetc_backend.dto.FlashcardSetResponse;
import org.example.projetc_backend.dto.FlashcardSetSearchRequest;
import org.example.projetc_backend.dto.VocabularyResponse; // Vẫn giữ nếu mapVocabularyToResponse được gọi ở đâu đó
import org.example.projetc_backend.entity.FlashcardSet;
import org.example.projetc_backend.entity.FlashcardSetVocabulary;
import org.example.projetc_backend.entity.FlashcardSetVocabularyId;
import org.example.projetc_backend.entity.User;
import org.example.projetc_backend.entity.UserFlashcard;
import org.example.projetc_backend.entity.Vocabulary;
import org.example.projetc_backend.repository.FlashcardSetRepository;
import org.example.projetc_backend.repository.FlashcardSetVocabularyRepository;
import org.example.projetc_backend.repository.UserFlashcardRepository;
import org.example.projetc_backend.repository.UserRepository;
import org.example.projetc_backend.repository.VocabularyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class FlashcardSetService {

    private final FlashcardSetRepository flashcardSetRepository;
    private final UserRepository userRepository;
    private final VocabularyRepository vocabularyRepository;
    private final FlashcardSetVocabularyRepository flashcardSetVocabularyRepository;
    private final UserFlashcardRepository userFlashcardRepository;
    private final FlashcardService flashcardService;

    public FlashcardSetService(FlashcardSetRepository flashcardSetRepository,
                               UserRepository userRepository,
                               VocabularyRepository vocabularyRepository,
                               FlashcardSetVocabularyRepository flashcardSetVocabularyRepository,
                               UserFlashcardRepository userFlashcardRepository,
                               FlashcardService flashcardService) {
        this.flashcardSetRepository = flashcardSetRepository;
        this.userRepository = userRepository;
        this.vocabularyRepository = vocabularyRepository;
        this.flashcardSetVocabularyRepository = flashcardSetVocabularyRepository;
        this.userFlashcardRepository = userFlashcardRepository;
        this.flashcardService = flashcardService;
    }

    @Transactional
    public FlashcardSetResponse createFlashcardSet(FlashcardSetRequest request) {
        if (request == null || request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Tiêu đề của bộ flashcard là bắt buộc.");
        }
        if (request.creatorUserId() == null && !Boolean.TRUE.equals(request.isSystemCreated())) {
            throw new IllegalArgumentException("Người tạo bộ flashcard (creatorUserId) là bắt buộc nếu không phải là bộ do hệ thống tạo.");
        }

        User creator = null;
        if (request.creatorUserId() != null) {
            creator = userRepository.findById(request.creatorUserId())
                    .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người tạo với ID: " + request.creatorUserId()));
        }

        Optional<FlashcardSet> existingSet = flashcardSetRepository.findByTitle(request.title());
        if (existingSet.isPresent()) {
            throw new IllegalArgumentException("Bộ flashcard với tiêu đề '" + request.title() + "' đã tồn tại.");
        }

        FlashcardSet flashcardSet = new FlashcardSet();
        flashcardSet.setTitle(request.title().trim());
        flashcardSet.setDescription(request.description() != null ? request.description().trim() : null);
        flashcardSet.setCreator(creator);
        flashcardSet.setSystemCreated(Boolean.TRUE.equals(request.isSystemCreated()));
        flashcardSet.setCreatedAt(LocalDateTime.now());
        flashcardSet = flashcardSetRepository.save(flashcardSet);

        if (request.wordIds() != null && !request.wordIds().isEmpty()) {
            for (Integer wordId : request.wordIds()) {
                if (flashcardSet.getSetId() != null) {
                    addVocabularyToSet(flashcardSet.getSetId(), wordId);
                } else {
                    throw new IllegalStateException("Set ID không được tạo sau khi lưu FlashcardSet.");
                }
            }
        }

        return mapToFlashcardSetResponse(flashcardSet, null);
    }

    @Transactional(readOnly = true)
    public FlashcardSetResponse getFlashcardSetById(Integer setId, Integer currentUserId) {
        if (setId == null) {
            throw new IllegalArgumentException("Set ID không được để trống.");
        }
        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ flashcard với ID: " + setId));

        return mapToFlashcardSetResponse(flashcardSet, currentUserId);
    }

    @Transactional(readOnly = true)
    public Page<FlashcardSetResponse> searchFlashcardSets(FlashcardSetSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        String sortBy = request.sortBy();
        if (!List.of("setId", "title", "createdAt", "creator.userId", "isSystemCreated").contains(sortBy)) {
            sortBy = "setId";
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        Page<FlashcardSet> sets = flashcardSetRepository.searchFlashcardSets(
                request.title(),
                request.isSystemCreated(),
                request.creatorUserId(),
                pageable
        );

        return sets.map(flashcardSet -> mapToFlashcardSetResponse(flashcardSet, null));
    }

    @Transactional
    public FlashcardSetResponse updateFlashcardSet(Integer setId, FlashcardSetRequest request) {
        if (setId == null || request == null || request.title() == null || request.title().trim().isEmpty()) {
            throw new IllegalArgumentException("Set ID và tiêu đề của bộ flashcard là bắt buộc.");
        }

        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ flashcard với ID: " + setId));

        flashcardSetRepository.findByTitle(request.title())
                .filter(fs -> !fs.getSetId().equals(setId))
                .ifPresent(fs -> {
                    throw new IllegalArgumentException("Bộ flashcard với tiêu đề '" + request.title() + "' đã tồn tại.");
                });

        flashcardSet.setTitle(request.title().trim());
        flashcardSet.setDescription(request.description() != null ? request.description().trim() : null);
        if (request.isSystemCreated() != null) {
            flashcardSet.setSystemCreated(request.isSystemCreated());
        }

        if (request.creatorUserId() != null) {
            if (flashcardSet.getCreator() == null || !flashcardSet.getCreator().getUserId().equals(request.creatorUserId())) {
                User newCreator = userRepository.findById(request.creatorUserId())
                        .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy người tạo mới với ID: " + request.creatorUserId()));
                flashcardSet.setCreator(newCreator);
            }
        } else {
            if (!flashcardSet.isSystemCreated()) {
                flashcardSet.setCreator(null);
            }
        }

        flashcardSet = flashcardSetRepository.save(flashcardSet);

        if (request.wordIds() != null) {
            List<Integer> currentWordIds = flashcardSetVocabularyRepository.findByFlashcardSetSetId(setId).stream()
                    .map(fsv -> fsv.getVocabulary().getWordId())
                    .collect(Collectors.toList());

            List<Integer> wordsToAdd = request.wordIds().stream()
                    .filter(wordId -> !currentWordIds.contains(wordId))
                    .collect(Collectors.toList());

            List<Integer> wordsToRemove = currentWordIds.stream()
                    .filter(wordId -> !request.wordIds().contains(wordId))
                    .collect(Collectors.toList());

            wordsToAdd.forEach(wordId -> addVocabularyToSet(setId, wordId));
            wordsToRemove.forEach(wordId -> removeVocabularyFromSet(setId, wordId));
        }

        return mapToFlashcardSetResponse(flashcardSet, null);
    }

    @Transactional
    public void deleteFlashcardSet(Integer setId) {
        if (setId == null) {
            throw new IllegalArgumentException("Set ID không được để trống.");
        }
        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ flashcard với ID: " + setId));

        flashcardSetRepository.delete(flashcardSet);
    }

    @Transactional
    public void addVocabularyToSet(Integer setId, Integer wordId) {
        if (setId == null || wordId == null) {
            throw new IllegalArgumentException("Set ID và Word ID không được để trống.");
        }

        FlashcardSet flashcardSet = flashcardSetRepository.findById(setId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bộ flashcard với ID: " + setId));
        Vocabulary vocabulary = vocabularyRepository.findById(wordId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy từ vựng với ID: " + wordId));

        if (flashcardSetVocabularyRepository.existsByFlashcardSetSetIdAndVocabularyWordId(setId, wordId)) {
            throw new IllegalArgumentException("Từ vựng ID " + wordId + " đã tồn tại trong bộ flashcard ID " + setId + ".");
        }

        FlashcardSetVocabulary fsv = new FlashcardSetVocabulary(flashcardSet, vocabulary);
        flashcardSetVocabularyRepository.save(fsv);
    }

    @Transactional
    public void removeVocabularyFromSet(Integer setId, Integer wordId) {
        if (setId == null || wordId == null) {
            throw new IllegalArgumentException("Set ID và Word ID không được để trống.");
        }
        FlashcardSetVocabulary fsv = flashcardSetVocabularyRepository.findById(new FlashcardSetVocabularyId(setId, wordId))
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy liên kết từ vựng ID " + wordId + " trong bộ flashcard ID " + setId + "."));

        flashcardSetVocabularyRepository.delete(fsv);
    }

    private FlashcardSetResponse mapToFlashcardSetResponse(FlashcardSet flashcardSet, Integer currentUserId) {
        if (flashcardSet == null) {
            return null;
        }

        List<FlashcardResponse> flashcardsInSet = new ArrayList<>();
        List<FlashcardSetVocabulary> setVocabularies = flashcardSetVocabularyRepository.findByFlashcardSetSetId(flashcardSet.getSetId());

        Map<Integer, UserFlashcard> userFlashcardMap = currentUserId != null
                ? userFlashcardRepository.findByUserUserIdAndVocabularyFlashcardSetVocabulariesFlashcardSetSetId(
                        currentUserId, flashcardSet.getSetId()).stream()
                .collect(Collectors.toMap(
                        (UserFlashcard uf) -> uf.getVocabulary().getWordId(),
                        (UserFlashcard uf) -> uf
                ))
                : Map.of();

        for (FlashcardSetVocabulary fsv : setVocabularies) {
            Vocabulary vocab = fsv.getVocabulary();
            if (vocab != null) {
                UserFlashcard userFlashcard = userFlashcardMap.get(vocab.getWordId());

                if (userFlashcard != null) {
                    flashcardsInSet.add(flashcardService.mapToFlashcardResponse(userFlashcard));
                } else {
                    flashcardsInSet.add(new FlashcardResponse(
                            null,
                            currentUserId,
                            vocab.getWordId(),
                            vocab.getWord(),
                            vocab.getMeaning(),
                            vocab.getExampleSentence(),
                            vocab.getPronunciation(),
                            vocab.getAudioUrl(),
                            vocab.getImageUrl(),
                            vocab.getWritingPrompt(),
                            vocab.getDifficultyLevel(),
                            false,
                            null,
                            null,
                            0,
                            2.5
                    ));
                }
            }
        }

        return new FlashcardSetResponse(
                flashcardSet.getSetId(),
                flashcardSet.getTitle(),
                flashcardSet.getDescription(),
                flashcardSet.getCreator() != null ? flashcardSet.getCreator().getUserId() : null,
                flashcardSet.isSystemCreated(),
                flashcardSet.getCreatedAt(),
                flashcardsInSet
        );
    }

    // Phương thức này không còn được sử dụng trực tiếp để ánh xạ trong mapToFlashcardSetResponse
    // nhưng có thể vẫn được dùng ở nơi khác, nên giữ nguyên.
    private VocabularyResponse mapVocabularyToResponse(Vocabulary vocabulary) {
        if (vocabulary == null) {
            return null;
        }
        return new VocabularyResponse(
                vocabulary.getWordId(),
                vocabulary.getWord(),
                vocabulary.getMeaning(),
                vocabulary.getExampleSentence(),
                vocabulary.getPronunciation(),
                vocabulary.getAudioUrl(),
                vocabulary.getImageUrl(),
                vocabulary.getWritingPrompt(),
                vocabulary.getDifficultyLevel()
        );
    }
}