// src/main/java/org/example/projetc_backend/dto/UserPageResponse.java
package org.example.projetc_backend.dto;

import java.util.List;

public record UserPageResponse(
        List<UserResponse> content,
        long totalElements,
        int totalPages,
        int currentPage,
        int pageSize
) {}