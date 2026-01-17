package com.ecommerce.dto;

import com.ecommerce.entity.Product;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 商品資料傳輸物件
 */
@Schema(description = "商品資訊")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProductDTO {

    @Schema(description = "商品 ID", example = "1")
    private Long id;

    @Schema(description = "商品名稱", example = "iPhone 15 Pro")
    private String name;

    @Schema(description = "商品價格", example = "35900.00")
    private BigDecimal price;

    @Schema(description = "商品描述", example = "最新款 Apple 智慧型手機")
    private String description;

    @Schema(description = "庫存數量", example = "100")
    private Integer stockQuantity;

    @Schema(description = "商品圖片網址")
    private String imageUrl;

    @Schema(description = "是否上架", example = "true")
    private Boolean active;

    @Schema(description = "分類名稱", example = "手機")
    private String categoryName;

    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    /**
     * 從實體轉換為 DTO
     */
    public static ProductDTO fromEntity(Product product) {
        return ProductDTO.builder()
            .id(product.getId())
            .name(product.getName())
            .price(product.getPrice())
            .description(product.getDescription())
            .stockQuantity(product.getStockQuantity())
            .imageUrl(product.getImageUrl())
            .active(product.getActive())
            .categoryName(product.getCategory() != null ? product.getCategory().getName() : null)
            .createdAt(product.getCreatedAt())
            .build();
    }
}
