package com.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

import java.math.BigDecimal;

/**
 * 更新商品請求 DTO
 */
@Schema(description = "更新商品請求")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class UpdateProductRequest {

    @Schema(description = "商品名稱", example = "iPhone 15 Pro Max")
    @Size(min = 2, max = 200, message = "商品名稱長度必須在 2-200 個字元之間")
    private String name;

    @Schema(description = "商品價格", example = "39900.00")
    @DecimalMin(value = "0.01", message = "商品價格必須大於 0")
    @Digits(integer = 8, fraction = 2, message = "價格格式不正確")
    private BigDecimal price;

    @Schema(description = "商品描述")
    @Size(max = 2000, message = "商品描述不能超過 2000 個字元")
    private String description;

    @Schema(description = "庫存數量", example = "50")
    @Min(value = 0, message = "庫存數量不能為負數")
    private Integer stockQuantity;

    @Schema(description = "商品圖片網址")
    @Size(max = 500, message = "圖片網址不能超過 500 個字元")
    private String imageUrl;

    @Schema(description = "是否上架", example = "true")
    private Boolean active;

    @Schema(description = "分類 ID", example = "1")
    private Long categoryId;
}
