package org.example.projetc_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.projetc_backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.validation.ObjectError;
import org.springframework.web.bind.MethodArgumentNotValidException;

import java.util.stream.Collectors;

@RestControllerAdvice
@Tag(name = "Error Handling", description = "Xử lý ngoại lệ toàn cục cho tất cả API")
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @Operation(summary = "Xử lý ngoại lệ IllegalArgumentException", description = "Xử lý tất cả các trường hợp IllegalArgumentException và trả về phản hồi lỗi phù hợp")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ do tham số đầu vào không hợp lệ")
    })
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    @Operation(summary = "Xử lý ngoại lệ valid dữ liệu", description = "Xử lý tất cả các ngoại lệ MethodArgumentNotValidException khi dữ liệu DTO không hợp lệ")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Yêu cầu không hợp lệ do lỗi valid dữ liệu")
    })
    public ResponseEntity<ErrorResponse> handleValidationExceptions(MethodArgumentNotValidException ex) {
        String errorMessage = ex.getBindingResult().getAllErrors().stream()
                .map(ObjectError::getDefaultMessage)
                .collect(Collectors.joining(", "));
        return ResponseEntity.badRequest().body(new ErrorResponse(errorMessage));
    }

    @ExceptionHandler(AccessDeniedException.class)
    @Operation(summary = "Xử lý ngoại lệ truy cập bị từ chối", description = "Xử lý tất cả các trường hợp AccessDeniedException và trả về phản hồi lỗi phù hợp")
    @ApiResponses({
            @ApiResponse(responseCode = "403", description = "Truy cập bị cấm do không đủ quyền")
    })
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(AccessDeniedException e) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN)
                .body(new ErrorResponse("Bạn không có quyền truy cập API này"));
    }

    @ExceptionHandler(Exception.class)
    @Operation(summary = "Xử lý ngoại lệ chung", description = "Xử lý tất cả các ngoại lệ không mong muốn và trả về phản hồi lỗi phù hợp")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Lỗi máy chủ nội bộ do tình trạng không mong muốn")
    })
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Đã xảy ra lỗi không mong muốn: " + e.getMessage()));
    }
}