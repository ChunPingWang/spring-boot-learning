package com.ecommerce;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 電子商務應用程式入口
 *
 * @SpringBootApplication 是組合註解，包含：
 * - @Configuration: 標記為配置類
 * - @EnableAutoConfiguration: 啟用自動配置
 * - @ComponentScan: 掃描當前包及子包的組件
 */
@SpringBootApplication
@EnableJpaAuditing      // 啟用 JPA 審計功能（自動填充 createdAt, updatedAt）
@EnableScheduling       // 啟用定時任務
@EnableAsync           // 啟用異步方法
public class ECommerceApplication {

    public static void main(String[] args) {
        SpringApplication.run(ECommerceApplication.class, args);
    }
}
