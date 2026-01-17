package com.ecommerce.config;

import com.ecommerce.entity.Category;
import com.ecommerce.entity.Product;
import com.ecommerce.repository.CategoryRepository;
import com.ecommerce.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

/**
 * 資料初始化器
 *
 * @Component: 標記為 Spring 組件
 * @Profile("!test"): 在非測試環境下執行
 *
 * CommandLineRunner: Spring Boot 啟動完成後執行
 */
@Component
@Profile("!test")
@RequiredArgsConstructor
@Slf4j
public class DataInitializer implements CommandLineRunner {

    private final CategoryRepository categoryRepository;
    private final ProductRepository productRepository;

    @Override
    public void run(String... args) {
        log.info("開始初始化示範資料...");

        // 創建分類
        Category electronics = createCategory("電子產品", "各種電子產品與3C配件");
        Category clothing = createCategory("服飾", "男女服飾與配件");
        Category food = createCategory("食品", "各式食品與零食");

        // 創建電子產品
        createProduct("iPhone 15 Pro", new BigDecimal("35900"), 50,
            "Apple 最新旗艦手機，搭載 A17 Pro 晶片", electronics);
        createProduct("iPhone 15", new BigDecimal("29900"), 80,
            "Apple iPhone 15，全新動態島設計", electronics);
        createProduct("MacBook Pro 14\"", new BigDecimal("59900"), 30,
            "M3 Pro 晶片，14 吋 Liquid Retina XDR 顯示器", electronics);
        createProduct("AirPods Pro 2", new BigDecimal("7990"), 100,
            "主動式降噪，適應性音訊", electronics);
        createProduct("iPad Air", new BigDecimal("19900"), 60,
            "M1 晶片，10.9 吋 Liquid Retina 顯示器", electronics);

        // 創建服飾
        createProduct("純棉 T-Shirt", new BigDecimal("590"), 200,
            "100% 純棉材質，舒適透氣", clothing);
        createProduct("牛仔褲", new BigDecimal("1290"), 150,
            "經典直筒版型，百搭款式", clothing);
        createProduct("運動外套", new BigDecimal("1990"), 80,
            "防風防水材質，適合戶外活動", clothing);

        // 創建食品
        createProduct("綜合堅果", new BigDecimal("299"), 300,
            "嚴選多種堅果，健康零食首選", food);
        createProduct("有機茶葉禮盒", new BigDecimal("899"), 50,
            "台灣高山有機茶葉，精美禮盒包裝", food);

        log.info("示範資料初始化完成！");
        log.info("=================================================");
        log.info("Swagger UI: http://localhost:8080/swagger-ui.html");
        log.info("H2 Console: http://localhost:8080/h2-console");
        log.info("=================================================");
    }

    private Category createCategory(String name, String description) {
        Category category = Category.builder()
            .name(name)
            .description(description)
            .build();
        return categoryRepository.save(category);
    }

    private void createProduct(String name, BigDecimal price, int stock,
                               String description, Category category) {
        Product product = Product.builder()
            .name(name)
            .price(price)
            .stockQuantity(stock)
            .description(description)
            .category(category)
            .active(true)
            .build();
        productRepository.save(product);
    }
}
