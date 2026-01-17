package com.ecommerce.dto;

import com.ecommerce.entity.OrderItem;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;

/**
 * 訂單項目資料傳輸物件
 */
@Schema(description = "訂單項目資訊")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemDTO {

    @Schema(description = "項目 ID", example = "1")
    private Long id;

    @Schema(description = "商品 ID", example = "1")
    private Long productId;

    @Schema(description = "商品名稱", example = "iPhone 15 Pro")
    private String productName;

    @Schema(description = "購買數量", example = "2")
    private Integer quantity;

    @Schema(description = "單價", example = "35900.00")
    private BigDecimal unitPrice;

    @Schema(description = "小計金額", example = "71800.00")
    private BigDecimal subtotal;

    /**
     * 從實體轉換為 DTO
     */
    public static OrderItemDTO fromEntity(OrderItem item) {
        return OrderItemDTO.builder()
            .id(item.getId())
            .productId(item.getProduct().getId())
            .productName(item.getProductName())
            .quantity(item.getQuantity())
            .unitPrice(item.getUnitPrice())
            .subtotal(item.getSubtotal())
            .build();
    }
}
