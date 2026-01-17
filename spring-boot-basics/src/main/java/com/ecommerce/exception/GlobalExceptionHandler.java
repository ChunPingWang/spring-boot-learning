package com.ecommerce.exception;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * 全域異常處理器
 *
 * @RestControllerAdvice: 組合註解，包含 @ControllerAdvice 和 @ResponseBody
 * - 集中處理所有 Controller 拋出的異常
 * - 自動將返回值轉換為 JSON
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    /**
     * 處理資源不存在異常
     *
     * @ExceptionHandler: 指定處理的異常類型
     */
    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<ErrorResponse> handleResourceNotFound(ResourceNotFoundException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.NOT_FOUND.value())
            .error("Not Found")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(error);
    }

    /**
     * 處理庫存不足異常
     */
    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponse> handleInsufficientStock(InsufficientStockException ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Bad Request")
            .message(ex.getMessage())
            .build();
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(error);
    }

    /**
     * 處理驗證異常
     */
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<ValidationErrorResponse> handleValidationException(
            MethodArgumentNotValidException ex) {

        Map<String, String> errors = new HashMap<>();
        ex.getBindingResult().getAllErrors().forEach(error -> {
            String fieldName = ((FieldError) error).getField();
            String errorMessage = error.getDefaultMessage();
            errors.put(fieldName, errorMessage);
        });

        ValidationErrorResponse response = ValidationErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.BAD_REQUEST.value())
            .error("Validation Failed")
            .message("輸入資料驗證失敗")
            .errors(errors)
            .build();

        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(response);
    }

    /**
     * 處理其他未捕獲的異常
     */
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponse> handleGenericException(Exception ex) {
        ErrorResponse error = ErrorResponse.builder()
            .timestamp(LocalDateTime.now())
            .status(HttpStatus.INTERNAL_SERVER_ERROR.value())
            .error("Internal Server Error")
            .message("伺服器發生錯誤，請稍後再試")
            .build();
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(error);
    }

    /**
     * 錯誤回應 DTO
     */
    @Schema(description = "錯誤回應")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ErrorResponse {

        @Schema(description = "錯誤發生時間")
        private LocalDateTime timestamp;

        @Schema(description = "HTTP 狀態碼", example = "404")
        private int status;

        @Schema(description = "錯誤類型", example = "Not Found")
        private String error;

        @Schema(description = "錯誤訊息", example = "商品不存在: id = '999'")
        private String message;
    }

    /**
     * 驗證錯誤回應 DTO
     */
    @Schema(description = "驗證錯誤回應")
    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    @Builder
    public static class ValidationErrorResponse {

        @Schema(description = "錯誤發生時間")
        private LocalDateTime timestamp;

        @Schema(description = "HTTP 狀態碼", example = "400")
        private int status;

        @Schema(description = "錯誤類型", example = "Validation Failed")
        private String error;

        @Schema(description = "錯誤訊息", example = "輸入資料驗證失敗")
        private String message;

        @Schema(description = "欄位驗證錯誤明細")
        private Map<String, String> errors;
    }
}
