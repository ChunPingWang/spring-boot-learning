package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * 商品倉儲測試類
 *
 * @DataJpaTest: JPA 切片測試
 * - 只載入 JPA 相關組件（Entity, Repository）
 * - 自動配置嵌入式資料庫（H2）
 * - 每個測試方法執行後自動回滾
 *
 * @ActiveProfiles("test"): 使用測試環境配置
 */
@DataJpaTest
@ActiveProfiles("test")
@DisplayName("商品倉儲測試")
class ProductRepositoryTest {

    /**
     * @Autowired: 注入要測試的 Repository
     */
    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private CategoryRepository categoryRepository;

    /**
     * TestEntityManager: Spring 提供的測試用 EntityManager
     * 用於準備測試資料
     */
    @Autowired
    private TestEntityManager entityManager;

    private Category electronics;
    private Category clothing;

    /**
     * @BeforeEach: 每個測試方法執行前執行
     * 用於準備測試資料
     */
    @BeforeEach
    void setUp() {
        // 創建測試用分類
        electronics = Category.builder()
            .name("電子產品")
            .description("各種電子產品")
            .build();
        entityManager.persist(electronics);

        clothing = Category.builder()
            .name("服飾")
            .description("各種服飾商品")
            .build();
        entityManager.persist(clothing);

        entityManager.flush();
    }

    /**
     * @Nested: 將相關測試分組
     */
    @Nested
    @DisplayName("方法名稱查詢測試")
    class MethodNameQueryTests {

        @Test
        @DisplayName("根據名稱模糊查詢 - 應返回包含關鍵字的商品")
        void findByNameContaining_ShouldReturnMatchingProducts() {
            // Arrange（準備）：創建測試資料
            Product iphone = createProduct("iPhone 15 Pro", new BigDecimal("35900"), 50);
            Product macbook = createProduct("MacBook Pro", new BigDecimal("59900"), 30);
            Product tshirt = createProduct("純棉 T-Shirt", new BigDecimal("590"), 100);
            entityManager.persist(iphone);
            entityManager.persist(macbook);
            entityManager.persist(tshirt);
            entityManager.flush();

            // Act（執行）：執行查詢
            List<Product> results = productRepository.findByNameContaining("Pro");

            // Assert（驗證）：驗證結果
            assertThat(results)
                .hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("iPhone 15 Pro", "MacBook Pro");
        }

        @Test
        @DisplayName("根據價格區間查詢 - 應返回指定區間內的商品")
        void findByPriceBetween_ShouldReturnProductsInRange() {
            // Arrange
            Product cheap = createProduct("便宜商品", new BigDecimal("100"), 10);
            Product medium = createProduct("中價商品", new BigDecimal("500"), 10);
            Product expensive = createProduct("昂貴商品", new BigDecimal("2000"), 10);
            entityManager.persist(cheap);
            entityManager.persist(medium);
            entityManager.persist(expensive);
            entityManager.flush();

            // Act
            List<Product> results = productRepository.findByPriceBetween(
                new BigDecimal("200"), new BigDecimal("1000"));

            // Assert
            assertThat(results)
                .hasSize(1)
                .first()
                .extracting(Product::getName)
                .isEqualTo("中價商品");
        }

        @Test
        @DisplayName("根據分類查詢上架商品")
        void findByCategoryIdAndActiveTrue_ShouldReturnActiveProductsOnly() {
            // Arrange
            Product active1 = createProduct("上架商品1", new BigDecimal("100"), 10);
            active1.setCategory(electronics);
            active1.setActive(true);

            Product active2 = createProduct("上架商品2", new BigDecimal("200"), 10);
            active2.setCategory(electronics);
            active2.setActive(true);

            Product inactive = createProduct("下架商品", new BigDecimal("300"), 0);
            inactive.setCategory(electronics);
            inactive.setActive(false);

            entityManager.persist(active1);
            entityManager.persist(active2);
            entityManager.persist(inactive);
            entityManager.flush();

            // Act
            List<Product> results = productRepository.findByCategoryIdAndActiveTrue(electronics.getId());

            // Assert
            assertThat(results)
                .hasSize(2)
                .allMatch(Product::getActive);
        }
    }

    @Nested
    @DisplayName("@Query 自訂查詢測試")
    class CustomQueryTests {

