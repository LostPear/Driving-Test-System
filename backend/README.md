# 驾照理论考试系统 - 后端

基于 Java Servlet 的驾照理论考试系统后端服务。

## 技术栈

- **Java**: JDK 1.8+
- **Servlet**: 4.0
- **数据库**: MySQL 5.7+
- **构建工具**: Maven
- **JSON处理**: Jackson
- **JWT认证**: jjwt

## 项目结构

```
backend/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/exam/
│       │       ├── dao/          # 数据访问层
│       │       ├── model/         # 数据模型
│       │       ├── servlet/       # Servlet控制器
│       │       └── util/          # 工具类
│       └── webapp/
│           └── WEB-INF/
│               └── web.xml        # Web配置文件
├── database/
│   └── schema.sql                 # 数据库表结构
├── pom.xml                        # Maven配置文件
└── README.md                      # 本文档
```

## 环境要求

1. **JDK**: 1.8 或更高版本
2. **Maven**: 3.6 或更高版本
3. **MySQL**: 5.7 或更高版本
4. **Tomcat**: 9.0 或更高版本（或其他支持Servlet 4.0的服务器）

## 配置步骤

### 1. 数据库配置

#### 1.1 创建数据库

执行 `database/schema.sql` 文件创建数据库和表：

```bash
mysql -u root -p < database/schema.sql
```

或者手动执行SQL语句：

```sql
CREATE DATABASE IF NOT EXISTS exam_db CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE exam_db;
-- 然后执行schema.sql中的其他SQL语句
```

#### 1.2 修改数据库连接配置

编辑 `src/main/java/com/exam/util/DBUtil.java`，修改以下配置：

```java
private static final String DB_URL = "jdbc:mysql://localhost:3306/exam_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8";
private static final String DB_USER = "root";        // 修改为你的数据库用户名
private static final String DB_PASSWORD = "root";    // 修改为你的数据库密码
```

### 2. Maven 构建

在项目根目录（backend/）执行：

```bash
mvn clean package
```

构建成功后，会在 `target/` 目录下生成 `exam-backend-1.0.0.war` 文件。

### 3. 部署到 Tomcat

#### 方式一：直接部署 WAR 文件

1. 将生成的 `exam-backend-1.0.0.war` 复制到 Tomcat 的 `webapps/` 目录
2. 启动 Tomcat
3. 访问 `http://localhost:8080/exam-backend-1.0.0/api/...`

#### 方式二：使用 Maven Tomcat 插件（开发环境）

在 `pom.xml` 中添加 Tomcat 插件：

```xml
<plugin>
    <groupId>org.apache.tomcat.maven</groupId>
    <artifactId>tomcat7-maven-plugin</artifactId>
    <version>2.2</version>
    <configuration>
        <port>8000</port>
        <path>/</path>
    </configuration>
</plugin>
```

然后运行：

```bash
mvn tomcat7:run
```

#### 方式三：使用 IDE（推荐开发环境）

1. **IntelliJ IDEA**:
   - 右键项目 -> Add Framework Support -> Web Application
   - 配置 Tomcat 运行配置
   - 设置 Application context 为 `/`
   - 设置端口为 `8000`

2. **Eclipse**:
   - 右键项目 -> Properties -> Project Facets -> 勾选 Dynamic Web Project
   - 配置 Server Runtime
   - 添加到 Tomcat 服务器并运行

### 4. 验证部署

启动服务后，访问以下URL验证：

- 健康检查: `http://localhost:8000/api/` （应该返回404或错误，说明服务已启动）
- 注册接口: `POST http://localhost:8000/api/auth/register/`

## API 接口说明

### 认证接口

- `POST /api/auth/register/` - 用户注册
- `POST /api/auth/login/` - 用户登录
- `POST /api/auth/logout/` - 用户登出
- `PUT /api/auth/profile/` - 更新个人资料（需要认证）
- `POST /api/auth/change-password/` - 修改密码（需要认证）

