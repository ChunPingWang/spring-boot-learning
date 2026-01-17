package com.ecommerce.controller;

import com.ecommerce.dto.CreateOrderRequest;
import com.ecommerce.dto.OrderItemRequest;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.OrderRepository;
import com.ecommerce.repository.ProductRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * 訂單 API 整合測試
 *
 * @SpringBootTest: 載入完整應用程式上下文
 * @AutoConfigureMockMvc: 自動配置 MockMvc
 * @Transactional: 每個測試方法執行後自動回滾
 *
 * 整合測試特點：
 * - 使用真實的資料庫（H2）
 * - 測試完整的請求流程
 * - 驗證各組件的協作
 */
@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
@DisplayName("訂單 API 整合測試")
class OrderControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    @Autowired
    private OrderRepository orderRepository;

    private Product testProduct1;
    private Product testProduct2;

    @BeforeEach
    void setUp() {
        // 清理資料
        orderRepository.deleteAll();
        productRepository.deleteAll();
        categoryRepository.deleteAll();

        // 創建測試分類
        Category category = categoryRepository.save(
            Category.builder().name("電子產品").build()
        );

        // 創建測試商品
        testProduct1 = productRepository.save(
            Product.builder()
                .name("iPhone 15 Pro")
                .price(new BigDecimal("35900"))
                .stockQuantity(50)
                .active(true)
                .category(category)
                .build()
        );

        testProduct2 = productRepository.save(
            Product.builder()
                .name("AirPods Pro")
                .price(new BigDecimal("7990"))
                .stockQuantity(100)
                .active(true)
                .category(category)
                .build()
        );
    }

    @Nested
    @DisplayName("POST /api/v1/orders - 創建訂單")
    class CreateOrderTests {

        @Test
        @DisplayName("成功創建訂單")
        void shouldCreateOrderSuccessfully() throws Exception {
            // Arrange
            CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("王小明")
                .customerEmail("test@example.com")
                .shippingAddress("台北市信義區信義路五段7號")
                .items(List.of(
                    OrderItemRequest.builder()
                        .productId(testProduct1.getId())
                        .quantity(2)
                        .build()
                ))
                .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.orderNumber").isNotEmpty())
                .andExpect(jsonPath("$.customerName").value("王小明"))
                .andExpect(jsonPath("$.status").value("PENDING"))
                .andExpect(jsonPath("$.totalAmount").value(71800.00))  // 35900 * 2
                .andExpect(jsonPath("$.items", hasSize(1)))
                .andExpect(jsonPath("$.items[0].productName").value("iPhone 15 Pro"))
                .andExpect(jsonPath("$.items[0].quantity").value(2));

            // 驗證庫存已扣減
            Product updatedProduct = productRepository.findById(testProduct1.getId()).orElseThrow();
            assertThat(updatedProduct.getStockQuantity()).isEqualTo(48);  // 50 - 2
        }

        @Test
        @DisplayName("創建包含多個商品的訂單")
        void shouldCreateOrderWithMultipleItems() throws Exception {
            // Arrange
            CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("李小華")
                .customerEmail("customer@example.com")
                .shippingAddress("台北市")
                .items(List.of(
                    OrderItemRequest.builder()
                        .productId(testProduct1.getId())
                        .quantity(1)
                        .build(),
                    OrderItemRequest.builder()
                        .productId(testProduct2.getId())
                        .quantity(2)
                        .build()
                ))
                .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.items", hasSize(2)))
                .andExpect(jsonPath("$.totalAmount").value(51880.00));  // 35900 + 7990*2
        }

        @Test
        @DisplayName("庫存不足時應返回 400")
        void shouldReturn400WhenInsufficientStock() throws Exception {
            // Arrange
            CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("客戶")
                .customerEmail("test@example.com")
                .shippingAddress("地址")
                .items(List.of(
                    OrderItemRequest.builder()
                        .productId(testProduct1.getId())
                        .quantity(60)  // 庫存只有 50，超過庫存但不超過驗證上限 99
                        .build()
                ))
                .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value(containsString("庫存不足")));

            // 驗證庫存未被扣減
            Product unchangedProduct = productRepository.findById(testProduct1.getId()).orElseThrow();
            assertThat(unchangedProduct.getStockQuantity()).isEqualTo(50);
        }

        @Test
        @DisplayName("商品不存在時應返回 404")
        void shouldReturn404WhenProductNotFound() throws Exception {
            // Arrange
            CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("客戶")
                .customerEmail("test@example.com")
                .shippingAddress("地址")
                .items(List.of(
                    OrderItemRequest.builder()
                        .productId(99999L)
                        .quantity(1)
                        .build()
                ))
                .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
        }

        @Test
        @DisplayName("驗證失敗時應返回 400")
        void shouldReturn400WhenValidationFails() throws Exception {
            // Arrange: 缺少必填欄位
            CreateOrderRequest request = CreateOrderRequest.builder()
                .customerName("")  // 空名稱
                .customerEmail("invalid-email")  // 無效 email
                .items(List.of())  // 空項目
                .build();

            // Act & Assert
            mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors.customerName").exists())
                .andExpect(jsonPath("$.errors.customerEmail").exists())
                .andExpect(jsonPath("$.errors.shippingAddress").exists())
                .andExpect(jsonPath("$.errors.items").exists());
        }
    }

    @Nested
    @DisplayName("訂單取消測試")
    class CancelOrderTests {

        @Test
        @DisplayName("取消訂單後應恢復庫存")
        void shouldRestoreStockWhenCancelled() throws Exception {
            // Arrange: 先創建訂單
            CreateOrderRequest createRequest = CreateOrderRequest.builder()
                .customerName("客戶")
                .customerEmail("test@example.com")
                .shippingAddress("地址")
                .items(List.of(
                    OrderItemRequest.builder()
                        .productId(testProduct1.getId())
                        .quantity(5)
                        .build()
                ))
                .build();

            String response = mockMvc.perform(post("/api/v1/orders")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(createRequest)))
                .andExpect(status().isCreated())
                .andReturn().getResponse().getContentAsString();

            Long orderId = objectMapper.readTree(response).get("id").asLong();

            // 驗證庫存已扣減
            assertThat(productRepository.findById(testProduct1.getId()).get().getStockQuantity())
                .isEqualTo(45);

            // Act: 取消訂單
            mockMvc.perform(post("/api/v1/orders/" + orderId + "/cancel"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("CANCELLED"));

            // Assert: 驗證庫存已恢復
            Product restoredProduct = productRepository.findById(testProduct1.getId()).orElseThrow();
            assertThat(restoredProduct.getStockQuantity()).isEqualTo(50);
        }
    }
}
