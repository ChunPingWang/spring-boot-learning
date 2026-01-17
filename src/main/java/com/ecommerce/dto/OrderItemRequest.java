package com.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.*;
import lombok.*;

/**
 * 訂單項目請求 DTO
 */
@Schema(description = "訂單項目")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderItemRequest {

    @Schema(description = "商品 ID", example = "1", required = true)
    @NotNull(message = "商品 ID 不能為空")
    @Positive(message = "商品 ID 必須為正數")
    private Long productId;

    @Schema(description = "購買數量", example = "2", required = true)
    @NotNull(message = "購買數量不能為空")
    @Min(value = 1, message = "購買數量至少為 1")
    @Max(value = 99, message = "單項商品數量不能超過 99")
    private Integer quantity;
}
