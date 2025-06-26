// src/main/java/org/example/projetc_backend/dto/ProgressPageResponse.java
package org.example.projetc_backend.dto;

import java.util.List;

public record ProgressPageResponse(
        List<ProgressResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {}