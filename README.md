# 任務管理系統（Todo Management System）

# 系統介紹
- 本專案為 **任務管理系統（Task Management System）**，由練習基礎語法的 Todo Management System 延伸而來，後續嘗試將工作中接觸到的技術與研究概念融入系統設計，進一步擴充功能與介面。  
- 系統採用 **前後端分離架構** 開發：前端以 **React.js** 建立使用者介面，後端則以 **Spring Boot + MySQL** 開發 API 服務。  
- 功能涵蓋任務的新增、編輯、參與、刪除與完成，以及任務資訊與進度的即時呈現。 
- 本檔案為 **後端原始碼**，前端原始碼請見：(link)

# 功能介紹
- 使用者管理：註冊 / 登入 / JWT Token 驗證，支援角色分流（Admin / User
- 任務 CRUD API：建立、更新、刪除、查詢任務，支援截止日期與逾期判斷
- 待審核流程：任務完成後需由管理者審核，API 記錄完成者與審核者資訊
- 儀表板統計 API：回傳任務數據（總數、完成、未完成、待審核、逾期）
- 排行榜 API：提供協作參與次數與任務完成數，支援查詢使用者明細
- 表單驗證與錯誤處理：帳號、Email、密碼規則檢查，重複註冊防護，統一錯誤訊息回傳

# 系統架構
### 程式分層設計
後端採用 **分層架構設計**，依職責將程式劃分Controller、Service、Repository 與 Model，各層責任如下：
- **Controller 層**：接收Api請求，回傳 JSON 回應（例如：`AuthController`, `TodoController`）  
- **Service 層**：處理業務邏輯（例如：`AuthService`, `TodoService`）  
- **Repository 層**：透過 Spring Data JPA 與 MySQL 互動（例如：`UserRepository`, `TodoRepository`）  
- **Model 層**：資料表對應的實體類別（例如：`User`, `Role`, `Todo`）
<br/>

### 資料庫設計
本系統主要資料表及其關聯設計如下所示：
- `users`：使用者  
- `roles`：角色  
- `users_roles`：使用者與角色關聯  
- `todos`：任務  
- `todo_items`：子任務  
- `messages`：留言  
<br/>

### 使用技術
- Java Spring Boot
- MySQL

## 本機安裝與使用

1. 取得原始碼
   ```bash
   git clone https://github.com/yourname/todo-backend.git
   cd todo-backend
   ```
   
2. 設定資料庫與環境  
   編輯 `src/main/resources/application.properties`，填入以下內容：

   ```properties
   spring.datasource.url=jdbc:mysql://localhost:3306/todo_db
   spring.datasource.username=root
   spring.datasource.password=yourpassword

   spring.jpa.hibernate.ddl-auto=update
   spring.jpa.show-sql=true

   # JWT 金鑰（自行更換）
   jwt.secret=your-secret-key
   ```
   
3. 啟動後端服務
   ```bash
   ./mvnw spring-boot:run
   #預設服務位置：http://localhost:8080
   ```
4. 資料庫建立預設帳號與角色，請手動在資料庫執行以下 SQL：
   ```bash
   USE todo_db;
   
   INSERT INTO roles (name)
   VALUES ('ROLE_ADMIN'), ('ROLE_USER');
   
   -- 管理員（password 請放 BCrypt 雜湊）
   INSERT INTO users (username, email, password)
   VALUES ('admin', 'admin@example.com', '$2a$10$yourBCryptHashHere');
   
   -- 指派管理員角色（請依實際 ID 調整）
   INSERT INTO users_roles (user_id, role_id)
   VALUES (1, 1);
   ```
5. API文件
   




