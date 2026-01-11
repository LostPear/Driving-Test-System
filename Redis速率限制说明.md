# Redis速率限制配置说明

## 一、Redis状态确认

项目已经集成了Redis，相关配置如下：

### 1. 依赖检查
- ✅ `pom.xml` 中已引入 `jedis 4.3.1`
- ✅ `RedisUtil.java` 已实现Redis连接和缓存功能
- ✅ 项目中已在多处使用Redis缓存（用户信息、题目信息、统计数据等）

### 2. Redis配置
Redis配置通过系统属性设置，启动Tomcat时可通过JVM参数配置：

```bash
-Dredis.host=localhost
-Dredis.port=6379
-Dredis.password=your_password  # 可选
-Dredis.database=0  # 可选，默认0
```

## 二、速率限制功能

### 1. 实现说明

已实现基于Redis的API速率限制功能，包括：

- **RateLimiter.java**：速率限制核心工具类
  - 使用滑动窗口算法（Sliding Window）
  - 基于Redis Sorted Set实现
  - 自动清理过期记录

- **RateLimitFilter.java**：速率限制过滤器
  - 拦截所有 `/api/*` 请求
  - 根据不同API路径设置不同的速率限制
  - 返回HTTP 429状态码（Too Many Requests）

### 2. 速率限制策略

根据API类型设置不同的速率限制：

| API类型 | 限制次数 | 时间窗口 | 说明 |
|---------|---------|---------|------|
| 认证接口 (`/api/auth/`) | 10次 | 60秒 | 严格限制，防止暴力破解 |
| 提交类接口 (`/api/exams/submit/`, POST `/api/questions/`) | 100次 | 60秒 | 中等限制，防止恶意提交 |
| 查询类接口（其他） | 200次 | 60秒 | 较宽松限制，支持正常查询 |

### 3. 标识符策略

速率限制基于以下标识符：

1. **已登录用户**：使用用户ID（`user:{userId}`）
   - 优势：可以为不同用户设置个性化限制
   - 更精确地识别和限制特定用户

2. **未登录用户**：使用IP地址（`ip:{ipAddress}`）
   - 从请求头中获取真实IP（支持代理环境）
   - 支持 `X-Forwarded-For`、`Proxy-Client-IP` 等头信息

## 三、使用说明

### 1. 启动Redis服务

**Windows:**
```bash
redis-server.exe
```

**Linux/Mac:**
```bash
redis-server
```

### 2. 配置Redis连接（可选）

如果Redis不在默认位置（localhost:6379），需要在启动Tomcat时配置：

**Linux/Mac (catalina.sh):**
```bash
export JAVA_OPTS="$JAVA_OPTS -Dredis.host=localhost -Dredis.port=6379"
```

**Windows (catalina.bat):**
```bat
set JAVA_OPTS=%JAVA_OPTS% -Dredis.host=localhost -Dredis.port=6379
```

### 3. 验证速率限制

**测试速率限制：**

```bash
# 快速连续请求（超过限制）
for i in {1..15}; do
  curl -X POST http://localhost:8000/api/auth/login \
    -H "Content-Type: application/json" \
    -d '{"username":"test","password":"test"}'
  echo ""
done
```

预期结果：前10个请求成功，第11个请求返回429状态码。

**查看响应：**
```json
{
  "error": "请求过于频繁，请稍后再试"
}
```

状态码：`429 Too Many Requests`

## 四、技术实现细节

### 1. 滑动窗口算法

使用Redis Sorted Set实现滑动窗口：

1. **键结构**：`rate_limit:{identifier}`
2. **存储方式**：
   - Score：请求时间戳（毫秒）
   - Member：时间戳字符串（用于唯一标识）
3. **清理机制**：
   - 每次检查时自动移除窗口外的记录
   - 使用 `ZREMRANGEBYSCORE` 命令清理过期数据
4. **计数方式**：使用 `ZCARD` 命令统计当前窗口内的请求数

### 2. 降级处理

如果Redis不可用：
- 自动降级，允许所有请求通过
- 记录错误日志
- 不影响系统正常运行

### 3. 性能优化

- 使用Redis Sorted Set，查询和更新性能优异
- 自动过期机制，避免内存泄漏
- 单次操作，原子性保证

## 五、自定义配置

如需修改速率限制参数，编辑 `RateLimitFilter.java`：

```java
// 认证接口限制
private static final int AUTH_LIMIT = 10;      // 修改限制次数
private static final int AUTH_WINDOW = 60;     // 修改时间窗口（秒）

// 普通API限制
private static final int API_LIMIT = 100;
private static final int API_WINDOW = 60;

// 查询接口限制
private static final int QUERY_LIMIT = 200;
private static final int QUERY_WINDOW = 60;
```

## 六、监控和调试

### 1. 查看速率限制日志

查看应用日志，搜索 "速率限制触发"：

```
WARN  - 速率限制触发: identifier=ip:127.0.0.1, path=/api/auth/login, limit=10/60s
```

### 2. 使用Redis CLI查看

```bash
redis-cli

# 查看所有速率限制键
KEYS rate_limit:*

# 查看特定标识符的请求记录
ZRANGE rate_limit:ip:127.0.0.1 0 -1 WITHSCORES

# 查看键的过期时间
TTL rate_limit:ip:127.0.0.1

# 清除速率限制数据（调试用）
DEL rate_limit:ip:127.0.0.1
```

### 3. 测试建议

1. **正常使用测试**：验证正常请求不受影响
2. **边界测试**：测试刚好达到限制和超过限制的情况
3. **时间窗口测试**：验证时间窗口过期后限制重置
4. **多用户测试**：验证不同用户/IP的限速相互独立

## 七、注意事项

1. **Redis可用性**：确保Redis服务正常运行，否则速率限制功能失效（但系统仍可正常运行）
2. **内存使用**：速率限制数据会自动过期，但大量并发用户时仍会占用一定内存
3. **时间同步**：确保服务器时间准确，时间不同步可能导致速率限制不准确
4. **代理环境**：如果应用部署在反向代理后，确保正确配置IP获取逻辑

## 八、故障排查

### 问题1：速率限制不生效

**可能原因：**
- Redis未启动
- Redis连接配置错误
- 过滤器未正确注册

**解决方法：**
1. 检查Redis服务状态
2. 查看应用日志中的Redis连接信息
3. 检查 `web.xml` 中的过滤器配置

### 问题2：正常用户被限制

**可能原因：**
- 限制阈值设置过低
- 多个用户共享同一IP（NAT环境）

**解决方法：**
1. 适当提高限制阈值
2. 考虑为已登录用户使用更宽松的限制
3. 使用用户ID而非IP进行限制（已实现）

### 问题3：Redis连接失败

**症状：** 日志中显示 "Redis连接失败"

**解决方法：**
1. 检查Redis服务是否运行
2. 检查连接配置（host、port、password）
3. 检查防火墙设置
4. 速率限制会自动降级，不影响系统运行

## 九、总结

速率限制功能已成功集成：

- ✅ 基于Redis实现，性能优异
- ✅ 使用滑动窗口算法，准确可靠
- ✅ 不同API类型设置不同限制策略
- ✅ 支持用户ID和IP地址双重标识
- ✅ 自动降级处理，保证系统可用性
- ✅ 返回HTTP 429标准状态码

该功能可以有效防止：
- 暴力破解攻击
- API滥用
- DDoS攻击
- 恶意刷接口
