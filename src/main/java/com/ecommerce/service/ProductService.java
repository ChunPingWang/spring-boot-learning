package com.ecommerce.service;

import com.ecommerce.dto.CreateProductRequest;
import com.ecommerce.dto.ProductDTO;
import com.ecommerce.dto.UpdateProductRequest;
import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.exception.ResourceNotFoundException;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 商品服務
 *
 * @Service: 標記為業務邏輯層組件，是 @Component 的語義化版本
 * @RequiredArgsConstructor: Lombok 自動生成包含 final 欄位的建構子
 * @Slf4j: Lombok 自動生成 log 物件
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ProductService {

    private final ProductRepository productRepository;
    private final CategoryRepository categoryRepository;

    /**
     * 查詢所有商品（分頁）
     *
     * @Transactional(readOnly = true): 唯讀事務，優化效能
     */
    @Transactional(readOnly = true)
    public Page<ProductDTO> getAllProducts(Pageable pageable) {
        log.debug("查詢所有商品，分頁: {}", pageable);
        return productRepository.findByActiveTrue(pageable)
            .map(ProductDTO::fromEntity);
    }

    /**
     * 根據 ID 查詢商品
     */
    @Transactional(readOnly = true)
    public ProductDTO getProductById(Long id) {
        log.debug("查詢商品 ID: {}", id);
        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("商品", "id", id));
        return ProductDTO.fromEntity(product);
    }

    /**
     * 根據關鍵字搜尋商品
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> searchProducts(String keyword) {
        log.debug("搜尋商品，關鍵字: {}", keyword);
        return productRepository.findByNameContaining(keyword).stream()
            .map(ProductDTO::fromEntity)
            .toList();
    }

    /**
     * 根據分類查詢商品
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getProductsByCategory(Long categoryId) {
        log.debug("查詢分類 {} 的商品", categoryId);
        return productRepository.findByCategoryIdAndActiveTrue(categoryId).stream()
            .map(ProductDTO::fromEntity)
            .toList();
    }

    /**
     * 創建商品
     *
     * @Transactional: 在事務中執行，失敗時自動回滾
     */
    @Transactional
    public ProductDTO createProduct(CreateProductRequest request) {
        log.info("創建新商品: {}", request.getName());

        Product product = Product.builder()
            .name(request.getName())
            .price(request.getPrice())
            .description(request.getDescription())
            .stockQuantity(request.getStockQuantity() != null ? request.getStockQuantity() : 0)
            .imageUrl(request.getImageUrl())
            .active(true)
            .build();

        // 設定分類
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("分類", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        log.info("商品創建成功，ID: {}", saved.getId());

        return ProductDTO.fromEntity(saved);
    }

    /**
     * 更新商品
     */
    @Transactional
    public ProductDTO updateProduct(Long id, UpdateProductRequest request) {
        log.info("更新商品 ID: {}", id);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("商品", "id", id));

        // 更新非空欄位
        if (request.getName() != null) {
            product.setName(request.getName());
        }
        if (request.getPrice() != null) {
            product.setPrice(request.getPrice());
        }
        if (request.getDescription() != null) {
            product.setDescription(request.getDescription());
        }
        if (request.getStockQuantity() != null) {
            product.setStockQuantity(request.getStockQuantity());
        }
        if (request.getImageUrl() != null) {
            product.setImageUrl(request.getImageUrl());
        }
        if (request.getActive() != null) {
            product.setActive(request.getActive());
        }
        if (request.getCategoryId() != null) {
            Category category = categoryRepository.findById(request.getCategoryId())
                .orElseThrow(() -> new ResourceNotFoundException("分類", "id", request.getCategoryId()));
            product.setCategory(category);
        }

        Product saved = productRepository.save(product);
        log.info("商品更新成功，ID: {}", saved.getId());

        return ProductDTO.fromEntity(saved);
    }

    /**
     * 刪除商品（軟刪除）
     */
    @Transactional
    public void deleteProduct(Long id) {
        log.info("刪除商品 ID: {}", id);

        Product product = productRepository.findById(id)
            .orElseThrow(() -> new ResourceNotFoundException("商品", "id", id));

        product.setActive(false);
        productRepository.save(product);

        log.info("商品已停用，ID: {}", id);
    }

    /**
     * 查詢低庫存商品
     */
    @Transactional(readOnly = true)
    public List<ProductDTO> getLowStockProducts(int threshold) {
        log.debug("查詢低庫存商品，閾值: {}", threshold);
        return productRepository.findLowStockProducts(threshold).stream()
            .map(ProductDTO::fromEntity)
            .toList();
    }
}
