# 任務管理系統（Todo Management System）

# 系統介紹
- 本專案為 **任務管理系統（Task Management System）**，由練習基礎語法的 Todo Management System 延伸而來，後續嘗試將工作中接觸到的技術與研究概念融入系統設計，進一步擴充功能與介面。  
- 系統採用 **前後端分離架構** 開發：前端以 **React.js** 建立使用者介面，後端則以 **Spring Boot + MySQL** 開發 API 服務。  
- 功能涵蓋任務的新增、編輯、參與、刪除與完成，以及任務資訊與進度的即時呈現。 
- 本檔案為 **後端原始碼**，前端原始碼請見：(link)

# 功能介紹
- 使用者管理：註冊 / 登入 / JWT Token 驗證，支援角色權限分流。
- 任務管理：任務建立、更新、刪除、查詢，子任務建立、更新、刪除、查詢，任務參與、完成、審核
- 排行榜 API：提供協作參與次數與任務完成數資料查詢。

# 系統架構
### 程式分層設計
後端採用 **分層架構設計**，依職責將程式劃分Controller、Service、Repository 與 Model，各層責任如下：
- **Controller 層**：接收API請求，回傳 JSON 回應（例如：`AuthController`, `TodoController`）  
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

### 使用技術
- Java Spring Boot
- MySQL

## 本機安裝與使用

1. 建立資料庫
   ```bash
   CREATE DATABASE ecom_db;
   ```
2. 取得原始碼
   ```bash
   git clone https://github.com/felixven/todo-backend.git
   cd todo-backend
   ```
   
3. 設定資料庫與環境  
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
   
4. 啟動後端服務
   ```bash
   ./mvnw spring-boot:run
   #預設服務位置：http://localhost:8080
   ```
   
5. 資料庫建立預設Admin帳號與角色，請手動在資料庫執行以下 SQL：
   ```bash
   USE todo_db;
   
   INSERT INTO roles (name)
   VALUES ('ROLE_ADMIN'), ('ROLE_USER');
   
   -- 預先建立Admin權限資料
   INSERT INTO users (username, email, password)
   VALUES ('admin', 'admin@example.com', '$2a$10$d26pt/jjWFnSlLQkWCNWFuwbZf0A97Pg6ZGbw8ZejYoEx3V1dPWay');
   
   -- 指派管理員角色（請依實際 ID 調整）
   INSERT INTO users_roles (user_id, role_id)
   VALUES (1, 1);
   ```
   
6. 預設帳號
   專案啟動後會自動建立預設帳號，可直接登入測試，亦可透過註冊 API 建立新帳號：
   - Admin帳號
     - 帳號：
     - 密碼：
   - User帳號
     - 帳號：
     - 密碼：
7. 測試 API (Postman Collection)
  - 匯入本專案提供的 [Postman Collection](docs/todo-api.postman_collection.json)
  - 匯入本專案提供的 [Postman Environment](docs/local_env.json)
  - 開啟 Postman，選擇 `local_env` 環境，點選 **Run Collection**
    
- 範例測試流程（Admin權限可執行所有Api）：  
     1. 建立新的任務（Admin專有權限）  
        **Request**  
        `POST /api/todos`  

        **Body (JSON)**  
        ```json
        {
          "title": "learn vite2",
          "description": "learning",
          "dueDate": "2025-09-15"
        }
        ```
        
     2. 為任務加入子任務（Admin專有權限 ）
        **Request**  
        `POST api/todos/{id}/items`
        
     3. 編輯任務（Admin專有權限 ） 
        **Request**  
        `PUT /api/todos/{id}`  

        **Body (JSON)**  
        ```json
        {
          "title": "learn vite2",
          "description": "learning",
          "dueDate": "2025-08-31"
        }
        ```
        
     4. 完成子任務  
        **Request**  
        `GET api/todos/{id}/items/{id}/complete`
        
     5. 查詢任務參與者（Admin專有權限 ）  
        **Request**  
        `GET api/todos/{id}/participation`
        
     6. 查詢任務進度（Admin專有權限 ）  
        **Request**  
        `GET api/todos/{id}/items/summary`
        
     7. 完成任務  
        **Request**
        api/todos/{id}/complete

     8. 取消完成任務  
        **Request**
        api/todos/{id}/incomplete
  
     9. 審核任務（Admin專有權限，任務需要先被完成，才能執行此Api）  
        **Request**  
        `POST /api/todos/{id}/review`
        
        **Body (JSON)**  
        ```json
        {
          "title": "learn vite2",
          "description": "learning",
          "dueDate": "2025-08-31"
        }
        ```
  
    10. 留言  
        **Request**  
        `POST /api/todos/{id}/review`
        
        **Body (JSON)**  
        ```json
        {
          "title": "learn vite2",
          "description": "learning",
          "dueDate": "2025-08-31"
        }
        ```
    11. 查詢排行榜  
        **Request**  
        `POST /api/todos/{id}/review`
        
        **Body (JSON)**  
        ```json
        {
          "title": "learn vite2",
          "description": "learning",
          "dueDate": "2025-08-31"
        }
        ```
        
 - 其他API測試（一般User權限）
     1. 首先註冊帳號，便可使用帳號和密碼發送Api請求，於**Authorization** 標籤頁 → **Auth Type** 選擇 **Basic Auth**輸入帳號密碼
        **Request**  
        `POST /api/auth/register`  

        **Body (JSON)**  
        ```json
        {
          "username": "user1",
          "email": "user1@example.com",
          "password": "password1",
          "firstName":"user",
          "lastName":"user"
        }
        ```
     2. 通過驗證後便可以一般User權限皆可測試Admin專有權限以外之Api
        **完成任務、未完成任務、參與任務、留言**  
        `POST /api/todos`
        **未完成任務**  
        `POST /api/todos`
        **未完成任務**  
        `POST /api/todos`


   




