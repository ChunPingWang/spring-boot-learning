package com.ecommerce.repository;

import com.ecommerce.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

/**
 * 商品分類倉儲介面
 *
 * @Repository: 標記為資料存取層組件
 *
 * 繼承 JpaRepository 可獲得：
 * - save(), saveAll()
 * - findById(), findAll(), findAllById()
 * - delete(), deleteById(), deleteAll()
 * - count(), existsById()
 * 等 CRUD 方法
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

    /**
     * 根據名稱查詢分類
     * Spring Data JPA 會自動實現此方法（方法名稱查詢）
     */
    Optional<Category> findByName(String name);

    /**
     * 檢查名稱是否存在
     */
    boolean existsByName(String name);
}
