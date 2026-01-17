package com.ecommerce.service;

import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.UpdateProductRequest;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

/**
 * 商品服務單元測試
 *
 * @ExtendWith(MockitoExtension.class): 啟用 Mockito 支援
 *
 * 單元測試特點：
 * - 不啟動 Spring 容器，執行速度快
 * - 使用 Mock 物件隔離依賴
 * - 專注測試業務邏輯
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("商品服務單元測試")
class ProductServiceTest {

    /**
     * @Mock: 創建模擬物件
     */
    @Mock
    private ProductRepository productRepository;

    @Mock
    private CategoryRepository categoryRepository;

    /**
     * @InjectMocks: 創建被測物件，並注入上面的 Mock
     */
    @InjectMocks
    private ProductService productService;

    private Product testProduct;
    private Category testCategory;

    @BeforeEach
    void setUp() {
        testCategory = Category.builder()
            .id(1L)
            .name("電子產品")
            .build();

        testProduct = Product.builder()
            .id(1L)
            .name("iPhone 15 Pro")
            .price(new BigDecimal("35900"))
            .stockQuantity(50)
            .active(true)
            .category(testCategory)
            .build();
    }

    @Nested
    @DisplayName("查詢商品測試")
    class GetProductTests {

        @Test
        @DisplayName("根據 ID 查詢商品 - 成功")
        void getProductById_WhenExists_ShouldReturnProductDTO() {
            // Arrange: 設定 Mock 行為
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));

            // Act: 執行被測方法
            ProductDTO result = productService.getProductById(1L);

            // Assert: 驗證結果
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo(1L);
            assertThat(result.getName()).isEqualTo("iPhone 15 Pro");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("35900"));

            // Verify: 驗證 Mock 被呼叫
            verify(productRepository, times(1)).findById(1L);
        }

        @Test
        @DisplayName("根據 ID 查詢商品 - 商品不存在時應拋出異常")
        void getProductById_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> productService.getProductById(999L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("商品")
                .hasMessageContaining("999");

            verify(productRepository, times(1)).findById(999L);
        }

        @Test
        @DisplayName("查詢所有商品（分頁）")
        void getAllProducts_ShouldReturnPageOfProducts() {
            // Arrange
            List<Product> products = List.of(testProduct);
            Page<Product> productPage = new PageImpl<>(products);
            Pageable pageable = PageRequest.of(0, 10);

            when(productRepository.findByActiveTrue(pageable)).thenReturn(productPage);

            // Act
            Page<ProductDTO> result = productService.getAllProducts(pageable);

            // Assert
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("iPhone 15 Pro");

            verify(productRepository).findByActiveTrue(pageable);
        }

        @Test
        @DisplayName("搜尋商品")
        void searchProducts_ShouldReturnMatchingProducts() {
            // Arrange
            when(productRepository.findByNameContaining("iPhone"))
                .thenReturn(List.of(testProduct));

            // Act
            List<ProductDTO> results = productService.searchProducts("iPhone");

            // Assert
            assertThat(results)
                .hasSize(1)
                .first()
                .satisfies(dto -> assertThat(dto.getName()).contains("iPhone"));
        }
    }

    @Nested
    @DisplayName("創建商品測試")
    class CreateProductTests {

        @Test
        @DisplayName("創建商品 - 成功")
        void createProduct_ShouldReturnCreatedProduct() {
            // Arrange
            CreateProductRequest request = CreateProductRequest.builder()
                .name("新商品")
                .price(new BigDecimal("1999"))
                .stockQuantity(100)
                .build();

            Product savedProduct = Product.builder()
                .id(2L)
                .name("新商品")
                .price(new BigDecimal("1999"))
                .stockQuantity(100)
                .active(true)
                .build();

            when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

            // Act
            ProductDTO result = productService.createProduct(request);

            // Assert
            assertThat(result.getId()).isEqualTo(2L);
            assertThat(result.getName()).isEqualTo("新商品");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("1999"));

            // 驗證 save 方法被呼叫一次
            verify(productRepository, times(1)).save(any(Product.class));
        }

        @Test
        @DisplayName("創建商品 - 帶分類")
        void createProduct_WithCategory_ShouldSetCategory() {
            // Arrange
            CreateProductRequest request = CreateProductRequest.builder()
                .name("帶分類商品")
                .price(new BigDecimal("999"))
                .categoryId(1L)
                .build();

            when(categoryRepository.findById(1L)).thenReturn(Optional.of(testCategory));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
                Product p = invocation.getArgument(0);
                p.setId(3L);
                return p;
            });

            // Act
            ProductDTO result = productService.createProduct(request);

            // Assert
            assertThat(result.getCategoryName()).isEqualTo("電子產品");

            verify(categoryRepository).findById(1L);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("創建商品 - 分類不存在時應拋出異常")
        void createProduct_WithInvalidCategory_ShouldThrowException() {
            // Arrange
            CreateProductRequest request = CreateProductRequest.builder()
                .name("商品")
                .price(new BigDecimal("999"))
                .categoryId(999L)
                .build();

            when(categoryRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> productService.createProduct(request))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("分類");

            // 驗證 productRepository.save 沒有被呼叫
            verify(productRepository, never()).save(any());
        }
    }

    @Nested
    @DisplayName("更新商品測試")
    class UpdateProductTests {

        @Test
        @DisplayName("更新商品 - 成功")
        void updateProduct_ShouldReturnUpdatedProduct() {
            // Arrange
            UpdateProductRequest request = UpdateProductRequest.builder()
                .name("更新後的名稱")
                .price(new BigDecimal("39900"))
                .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ProductDTO result = productService.updateProduct(1L, request);

            // Assert
            assertThat(result.getName()).isEqualTo("更新後的名稱");
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("39900"));

            verify(productRepository).findById(1L);
            verify(productRepository).save(any(Product.class));
        }

        @Test
        @DisplayName("更新商品 - 只更新部分欄位")
        void updateProduct_PartialUpdate_ShouldOnlyUpdateProvidedFields() {
            // Arrange
            UpdateProductRequest request = UpdateProductRequest.builder()
                .price(new BigDecimal("32900"))  // 只更新價格
                .build();

            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            ProductDTO result = productService.updateProduct(1L, request);

            // Assert
            assertThat(result.getName()).isEqualTo("iPhone 15 Pro");  // 名稱未變
            assertThat(result.getPrice()).isEqualByComparingTo(new BigDecimal("32900"));  // 價格已更新
        }
    }

    @Nested
    @DisplayName("刪除商品測試")
    class DeleteProductTests {

        @Test
        @DisplayName("刪除商品 - 應執行軟刪除")
        void deleteProduct_ShouldSetActiveToFalse() {
            // Arrange
            when(productRepository.findById(1L)).thenReturn(Optional.of(testProduct));
            when(productRepository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));

            // Act
            productService.deleteProduct(1L);

            // Assert: 驗證商品被設為非活躍
            verify(productRepository).save(argThat(product ->
                !product.getActive()
            ));
        }

        @Test
        @DisplayName("刪除不存在的商品 - 應拋出異常")
        void deleteProduct_WhenNotExists_ShouldThrowException() {
            // Arrange
            when(productRepository.findById(999L)).thenReturn(Optional.empty());

            // Act & Assert
            assertThatThrownBy(() -> productService.deleteProduct(999L))
                .isInstanceOf(ResourceNotFoundException.class);

            verify(productRepository, never()).save(any());
        }
    }
}