        @Test
        @DisplayName("查詢低庫存商品 - 應返回庫存低於閾值的商品")
        void findLowStockProducts_ShouldReturnProductsBelowThreshold() {
            // Arrange
            Product lowStock1 = createProduct("低庫存1", new BigDecimal("100"), 3);
            lowStock1.setActive(true);
            Product lowStock2 = createProduct("低庫存2", new BigDecimal("200"), 7);
            lowStock2.setActive(true);
            Product normalStock = createProduct("正常庫存", new BigDecimal("300"), 50);
            normalStock.setActive(true);

            entityManager.persist(lowStock1);
            entityManager.persist(lowStock2);
            entityManager.persist(normalStock);
            entityManager.flush();

            // Act
            List<Product> results = productRepository.findLowStockProducts(10);

            // Assert
            assertThat(results)
                .hasSize(2)
                .extracting(Product::getName)
                .containsExactlyInAnyOrder("低庫存1", "低庫存2");
        }

        @Test
        @DisplayName("根據分類名稱查詢並按價格降序排列")
        void findByCategoryNameOrderByPriceDesc_ShouldReturnSortedProducts() {
            // Arrange
            Product p1 = createProduct("商品A", new BigDecimal("1000"), 10);
            p1.setCategory(electronics);
            Product p2 = createProduct("商品B", new BigDecimal("3000"), 10);
            p2.setCategory(electronics);
            Product p3 = createProduct("商品C", new BigDecimal("2000"), 10);
            p3.setCategory(electronics);

            entityManager.persist(p1);
            entityManager.persist(p2);
            entityManager.persist(p3);
            entityManager.flush();

            // Act
            List<Product> results = productRepository.findByCategoryNameOrderByPriceDesc("電子產品");

            // Assert
            assertThat(results)
                .hasSize(3)
                .extracting(Product::getName)
                .containsExactly("商品B", "商品C", "商品A");  // 按價格降序
        }

        @Test
        @DisplayName("統計分類下上架商品數量")
        void countActiveProductsByCategory_ShouldReturnCorrectCount() {
            // Arrange
            for (int i = 1; i <= 5; i++) {
                Product p = createProduct("電子產品" + i, new BigDecimal(i * 100), 10);
                p.setCategory(electronics);
                p.setActive(true);
                entityManager.persist(p);
            }

            Product inactive = createProduct("下架電子產品", new BigDecimal("100"), 0);
            inactive.setCategory(electronics);
            inactive.setActive(false);
            entityManager.persist(inactive);

            entityManager.flush();

            // Act
            long count = productRepository.countActiveProductsByCategory(electronics.getId());

            // Assert
            assertThat(count).isEqualTo(5);
        }
    }

    @Nested
    @DisplayName("CRUD 操作測試")
    class CrudOperationTests {

        @Test
        @DisplayName("保存商品 - 應自動生成 ID")
        void save_ShouldGenerateId() {
            // Arrange
            Product product = createProduct("新商品", new BigDecimal("999"), 10);

            // Act
            Product saved = productRepository.save(product);

            // Assert
            assertThat(saved.getId()).isNotNull();
            assertThat(saved.getName()).isEqualTo("新商品");
        }

        @Test
        @DisplayName("根據 ID 查詢 - 商品存在時應返回商品")
        void findById_WhenExists_ShouldReturnProduct() {
            // Arrange
            Product product = createProduct("測試商品", new BigDecimal("500"), 20);
            entityManager.persist(product);
            entityManager.flush();

            // Act
            var result = productRepository.findById(product.getId());

            // Assert
            assertThat(result)
                .isPresent()
                .hasValueSatisfying(p -> {
                    assertThat(p.getName()).isEqualTo("測試商品");
                    assertThat(p.getPrice()).isEqualByComparingTo(new BigDecimal("500"));
                });
        }

        @Test
        @DisplayName("根據 ID 查詢 - 商品不存在時應返回空")
        void findById_WhenNotExists_ShouldReturnEmpty() {
            // Act
            var result = productRepository.findById(999L);

            // Assert
            assertThat(result).isEmpty();
        }

        @Test
        @DisplayName("刪除商品")
        void delete_ShouldRemoveProduct() {
            // Arrange
            Product product = createProduct("待刪除商品", new BigDecimal("100"), 5);
            entityManager.persist(product);
            entityManager.flush();
            Long productId = product.getId();

            // Act
            productRepository.deleteById(productId);
            entityManager.flush();

            // Assert
            assertThat(productRepository.findById(productId)).isEmpty();
        }
    }

    /**
     * 輔助方法：創建測試用商品
     */
    private Product createProduct(String name, BigDecimal price, int stock) {
        return Product.builder()
            .name(name)
            .price(price)
            .stockQuantity(stock)
            .active(true)
            .build();
    }
}
