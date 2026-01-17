package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.math.BigDecimal;

/**
 * 商品實體
 *
 * 展示 JPA 實體註解的使用方式
 */
@Entity
@Table(name = "products", indexes = {
    @Index(name = "idx_product_name", columnList = "name"),
    @Index(name = "idx_product_category", columnList = "category_id")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Product extends BaseEntity {

    @Column(name = "name", nullable = false, length = 200)
    private String name;

    /**
     * @Column precision 和 scale 用於 BigDecimal
     * - precision: 總位數
     * - scale: 小數位數
     */
    @Column(name = "price", nullable = false, precision = 10, scale = 2)
    private BigDecimal price;

    /**
     * @Column columnDefinition 可指定原生 SQL 類型
     */
    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "stock_quantity", nullable = false)
    @Builder.Default
    private Integer stockQuantity = 0;

    @Column(name = "image_url", length = 500)
    private String imageUrl;

    @Column(name = "active")
    @Builder.Default
    private Boolean active = true;

    /**
     * @ManyToOne: 多對一關聯
     * - fetch: 載入策略 (LAZY 延遲載入, EAGER 立即載入)
     *
     * @JoinColumn: 指定外鍵欄位
     */
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "category_id")
    private Category category;

    /**
     * 業務方法：檢查庫存是否足夠
     */
    public boolean hasStock(int quantity) {
        return this.stockQuantity >= quantity;
    }

    /**
     * 業務方法：扣減庫存
     */
    public void decreaseStock(int quantity) {
        if (!hasStock(quantity)) {
            throw new IllegalStateException("庫存不足");
        }
        this.stockQuantity -= quantity;
    }

    /**
     * 業務方法：增加庫存
     */
    public void increaseStock(int quantity) {
        this.stockQuantity += quantity;
    }
}
