package com.ecommerce.controller;

import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.UpdateProductRequest;
import com.ecommerce.service.ProductService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
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

import java.util.List;

/**
 * 商品 API 控制器
 *
 * @RestController: 組合 @Controller 和 @ResponseBody，自動將返回值轉為 JSON
 * @RequestMapping: 指定基礎 URL 路徑
 * @Tag: Swagger 標籤，用於 API 分組
 */
@RestController
@RequestMapping("/api/v1/products")
@RequiredArgsConstructor
@Tag(name = "商品管理", description = "商品 CRUD 操作 API")
public class ProductController {

    private final ProductService productService;

    /**
     * 查詢所有商品（分頁）
     *
     * @GetMapping: 處理 HTTP GET 請求
     * @PageableDefault: 設定分頁預設值
     */
    @Operation(summary = "查詢所有商品", description = "分頁查詢所有上架中的商品")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查詢成功")
    })
    @GetMapping
    public ResponseEntity<Page<ProductDTO>> getAllProducts(
            @Parameter(description = "分頁參數")
            @PageableDefault(size = 10, sort = "createdAt") Pageable pageable) {
        return ResponseEntity.ok(productService.getAllProducts(pageable));
    }

    /**
     * 根據 ID 查詢商品
     *
     * @PathVariable: 從 URL 路徑中提取變數
     */
    @Operation(summary = "查詢單一商品", description = "根據商品 ID 查詢商品詳情")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "查詢成功"),
        @ApiResponse(responseCode = "404", description = "商品不存在",
            content = @Content(schema = @Schema(ref = "#/components/schemas/ErrorResponse")))
    })
    @GetMapping("/{id}")
    public ResponseEntity<ProductDTO> getProductById(
            @Parameter(description = "商品 ID", required = true, example = "1")
            @PathVariable Long id) {
        return ResponseEntity.ok(productService.getProductById(id));
    }

    /**
     * 搜尋商品
     *
     * @RequestParam: 獲取 URL 查詢參數
     */
    @Operation(summary = "搜尋商品", description = "根據關鍵字搜尋商品名稱")
    @GetMapping("/search")
    public ResponseEntity<List<ProductDTO>> searchProducts(
            @Parameter(description = "搜尋關鍵字", required = true, example = "iPhone")
            @RequestParam String keyword) {
        return ResponseEntity.ok(productService.searchProducts(keyword));
    }

    /**
     * 根據分類查詢商品
     */
    @Operation(summary = "查詢分類商品", description = "根據分類 ID 查詢該分類下的所有商品")
    @GetMapping("/category/{categoryId}")
    public ResponseEntity<List<ProductDTO>> getProductsByCategory(
            @Parameter(description = "分類 ID", required = true, example = "1")
            @PathVariable Long categoryId) {
        return ResponseEntity.ok(productService.getProductsByCategory(categoryId));
    }

    /**
     * 創建商品
     *
     * @PostMapping: 處理 HTTP POST 請求
     * @Valid: 觸發 RequestBody 的驗證
     * @RequestBody: 將 HTTP 請求體綁定到參數
     * @ResponseStatus: 指定成功時的 HTTP 狀態碼
     */
    @Operation(summary = "創建商品", description = "創建新的商品")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "201", description = "創建成功"),
        @ApiResponse(responseCode = "400", description = "請求參數驗證失敗")
    })
    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public ResponseEntity<ProductDTO> createProduct(
            @Parameter(description = "商品資訊", required = true)
            @Valid @RequestBody CreateProductRequest request) {
        ProductDTO created = productService.createProduct(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    /**
     * 更新商品
     *
     * @PutMapping: 處理 HTTP PUT 請求
     */
    @Operation(summary = "更新商品", description = "更新指定商品的資訊")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "200", description = "更新成功"),
        @ApiResponse(responseCode = "404", description = "商品不存在"),
        @ApiResponse(responseCode = "400", description = "請求參數驗證失敗")
    })
    @PutMapping("/{id}")
    public ResponseEntity<ProductDTO> updateProduct(
            @Parameter(description = "商品 ID", required = true, example = "1")
            @PathVariable Long id,
            @Parameter(description = "更新資訊", required = true)
            @Valid @RequestBody UpdateProductRequest request) {
        return ResponseEntity.ok(productService.updateProduct(id, request));
    }

    /**
     * 刪除商品
     *
     * @DeleteMapping: 處理 HTTP DELETE 請求
     */
    @Operation(summary = "刪除商品", description = "停用指定的商品（軟刪除）")
    @ApiResponses(value = {
        @ApiResponse(responseCode = "204", description = "刪除成功"),
        @ApiResponse(responseCode = "404", description = "商品不存在")
    })
    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public ResponseEntity<Void> deleteProduct(
            @Parameter(description = "商品 ID", required = true, example = "1")
            @PathVariable Long id) {
        productService.deleteProduct(id);
        return ResponseEntity.noContent().build();
    }

    /**
     * 查詢低庫存商品
     */
    @Operation(summary = "查詢低庫存商品", description = "查詢庫存低於指定閾值的商品")
    @GetMapping("/low-stock")
    public ResponseEntity<List<ProductDTO>> getLowStockProducts(
            @Parameter(description = "庫存閾值", example = "10")
            @RequestParam(defaultValue = "10") int threshold) {
        return ResponseEntity.ok(productService.getLowStockProducts(threshold));
    }
}
