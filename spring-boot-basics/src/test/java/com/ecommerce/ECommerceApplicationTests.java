package com.ecommerce;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

/**
 * 應用程式整合測試
 *
 * @SpringBootTest: 載入完整的 Spring 應用程式上下文
 * - 用於整合測試
 * - 測試所有組件的協作
 * - 執行速度較慢，但測試覆蓋更全面
 */
@SpringBootTest
@ActiveProfiles("test")
@DisplayName("應用程式整合測試")
class ECommerceApplicationTests {

    @Test
    @DisplayName("應用程式上下文應能成功載入")
    void contextLoads() {
        // 此測試驗證 Spring Context 可以成功啟動
        // 如果有任何配置錯誤，此測試會失敗
    }
}
