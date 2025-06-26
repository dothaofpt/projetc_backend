package org.example.projetc_backend.service;

import org.example.projetc_backend.dto.LearningMaterialRequest;
import org.example.projetc_backend.dto.LearningMaterialResponse;
import org.example.projetc_backend.dto.LearningMaterialSearchRequest;
import org.example.projetc_backend.entity.LearningMaterial;
import org.example.projetc_backend.entity.Lesson;
import org.example.projetc_backend.repository.LearningMaterialRepository;
import org.example.projetc_backend.repository.LessonRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional; // Thêm import này

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

    /**
     * Tạo một tài liệu học tập mới.
     * Đảm bảo rằng bài học liên quan tồn tại và URL tài liệu là duy nhất.
     *
     * @param request DTO chứa thông tin tài liệu học tập.
     * @return LearningMaterialResponse của tài liệu đã tạo.
     * @throws IllegalArgumentException Nếu dữ liệu request không hợp lệ, bài học không tồn tại, hoặc URL đã trùng.
     */
    @Transactional // Đảm bảo giao dịch cho thao tác ghi
    public LearningMaterialResponse createLearningMaterial(LearningMaterialRequest request) {
        // Kiểm tra tính hợp lệ cơ bản của request
        if (request == null || request.lessonId() == null || request.materialType() == null ||
                request.materialUrl() == null || request.materialUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Lesson ID, Material Type, và Material URL là bắt buộc.");
        }

        // Tìm bài học liên quan
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        // Kiểm tra URL tài liệu có bị trùng lặp không
        // `findByMaterialUrl` đã tồn tại trong repository
        if (learningMaterialRepository.findByMaterialUrl(request.materialUrl()).isPresent()) {
            throw new IllegalArgumentException("URL tài liệu đã tồn tại: " + request.materialUrl());
        }

        // Ánh xạ từ DTO sang Entity
        LearningMaterial material = new LearningMaterial();
        material.setLesson(lesson);
        material.setMaterialType(request.materialType());
        material.setMaterialUrl(request.materialUrl().trim());
        material.setDescription(request.description() != null ? request.description().trim() : null); // Trim hoặc để null nếu rỗng
        material.setTranscriptText(request.transcriptText() != null ? request.transcriptText().trim() : null); // Trim hoặc để null nếu rỗng

        // Lưu tài liệu mới vào cơ sở dữ liệu
        material = learningMaterialRepository.save(material);
        return mapToLearningMaterialResponse(material);
    }

    /**
     * Lấy thông tin chi tiết của một tài liệu học tập theo ID.
     *
     * @param materialId ID của tài liệu.
     * @return LearningMaterialResponse của tài liệu.
     * @throws IllegalArgumentException Nếu Material ID trống hoặc không tìm thấy tài liệu.
     */
    @Transactional(readOnly = true) // Đảm bảo chỉ đọc cho thao tác truy vấn
    public LearningMaterialResponse getLearningMaterialById(Integer materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID không được để trống.");
        }
        LearningMaterial material = learningMaterialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu với ID: " + materialId));
        return mapToLearningMaterialResponse(material);
    }

    /**
     * Lấy tất cả các tài liệu học tập liên quan đến một bài học cụ thể.
     *
     * @param lessonId ID của bài học.
     * @return Danh sách LearningMaterialResponse.
     * @throws IllegalArgumentException Nếu Lesson ID trống.
     */
    @Transactional(readOnly = true) // Đảm bảo chỉ đọc cho thao tác truy vấn
    public List<LearningMaterialResponse> getLearningMaterialsByLessonId(Integer lessonId) {
        if (lessonId == null) {
            throw new IllegalArgumentException("Lesson ID không được để trống.");
        }
        // Kiểm tra xem lesson có tồn tại không trước khi tìm materials
        if (!lessonRepository.existsById(lessonId)) {
            throw new IllegalArgumentException("Không tìm thấy bài học với ID: " + lessonId);
        }

        return learningMaterialRepository.findByLessonLessonId(lessonId).stream()
                .map(this::mapToLearningMaterialResponse)
                .collect(Collectors.toList());
    }

    /**
     * Cập nhật thông tin của một tài liệu học tập hiện có.
     * Đảm bảo rằng tài liệu và bài học liên quan tồn tại, và URL tài liệu là duy nhất (trừ chính nó).
     *
     * @param materialId ID của tài liệu cần cập nhật.
     * @param request DTO chứa thông tin cập nhật.
     * @return LearningMaterialResponse của tài liệu đã cập nhật.
     * @throws IllegalArgumentException Nếu dữ liệu request không hợp lệ, tài liệu/bài học không tồn tại, hoặc URL đã trùng.
     */
    @Transactional // Đảm bảo giao dịch cho thao tác ghi
    public LearningMaterialResponse updateLearningMaterial(Integer materialId, LearningMaterialRequest request) {
        // Kiểm tra tính hợp lệ cơ bản của request
        if (materialId == null || request == null || request.lessonId() == null || request.materialType() == null ||
                request.materialUrl() == null || request.materialUrl().trim().isEmpty()) {
            throw new IllegalArgumentException("Material ID, Lesson ID, Material Type, và Material URL là bắt buộc.");
        }

        // Tìm tài liệu hiện có
        LearningMaterial material = learningMaterialRepository.findById(materialId)
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy tài liệu với ID: " + materialId));

        // Tìm bài học liên quan
        Lesson lesson = lessonRepository.findById(request.lessonId())
                .orElseThrow(() -> new IllegalArgumentException("Không tìm thấy bài học với ID: " + request.lessonId()));

        // Kiểm tra URL tài liệu có bị trùng lặp với các tài liệu khác (không phải chính nó)
        learningMaterialRepository.findByMaterialUrl(request.materialUrl())
                .filter(existing -> !existing.getMaterialId().equals(materialId)) // Loại trừ tài liệu hiện tại
                .ifPresent(existing -> {
                    throw new IllegalArgumentException("URL tài liệu đã tồn tại: " + request.materialUrl());
                });

        // Cập nhật các trường từ DTO
        material.setLesson(lesson); // Cập nhật Lesson nếu có thay đổi
        material.setMaterialType(request.materialType());
        material.setMaterialUrl(request.materialUrl().trim());
        material.setDescription(request.description() != null ? request.description().trim() : null); // Cập nhật hoặc để null
        material.setTranscriptText(request.transcriptText() != null ? request.transcriptText().trim() : null); // Cập nhật hoặc để null

        // Lưu thay đổi
        material = learningMaterialRepository.save(material);
        return mapToLearningMaterialResponse(material);
    }

    /**
     * Xóa một tài liệu học tập theo ID.
     *
     * @param materialId ID của tài liệu cần xóa.
     * @throws IllegalArgumentException Nếu Material ID trống hoặc không tìm thấy tài liệu.
     */
    @Transactional // Đảm bảo giao dịch cho thao tác ghi
    public void deleteLearningMaterial(Integer materialId) {
        if (materialId == null) {
            throw new IllegalArgumentException("Material ID không được để trống.");
        }
        if (!learningMaterialRepository.existsById(materialId)) {
            throw new IllegalArgumentException("Không tìm thấy tài liệu với ID: " + materialId);
        }
        learningMaterialRepository.deleteById(materialId);
    }

    /**
     * Tìm kiếm và phân trang các tài liệu học tập dựa trên các tiêu chí.
     *
     * @param request DTO chứa các tiêu chí tìm kiếm và thông tin phân trang/sắp xếp.
     * @return Trang (Page) các LearningMaterialResponse.
     * @throws IllegalArgumentException Nếu Search request trống.
     */
    @Transactional(readOnly = true) // Đảm bảo chỉ đọc cho thao tác truy vấn
    public Page<LearningMaterialResponse> searchLearningMaterials(LearningMaterialSearchRequest request) {
        if (request == null) {
            throw new IllegalArgumentException("Search request không được để trống.");
        }

        // Xử lý sắp xếp
        String sortBy = request.sortBy();
        // Cần đảm bảo `sortBy` khớp với tên thuộc tính trong entity `LearningMaterial`
        // Ví dụ: `lesson.lessonId` nếu muốn sắp xếp theo ID của lesson
        if (!List.of("materialId", "materialType", "materialUrl", "description", "transcriptText", "lesson.lessonId").contains(sortBy)) {
            sortBy = "materialId"; // Mặc định sắp xếp theo materialId nếu không hợp lệ
        }

        Sort sort = Sort.by(request.sortDir().equalsIgnoreCase("DESC") ? Sort.Direction.DESC : Sort.Direction.ASC, sortBy);
        PageRequest pageable = PageRequest.of(request.page(), request.size(), sort);

        // Lời gọi phương thức tìm kiếm trong repository
        Page<LearningMaterial> materials = learningMaterialRepository.searchMaterials(
                request.lessonId(),
                request.materialType(),
                request.description(), // description đã có trong DTO và Repository query
                pageable
        );

        // Ánh xạ kết quả sang DTO Page
        return materials.map(this::mapToLearningMaterialResponse);
    }

    /**
     * Phương thức ánh xạ từ LearningMaterial Entity sang LearningMaterialResponse DTO.
     *
     * @param material Entity LearningMaterial.
     * @return LearningMaterialResponse DTO.
     */
    private LearningMaterialResponse mapToLearningMaterialResponse(LearningMaterial material) {
        if (material == null) {
            return null;
        }
        return new LearningMaterialResponse(
                material.getMaterialId(),
                material.getLesson() != null ? material.getLesson().getLessonId() : null, // Đảm bảo Lesson không null
                material.getMaterialType(),
                material.getMaterialUrl(),
                material.getDescription(),
                material.getTranscriptText()
        );
    }
}