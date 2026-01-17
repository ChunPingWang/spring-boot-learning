# Spring Security Demo - JWT 認證範例

這個模組展示 Spring Security 的實作方式，包含 JWT Token 認證、角色基礎存取控制 (RBAC) 等功能。

## 功能特色

- **JWT 認證** - 無狀態的 Token 認證機制
- **使用者註冊/登入** - 完整的認證流程
- **角色授權 (RBAC)** - USER, ADMIN, MODERATOR 角色
- **方法級安全** - `@PreAuthorize` 註解
- **密碼加密** - BCrypt 雜湊演算法
- **Swagger/OpenAPI** - API 文檔（含 Bearer Token 支援）

## 專案結構

```
spring-security-demo/
├── src/main/java/com/security/
│   ├── SecurityDemoApplication.java  # 應用程式進入點
│   ├── config/                       # 安全配置
│   │   ├── SecurityConfig.java       # Spring Security 配置
│   │   ├── JwtAuthenticationFilter.java  # JWT 過濾器
│   │   ├── OpenApiConfig.java        # Swagger 配置
│   │   └── DataInitializer.java      # 初始使用者資料
│   ├── controller/                   # REST 控制器
│   │   ├── AuthController.java       # 認證 API（註冊/登入）
│   │   ├── UserController.java       # 使用者 API（需認證）
│   │   └── PublicController.java     # 公開 API
│   ├── dto/                          # 資料傳輸物件
│   │   ├── LoginRequest.java
│   │   ├── RegisterRequest.java
│   │   ├── AuthResponse.java
│   │   └── UserDTO.java
│   ├── entity/                       # JPA 實體
│   │   ├── User.java                 # 使用者（實作 UserDetails）
│   │   └── Role.java                 # 角色枚舉
│   ├── exception/                    # 異常處理
│   ├── repository/                   # 資料存取層
│   └── service/                      # 業務邏輯層
│       ├── AuthService.java          # 認證服務
│       ├── JwtService.java           # JWT 處理
│       └── UserService.java          # 使用者服務
└── src/test/java/                    # 測試程式碼
```

## 執行方式

```bash
# 從根目錄執行
./gradlew :spring-security-demo:bootRun

# 或進入模組目錄
cd spring-security-demo
../gradlew bootRun
```

## API 端點

應用程式啟動後，可透過以下方式存取：

- **Swagger UI**: http://localhost:8081/swagger-ui.html
- **H2 Console**: http://localhost:8081/h2-console
- **API 文檔**: http://localhost:8081/v3/api-docs

### 認證 API（公開）

| 方法 | 路徑 | 說明 |
|------|------|------|
| POST | `/api/auth/register` | 使用者註冊 |
| POST | `/api/auth/login` | 使用者登入 |

### 使用者 API（需認證）

| 方法 | 路徑 | 權限 | 說明 |
|------|------|------|------|
| GET | `/api/users/me` | 任何已認證使用者 | 取得當前使用者資訊 |
| GET | `/api/users/admin-only` | ADMIN | 管理員專用端點 |
| GET | `/api/users/moderator` | ADMIN, MODERATOR | 管理者專用端點 |

### 公開 API

| 方法 | 路徑 | 說明 |
|------|------|------|
| GET | `/api/public/health` | 健康檢查 |
| GET | `/api/public/info` | API 資訊 |

## 預設帳號

應用程式啟動時會自動建立以下測試帳號：

| 使用者名稱 | 密碼 | 角色 |
|-----------|------|------|
| admin | admin123 | USER, ADMIN |
| moderator | mod123 | USER, MODERATOR |
| user | user123 | USER |

## 使用範例

### 1. 登入取得 Token

```bash
curl -X POST http://localhost:8081/api/auth/login \
  -H "Content-Type: application/json" \
  -d '{"username":"admin","password":"admin123"}'
```

回應：
```json
{
  "accessToken": "eyJhbGciOiJIUzI1NiJ9...",
  "tokenType": "Bearer",
  "expiresIn": 86400000,
  "username": "admin",
  "roles": ["ROLE_USER", "ROLE_ADMIN"]
}
```

### 2. 使用 Token 存取受保護資源

```bash
curl -X GET http://localhost:8081/api/users/me \
  -H "Authorization: Bearer eyJhbGciOiJIUzI1NiJ9..."
```

## 核心元件說明

### SecurityConfig

配置 Spring Security 的安全規則：
- 停用 CSRF（使用 JWT）
- 設定無狀態 Session
- 定義公開/保護路徑
- 配置 JWT 過濾器

### JwtAuthenticationFilter

每個請求都會經過的過濾器：
- 從 Authorization 標頭提取 Token
- 驗證 Token 有效性
- 設定 SecurityContext

### JwtService

處理 JWT Token：
- 生成 Token
- 驗證 Token
- 提取 Claims（使用者名稱、過期時間等）

## 測試

```bash
# 執行所有測試
./gradlew :spring-security-demo:test

# 執行特定測試類
./gradlew :spring-security-demo:test --tests "*AuthControllerTest*"
```

### 測試類型

- **JWT 服務測試** - `JwtServiceTest`
- **認證控制器測試** - `AuthControllerTest`
- **使用者控制器測試** - `UserControllerTest`（權限測試）

## 相關資源

- [返回專案根目錄](../README.md)
- [Spring Boot 基礎模組](../spring-boot-basics/README.md)
- [Spring Security 完整教學](../SPRING_SECURITY.md)
