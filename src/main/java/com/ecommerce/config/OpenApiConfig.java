package com.ecommerce.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

/**
 * OpenAPI (Swagger) 配置類
 *
 * @Configuration: 標記為配置類
 */
@Configuration
public class OpenApiConfig {

    /**
     * @Bean: 在 Spring 容器中創建 OpenAPI 物件
     */
    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("電子商務 API")
                .version("1.0.0")
                .description("""
                    Spring Boot 學習專案 - 電子商務 API 文件

                    此專案展示 Spring Boot 常用註解的使用方式，包含：
                    - 核心註解 (@SpringBootApplication, @Configuration)
                    - 組件註解 (@Service, @Repository, @RestController)
                    - 依賴注入 (@Autowired, @Value)
                    - Web/REST (@GetMapping, @PostMapping, @RequestBody)
                    - 數據訪問 (@Entity, @Query, @Transactional)
                    - 驗證 (@Valid, @NotNull, @Size)
                    - 測試 (@SpringBootTest, @MockBean)

                    **H2 Console**: [/h2-console](/h2-console)
                    """)
                .contact(new Contact()
                    .name("Spring Boot Learning")
                    .email("learning@example.com"))
                .license(new License()
                    .name("MIT License")
                    .url("https://opensource.org/licenses/MIT")))
            .servers(List.of(
                new Server()
                    .url("http://localhost:8080")
                    .description("本地開發環境")
            ));
    }
}
