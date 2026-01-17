package com.ecommerce.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.*;

import java.util.List;

/**
 * 創建訂單請求 DTO
 *
 * 展示嵌套驗證 (@Valid) 的使用
 */
@Schema(description = "創建訂單請求")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateOrderRequest {

    @Schema(description = "客戶姓名", example = "王小明", required = true)
    @NotBlank(message = "客戶姓名不能為空")
    @Size(max = 100, message = "客戶姓名不能超過 100 個字元")
    private String customerName;

    @Schema(description = "客戶 Email", example = "customer@example.com", required = true)
    @NotBlank(message = "客戶 Email 不能為空")
    @Email(message = "請輸入有效的 Email 地址")
    private String customerEmail;

    @Schema(description = "配送地址", example = "台北市信義區信義路五段7號", required = true)
    @NotBlank(message = "配送地址不能為空")
    @Size(max = 500, message = "配送地址不能超過 500 個字元")
    private String shippingAddress;

    /**
     * @Valid: 觸發嵌套物件的驗證
     * @NotEmpty: 集合不能為空
     * @Size: 限制集合大小
     */
    @Schema(description = "訂單項目清單", required = true)
    @Valid
    @NotEmpty(message = "訂單項目不能為空")
    @Size(max = 50, message = "單筆訂單最多 50 件商品")
    private List<OrderItemRequest> items;
}
