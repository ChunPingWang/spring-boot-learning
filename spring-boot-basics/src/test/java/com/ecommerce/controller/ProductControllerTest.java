package com.ecommerce.controller;

import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.UpdateProductRequest;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.service.ProductService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.math.BigDecimal;
import java.util.List;

import static org.hamcrest.Matchers.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 商品控制器測試
 *
 * @WebMvcTest: Web 層切片測試
 * - 只載入 Web 相關組件（Controller, Filter, Advice）
 * - 不啟動完整的 Spring Context
 * - 使用 MockMvc 進行 HTTP 請求模擬
 */
@WebMvcTest(controllers = ProductController.class,
    excludeAutoConfiguration = {
        org.springdoc.core.configuration.SpringDocConfiguration.class,
        org.springdoc.webmvc.core.configuration.SpringDocWebMvcConfiguration.class
    })
@DisplayName("商品 API 測試")
class ProductControllerTest {

    /**
     * MockMvc: Spring 提供的 MVC 測試工具
     * 用於模擬 HTTP 請求和驗證響應
     */
    @Autowired
    private MockMvc mockMvc;

    /**
     * ObjectMapper: JSON 序列化/反序列化工具
     */
    @Autowired
    private ObjectMapper objectMapper;

    /**
     * @MockBean: 創建 Mock 並注入到 Spring Context
     */
    @MockBean
    private ProductService productService;

    @Nested
    @DisplayName("GET /api/v1/products")
    class GetAllProductsTests {

