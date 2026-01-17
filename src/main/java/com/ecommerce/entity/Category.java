package com.ecommerce.entity;

import jakarta.persistence.*;
import lombok.*;
import lombok.experimental.SuperBuilder;

import java.util.ArrayList;
import java.util.List;

/**
 * 商品分類實體
 *
 * @Entity: 標記為 JPA 實體，對應資料庫表
 * @Table: 指定對應的資料庫表名及約束
 */
@Entity
@Table(name = "categories", uniqueConstraints = {
    @UniqueConstraint(columnNames = "name", name = "uk_category_name")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@SuperBuilder
public class Category extends BaseEntity {

    /**
     * @Column: 指定欄位對應的資料庫列屬性
     * - nullable: 是否允許 null
     * - length: 字串最大長度
     */
    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "description", length = 500)
    private String description;

    /**
     * @OneToMany: 一對多關聯
     * - mappedBy: 指定關聯的擁有方欄位
     * - cascade: 級聯操作
     * - orphanRemoval: 刪除孤兒記錄
     */
    @OneToMany(mappedBy = "category", cascade = CascadeType.ALL)
    @Builder.Default
    private List<Product> products = new ArrayList<>();
}
