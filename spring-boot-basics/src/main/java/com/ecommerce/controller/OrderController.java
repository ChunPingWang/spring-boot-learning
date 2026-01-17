package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderDTO;
import com.ecommerce.entity.OrderStatus;
import com.ecommerce.service.OrderService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * 訂單 API 控制器
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
@Tag(name = "訂單管理", description = "訂單相關操作 API")
public class OrderController {

    private final OrderService orderService;

    /**
     * 創建訂單
     */
    @Operation(summary = "創建訂單", description = "創建新的訂單，會自動扣減商品庫存")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "訂單創建成功"),
        @ApiResponse(responseCode = "400", description = "請求參數驗證失敗或庫存不足"),
        @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<OrderDTO> createOrder(
            @Parameter(description = "訂單資訊", required = true)
            @Valid @RequestBody CreateOrderRequest request) {
        OrderDTO created = orderService.createOrder(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 根據 ID 查詢訂單
     */
    @Operation(summary = "查詢訂單", description = "根據訂單 ID 查詢訂單詳情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查詢成功"),
        @ApiResponse(responseCode = "404", description = "訂單不存在")
    })
    @GetMapping("/{id}")
    public ResponseEntity<OrderDTO> getOrderById(
            @Parameter(description = "訂單 ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.getOrderById(id));
    }

    /**
     * 根據訂單編號查詢訂單
     */
    @Operation(summary = "查詢訂單（依編號）", description = "根據訂單編號查詢訂單詳情")
    @GetMapping("/number/{orderNumber}")
    public ResponseEntity<OrderDTO> getOrderByNumber(
            @Parameter(description = "訂單編號", required = true, example = "ORD-20240101-ABCD1234")
            @PathVariable String orderNumber) {
        return ResponseEntity.ok(orderService.getOrderByNumber(orderNumber));
    }

    /**
     * 查詢客戶訂單
     */
    @Operation(summary = "查詢客戶訂單", description = "根據客戶 Email 查詢其所有訂單")
    @GetMapping("/customer")
    public ResponseEntity<Page<OrderDTO>> getOrdersByCustomer(
            @Parameter(description = "客戶 Email", required = true, example = "customer@example.com")
            @RequestParam String email,
            @Parameter(description = "分頁參數")
            @PageableDefault(size = 10) Pageable pageable) {
        return ResponseEntity.ok(orderService.getOrdersByCustomer(email, pageable));
    }

    /**
     * 更新訂單狀態
     *
     * @PatchMapping: 處理 HTTP PATCH 請求（部分更新）
     */
    @Operation(summary = "更新訂單狀態", description = "更新訂單的狀態")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "404", description = "訂單不存在")
    })
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrderDTO> updateOrderStatus(
            @Parameter(description = "訂單 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "新狀態", required = true, example = "PAID")
            @RequestParam OrderStatus status) {
        return ResponseEntity.ok(orderService.updateOrderStatus(id, status));
    }

    /**
     * 取消訂單
     */
    @Operation(summary = "取消訂單", description = "取消訂單並恢復商品庫存")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "取消成功"),
        @ApiResponse(responseCode = "400", description = "訂單無法取消（已出貨等）"),
        @ApiResponse(responseCode = "404", description = "訂單不存在")
    })
    @PostMapping("/{id}/cancel")
    public ResponseEntity<OrderDTO> cancelOrder(
            @Parameter(description = "訂單 ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(orderService.cancelOrder(id));
    }
}