        @Test
        @DisplayName("應返回商品列表（分頁）")
        void shouldReturnPageOfProducts() throws Exception {
            // Arrange
            ProductDTO product1 = ProductDTO.builder()
                .id(1L).name("商品A").price(new BigDecimal("100")).build();
            ProductDTO product2 = ProductDTO.builder()
                .id(2L).name("商品B").price(new BigDecimal("200")).build();

            when(productService.getAllProducts(any(Pageable.class)))
                .thenReturn(new PageImpl<>(List.of(product1, product2)));

            // Act & Assert
            mockMvc.perform(get("/api/v1/products")
                    .param("page", "0")
                    .param("size", "10"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.content", hasSize(2)))
                .andExpect(jsonPath("$.content[0].name").value("商品A"))
                .andExpect(jsonPath("$.content[1].name").value("商品B"));
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/{id}")
    class GetProductByIdTests {

        @Test
        @DisplayName("商品存在時應返回商品詳情")
        void whenProductExists_shouldReturnProduct() throws Exception {
            // Arrange
            ProductDTO product = ProductDTO.builder()
                .id(1L)
                .name("iPhone 15 Pro")
                .price(new BigDecimal("35900"))
                .stockQuantity(50)
                .active(true)
                .build();

            when(productService.getProductById(1L)).thenReturn(product);

            // Act & Assert
            mockMvc.perform(get("/api/v1/products/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.price").value(35900));
        }

        @Test
        @DisplayName("商品不存在時應返回 404")
        void whenProductNotExists_shouldReturn404() throws Exception {
            // Arrange
            when(productService.getProductById(999L))
                .thenThrow(new ResourceNotFoundException("商品", "id", 999L));

            // Act & Assert
            mockMvc.perform(get("/api/v1/products/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(containsString("商品")));
        }
    }

    @Nested
    @DisplayName("POST /api/v1/products")
    class CreateProductTests {

        @Test
        @DisplayName("有效請求應創建商品並返回 201")
        void withValidRequest_shouldCreateProductAndReturn201() throws Exception {
            // Arrange
            CreateProductRequest request = CreateProductRequest.builder()
                .name("新商品")
                .price(new BigDecimal("999.00"))
                .stockQuantity(50)
                .build();

            ProductDTO created = ProductDTO.builder()
                .id(1L)
                .name("新商品")
                .price(new BigDecimal("999.00"))
                .stockQuantity(50)
                .active(true)
                .build();

            when(productService.createProduct(any(CreateProductRequest.class))).thenReturn(created);

            // Act & Assert
            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("新商品"))
                .andExpect(jsonPath("$.price").value(999.00));

            verify(productService).createProduct(any(CreateProductRequest.class));
        }

        @Test
        @DisplayName("名稱為空時應返回 400 驗證錯誤")
        void withEmptyName_shouldReturn400() throws Exception {
            // Arrange
            CreateProductRequest request = CreateProductRequest.builder()
                .name("")  // 空名稱
                .price(new BigDecimal("999.00"))
                .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists());

            // 驗證 service 沒有被呼叫
            verify(productService, never()).createProduct(any());
        }

        @Test
        @DisplayName("價格為負數時應返回 400 驗證錯誤")
        void withNegativePrice_shouldReturn400() throws Exception {
            // Arrange
            CreateProductRequest request = CreateProductRequest.builder()
                .name("商品")
                .price(new BigDecimal("-100"))  // 負數價格
                .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.price").exists());
        }

        @Test
        @DisplayName("缺少必填欄位時應返回 400 並列出所有錯誤")
        void withMissingRequiredFields_shouldReturn400WithAllErrors() throws Exception {
            // Arrange: 完全空的請求
            CreateProductRequest request = new CreateProductRequest();

            // Act & Assert
            mockMvc.perform(post("/api/v1/products")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.name").exists())
                .andExpect(jsonPath("$.errors.price").exists());
        }
    }

    @Nested
    @DisplayName("PUT /api/v1/products/{id}")
    class UpdateProductTests {

        @Test
        @DisplayName("有效請求應更新商品")
        void withValidRequest_shouldUpdateProduct() throws Exception {
            // Arrange
            UpdateProductRequest request = UpdateProductRequest.builder()
                .name("更新後的名稱")
                .price(new BigDecimal("1999.00"))
                .build();

            ProductDTO updated = ProductDTO.builder()
                .id(1L)
                .name("更新後的名稱")
                .price(new BigDecimal("1999.00"))
                .build();

            when(productService.updateProduct(eq(1L), any(UpdateProductRequest.class)))
                .thenReturn(updated);

            // Act & Assert
            mockMvc.perform(put("/api/v1/products/1")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("更新後的名稱"))
                .andExpect(jsonPath("$.price").value(1999.00));
        }

        @Test
        @DisplayName("更新不存在的商品應返回 404")
        void whenProductNotExists_shouldReturn404() throws Exception {
            // Arrange
            UpdateProductRequest request = UpdateProductRequest.builder()
                .name("更新")
                .build();

            when(productService.updateProduct(eq(999L), any(UpdateProductRequest.class)))
                .thenThrow(new ResourceNotFoundException("商品", "id", 999L));

            // Act & Assert
            mockMvc.perform(put("/api/v1/products/999")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/products/{id}")
    class DeleteProductTests {

        @Test
        @DisplayName("刪除存在的商品應返回 204")
        void whenProductExists_shouldReturn204() throws Exception {
            // Arrange
            doNothing().when(productService).deleteProduct(1L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/products/1"))
                .andExpect(status().isNoContent());

            verify(productService).deleteProduct(1L);
        }

        @Test
        @DisplayName("刪除不存在的商品應返回 404")
        void whenProductNotExists_shouldReturn404() throws Exception {
            // Arrange
            doThrow(new ResourceNotFoundException("商品", "id", 999L))
                .when(productService).deleteProduct(999L);

            // Act & Assert
            mockMvc.perform(delete("/api/v1/products/999"))
                .andExpect(status().isNotFound());
        }
    }

    @Nested
    @DisplayName("GET /api/v1/products/search")
    class SearchProductsTests {

        @Test
        @DisplayName("搜尋應返回匹配的商品")
        void shouldReturnMatchingProducts() throws Exception {
            // Arrange
            ProductDTO product = ProductDTO.builder()
                .id(1L).name("iPhone 15 Pro").build();

            when(productService.searchProducts("iPhone")).thenReturn(List.of(product));

            // Act & Assert
            mockMvc.perform(get("/api/v1/products/search")
                    .param("keyword", "iPhone"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name").value("iPhone 15 Pro"));
        }
    }
}
