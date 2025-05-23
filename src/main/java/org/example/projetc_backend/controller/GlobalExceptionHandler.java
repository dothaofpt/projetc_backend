package org.example.projetc_backend.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.example.projetc_backend.dto.ErrorResponse;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice
@Tag(name = "Error Handling", description = "Global exception handling for all APIs")
public class GlobalExceptionHandler {

    @ExceptionHandler(IllegalArgumentException.class)
    @Operation(summary = "Handle illegal argument exceptions", description = "Handles all IllegalArgumentException instances and returns a proper error response")
    @ApiResponses({
            @ApiResponse(responseCode = "400", description = "Bad request due to invalid input parameters")
    })
    public ResponseEntity<ErrorResponse> handleIllegalArgumentException(IllegalArgumentException e) {
        return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
    }

    @ExceptionHandler(Exception.class)
    @Operation(summary = "Handle general exceptions", description = "Handles all unexpected exceptions and returns a proper error response")
    @ApiResponses({
            @ApiResponse(responseCode = "500", description = "Internal server error due to unexpected condition")
    })
    public ResponseEntity<ErrorResponse> handleGeneralException(Exception e) {
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                .body(new ErrorResponse("Đã xảy ra lỗi không mong muốn"));
    }
}