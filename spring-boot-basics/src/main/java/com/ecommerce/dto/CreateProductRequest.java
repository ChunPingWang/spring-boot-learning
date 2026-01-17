package com.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 創建商品請求 DTO
 *
 * 展示各種驗證註解的使用
 */
@Schema(description = "創建商品請求")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateProductRequest {

    /**
     * @NotBlank: 不能為 null 且不能為空白字串
     * @Size: 限制字串長度
     */
    @Schema(description = "商品名稱", example = "iPhone 15 Pro", required = true)
    @NotBlank(message = "商品名稱不能為空")
    @Size(min = 2, max = 200, message = "商品名稱長度必須在 2-200 個字元之間")
    private String name;

    /**
     * @NotNull: 不能為 null
     * @DecimalMin: 最小值
     * @Digits: 限制整數位數和小數位數
     */
    @Schema(description = "商品價格", example = "35900.00", required = true)
    @NotNull(message = "商品價格不能為空")
    @DecimalMin(value = "0.01", message = "商品價格必須大於 0")
    @Digits(integer = 8, fraction = 2, message = "價格格式不正確")
    private BigDecimal price;

    @Schema(description = "商品描述", example = "最新款 Apple 智慧型手機")
    @Size(max = 2000, message = "商品描述不能超過 2000 個字元")
    private String description;

    /**
     * @Min: 最小值
     */
    @Schema(description = "初始庫存數量", example = "100")
    @Min(value = 0, message = "庫存數量不能為負數")
    @Builder.Default
    private Integer stockQuantity = 0;

    @Schema(description = "商品圖片網址")
    @Size(max = 500, message = "圖片網址不能超過 500 個字元")
    private String imageUrl;

    @Schema(description = "分類 ID", example = "1")
    private Long categoryId;
}