### 题目接口

- `GET /api/questions/` - 获取题目列表（支持分页和筛选）
- `GET /api/questions/{id}/` - 获取单个题目
- `POST /api/questions/` - 创建题目（需要认证）
- `PUT /api/questions/{id}/` - 更新题目（需要认证）
- `DELETE /api/questions/{id}/` - 删除题目（需要认证）
- `GET /api/questions/random/` - 获取随机题目
- `GET /api/questions/sequential/` - 获取顺序题目
- `POST /api/questions/import/` - 批量导入题目（需要认证，暂未实现）

### 考试接口

- `POST /api/exams/` - 创建考试（需要认证）
- `GET /api/exams/{id}/` - 获取考试详情（需要认证）
- `POST /api/exams/{id}/submit/` - 提交考试（需要认证）
- `GET /api/exams/{id}/result/` - 获取考试结果（需要认证）
- `GET /api/exams/history/` - 获取考试历史（需要认证）

### 用户管理接口（仅管理员）

- `GET /api/users/` - 获取用户列表
- `GET /api/users/{id}/` - 获取用户详情
- `PUT /api/users/{id}/` - 更新用户
- `DELETE /api/users/{id}/` - 删除用户
- `GET /api/users/{id}/stats/` - 获取用户统计

## 默认账户

数据库初始化后会创建以下默认账户：

- **管理员**: 
  - 用户名: `admin`
  - 密码: `admin`
  
- **普通用户**: 
  - 用户名: `user`
  - 密码: `password`

## 认证方式

所有需要认证的接口都需要在请求头中携带 JWT Token：

```
Authorization: Bearer <token>
```

登录成功后，响应中会返回 `token` 字段，前端需要保存并在后续请求中携带。

## 数据库表结构

### users 表
- `id`: 用户ID（主键）
- `username`: 用户名（唯一）
- `email`: 邮箱（唯一）
- `password`: 密码（SHA-256加密）
- `role`: 角色（user/admin）
- `created_at`: 创建时间

### questions 表
- `id`: 题目ID（主键）
- `type`: 题目类型（single/judge）
- `difficulty`: 难度（easy/medium/hard）
- `question`: 题目内容
- `options`: 选项（JSON格式）
- `correct_answer`: 正确答案索引
- `explanation`: 解析
- `created_at`: 创建时间

### exams 表
- `id`: 考试ID（主键）
- `user_id`: 用户ID（外键）
- `questions`: 题目ID列表（JSON格式）
- `answers`: 用户答案（JSON格式，questionId -> answer）
- `score`: 分数
- `passed`: 是否通过
- `created_at`: 创建时间
- `submitted_at`: 提交时间

## 常见问题

### 1. 数据库连接失败

- 检查 MySQL 服务是否启动
- 检查 `DBUtil.java` 中的数据库连接配置是否正确
- 检查数据库用户是否有足够权限
- 确认数据库 `exam_db` 已创建

### 2. 端口冲突

如果 8000 端口被占用，可以：
- 修改 Tomcat 配置使用其他端口
- 或者修改前端 `vite.config.js` 中的代理配置

### 3. CORS 跨域问题

如果遇到跨域问题，检查 `CorsFilter.java` 和 `web.xml` 中的 CORS 配置是否正确。

### 4. JWT Token 验证失败

- 检查请求头中是否正确携带 `Authorization: Bearer <token>`
- 检查 Token 是否过期（默认24小时）
- 检查 `JwtUtil.java` 中的密钥配置

## 开发建议

1. **日志**: 建议添加日志框架（如 Log4j 或 SLF4J）用于调试
2. **异常处理**: 可以添加全局异常处理器统一处理异常
3. **连接池**: 生产环境建议使用数据库连接池（如 HikariCP）
4. **参数验证**: 建议添加参数验证框架（如 Hibernate Validator）
5. **单元测试**: 建议为 DAO 和 Servlet 添加单元测试

## 许可证

MIT

