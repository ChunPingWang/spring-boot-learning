package com.ecommerce.dto;

import com.ecommerce.entity.Order;
import com.ecommerce.entity.OrderStatus;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 訂單資料傳輸物件
 */
@Schema(description = "訂單資訊")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderDTO {

    @Schema(description = "訂單 ID", example = "1")
    private Long id;

    @Schema(description = "訂單編號", example = "ORD-20240101-001")
    private String orderNumber;

    @Schema(description = "客戶姓名", example = "王小明")
    private String customerName;

    @Schema(description = "客戶 Email", example = "customer@example.com")
    private String customerEmail;

    @Schema(description = "配送地址")
    private String shippingAddress;

    @Schema(description = "訂單狀態", example = "PENDING")
    private OrderStatus status;

    @Schema(description = "訂單總金額", example = "71800.00")
    private BigDecimal totalAmount;

    @Schema(description = "訂單項目清單")
    private List<OrderItemDTO> items;

    @Schema(description = "建立時間")
    private LocalDateTime createdAt;

    /**
     * 從實體轉換為 DTO
     */
    public static OrderDTO fromEntity(Order order) {
        return OrderDTO.builder()
            .id(order.getId())
            .orderNumber(order.getOrderNumber())
            .customerName(order.getCustomerName())
            .customerEmail(order.getCustomerEmail())
            .shippingAddress(order.getShippingAddress())
            .status(order.getStatus())
            .totalAmount(order.getTotalAmount())
            .items(order.getItems().stream()
                .map(OrderItemDTO::fromEntity)
                .toList())
            .createdAt(order.getCreatedAt())
            .build();
    }
}
