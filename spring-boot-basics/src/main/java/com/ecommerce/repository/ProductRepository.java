package com.ecommerce.repository;

import com.ecommerce.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.math.BigDecimal;
import java.util.List;

/**
 * 商品倉儲介面
 *
 * 展示各種查詢方式：
 * 1. 方法名稱查詢
 * 2. @Query JPQL 查詢
 * 3. @Query 原生 SQL 查詢
 * 4. @Modifying 更新/刪除操作
 */
@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {

    // ============ 方法名稱查詢 ============

    /**
     * 根據名稱模糊查詢
     * findBy + 屬性名 + Containing = LIKE %keyword%
     */
    List<Product> findByNameContaining(String keyword);

    /**
     * 根據分類 ID 查詢
     */
    List<Product> findByCategoryId(Long categoryId);

    /**
     * 根據價格區間查詢
     * Between = BETWEEN ... AND ...
     */
    List<Product> findByPriceBetween(BigDecimal minPrice, BigDecimal maxPrice);

    /**
     * 查詢上架中的商品（分頁）
     */
    Page<Product> findByActiveTrue(Pageable pageable);

    /**
     * 根據分類 ID 和上架狀態查詢
     * And = 條件組合
     */
    List<Product> findByCategoryIdAndActiveTrue(Long categoryId);

    /**
     * 查詢庫存小於指定數量的商品
     * LessThan = <
     */
    List<Product> findByStockQuantityLessThan(int threshold);

    // ============ @Query JPQL 查詢 ============

    /**
     * @Query: 自訂 JPQL 查詢
     * @Param: 綁定命名參數
     */
    @Query("SELECT p FROM Product p WHERE p.stockQuantity < :threshold AND p.active = true")
    List<Product> findLowStockProducts(@Param("threshold") int threshold);

    /**
     * 帶排序的查詢
     */
    @Query("SELECT p FROM Product p WHERE p.category.name = :categoryName ORDER BY p.price DESC")
    List<Product> findByCategoryNameOrderByPriceDesc(@Param("categoryName") String categoryName);

    /**
     * 使用 JPQL 聚合函數
     */
    @Query("SELECT COUNT(p) FROM Product p WHERE p.category.id = :categoryId AND p.active = true")
    long countActiveProductsByCategory(@Param("categoryId") Long categoryId);

    // ============ @Query 原生 SQL 查詢 ============

    /**
     * nativeQuery = true 表示使用原生 SQL
     */
    @Query(value = "SELECT * FROM products WHERE active = true ORDER BY created_at DESC LIMIT :limit",
           nativeQuery = true)
    List<Product> findRecentProducts(@Param("limit") int limit);

    // ============ @Modifying 更新/刪除操作 ============

    /**
     * @Modifying: 標記為更新/刪除操作
     * 必須在 @Transactional 環境下執行
     */
    @Modifying
    @Query("UPDATE Product p SET p.price = p.price * :multiplier WHERE p.category.id = :categoryId")
    int updatePricesByCategory(@Param("categoryId") Long categoryId,
                               @Param("multiplier") BigDecimal multiplier);

    @Modifying
    @Query("UPDATE Product p SET p.active = false WHERE p.stockQuantity = 0")
    int deactivateOutOfStockProducts();

    @Modifying
    @Query("DELETE FROM Product p WHERE p.active = false AND p.stockQuantity = 0")
    int deleteInactiveAndOutOfStockProducts();
}
