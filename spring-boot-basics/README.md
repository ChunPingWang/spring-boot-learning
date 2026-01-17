# Spring Boot Basics - 電子商務 REST API 範例

這個模組展示 Spring Boot 的基礎功能，透過一個電子商務 REST API 來說明各種核心概念。

## 功能特色

- **RESTful API** - 完整的 CRUD 操作範例
- **JPA/Hibernate** - 資料持久層實作
- **Bean Validation** - 請求驗證機制
- **Exception Handling** - 全域異常處理
- **Swagger/OpenAPI** - API 文檔自動生成
- **H2 Database** - 記憶體資料庫（開發/測試用）

## 專案結構

```
spring-boot-basics/
├── src/main/java/com/ecommerce/
│   ├── ECommerceApplication.java    # 應用程式進入點
│   ├── config/                      # 配置類
│   │   ├── DataInitializer.java     # 資料初始化
│   │   └── OpenApiConfig.java       # Swagger 配置
│   ├── controller/                  # REST 控制器
│   │   ├── ProductController.java   # 商品 API
│   │   └── OrderController.java     # 訂單 API
│   ├── dto/                         # 資料傳輸物件
│   ├── entity/                      # JPA 實體
│   │   ├── BaseEntity.java          # 基礎實體（ID、時間戳）
│   │   ├── Category.java            # 分類
│   │   ├── Product.java             # 商品
│   │   ├── Order.java               # 訂單
│   │   └── OrderItem.java           # 訂單項目
│   ├── exception/                   # 異常處理
│   ├── repository/                  # 資料存取層
│   └── service/                     # 業務邏輯層
└── src/test/java/                   # 測試程式碼
```

## 執行方式

```bash
# 從根目錄執行
./gradlew :spring-boot-basics:bootRun

# 或進入模組目錄
cd spring-boot-basics
../gradlew bootRun
```

## API 端點

應用程式啟動後，可透過以下方式存取：

- **Swagger UI**: http://localhost:8080/swagger-ui.html
- **H2 Console**: http://localhost:8080/h2-console
- **API 文檔**: http://localhost:8080/api-docs

### 商品 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/v1/products` | 取得商品列表（分頁） |
| GET | `/api/v1/products/{id}` | 取得單一商品 |
| POST | `/api/v1/products` | 建立商品 |
| PUT | `/api/v1/products/{id}` | 更新商品 |
| DELETE | `/api/v1/products/{id}` | 刪除商品 |
| GET | `/api/v1/products/search` | 搜尋商品 |

### 訂單 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/v1/orders/{id}` | 取得訂單詳情 |
| POST | `/api/v1/orders` | 建立訂單 |
| PUT | `/api/v1/orders/{id}/status` | 更新訂單狀態 |
| POST | `/api/v1/orders/{id}/cancel` | 取消訂單 |

## 核心註解說明

### 實體層 (Entity)
- `@Entity` - 標記為 JPA 實體
- `@Table` - 指定資料表名稱
- `@Id`, `@GeneratedValue` - 主鍵設定
- `@ManyToOne`, `@OneToMany` - 關聯映射
- `@Column` - 欄位配置

### 控制器層 (Controller)
- `@RestController` - RESTful 控制器
- `@RequestMapping` - 路由映射
- `@GetMapping`, `@PostMapping` 等 - HTTP 方法映射
- `@Valid` - 啟用請求驗證

### 服務層 (Service)
- `@Service` - 業務邏輯元件
- `@Transactional` - 事務管理

### 資料層 (Repository)
- `@Repository` - 資料存取元件
- `@Query` - 自定義查詢

## 測試

```bash
# 執行所有測試
./gradlew :spring-boot-basics:test

# 執行特定測試類
./gradlew :spring-boot-basics:test --tests "*ProductServiceTest*"
```

### 測試類型

- **單元測試** - `ProductServiceTest`, `OrderServiceTest`
- **Repository 測試** - `ProductRepositoryTest` (使用 `@DataJpaTest`)
- **控制器測試** - `ProductControllerTest` (使用 `@SpringBootTest`)
- **整合測試** - `OrderControllerIntegrationTest`

## 相關資源

- [返回專案根目錄](../README.md)
- [Spring Security 模組](../spring-security-demo/README.md)
