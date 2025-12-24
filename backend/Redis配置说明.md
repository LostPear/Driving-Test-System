# Redis 缓存配置说明

## 概述

系统已集成 Redis 缓存，用于提高访问速度和减少数据库负载。Redis 是可选的，如果 Redis 不可用，系统会自动降级到直接访问数据库。

## 安装 Redis

### Windows
1. 下载 Redis for Windows: https://github.com/microsoftarchive/redis/releases
2. 解压并运行 `redis-server.exe`
3. 默认端口: 6379

### Linux/Mac
```bash
# Ubuntu/Debian
sudo apt-get install redis-server

# CentOS/RHEL
sudo yum install redis

# Mac
brew install redis

# 启动 Redis
redis-server
```

## 配置

Redis 配置通过系统属性（System Properties）设置，可以在启动 Tomcat 时通过 JVM 参数配置：

```bash
-Dredis.host=localhost
-Dredis.port=6379
-Dredis.password=your_password  # 可选，如果Redis设置了密码
-Dredis.database=0  # 可选，默认0
```

### 在 Tomcat 中配置

编辑 `catalina.sh` (Linux/Mac) 或 `catalina.bat` (Windows)，添加：

```bash
# Linux/Mac
export JAVA_OPTS="$JAVA_OPTS -Dredis.host=localhost -Dredis.port=6379"

# Windows
set JAVA_OPTS=%JAVA_OPTS% -Dredis.host=localhost -Dredis.port=6379
```

或者在 `setenv.sh` / `setenv.bat` 中配置。

## 缓存策略

### 1. 用户信息缓存
- **键格式**: `user:{userId}`
- **过期时间**: 30分钟 (1800秒)
- **缓存位置**: `UserDAO.getUserById()`
- **清除时机**: 用户信息更新时自动清除

### 2. 题目信息缓存
- **键格式**: `question:{questionId}`
- **过期时间**: 1小时 (3600秒)
- **缓存位置**: `QuestionDAO.getQuestionById()`
- **清除时机**: 题目创建/更新/删除时自动清除

### 3. 用户统计数据缓存
- **键格式**: `user_stats:{userId}`
- **过期时间**: 5分钟 (300秒)
- **缓存位置**: `UserManagementDAO.getUserStats()`
- **清除时机**: 用户进行考试或练习后自动清除（通过过期时间）

### 4. 管理员统计数据缓存
- **键格式**: `admin_stats`
- **过期时间**: 5分钟 (300秒)
- **缓存位置**: `UserManagementServlet.handleGetAdminStats()`
- **清除时机**: 通过过期时间自动刷新

### 5. 收藏列表缓存
- **键格式**: `favorites:{userId}`
- **过期时间**: 10分钟 (600秒)
- **缓存位置**: `FavoriteDAO` (计划中)
- **清除时机**: 添加/删除收藏时自动清除

## 性能优化效果

使用 Redis 缓存后，预期性能提升：

1. **用户信息查询**: 从数据库查询（~10-50ms）降低到缓存查询（~1-5ms）
2. **题目信息查询**: 从数据库查询（~10-50ms）降低到缓存查询（~1-5ms）
3. **统计数据查询**: 从复杂聚合查询（~100-500ms）降低到缓存查询（~1-5ms）

## 故障降级

如果 Redis 不可用或连接失败：
- 系统会自动检测并降级到直接访问数据库
- 不会影响系统正常运行
- 日志会记录 Redis 连接失败信息

## 监控和调试

### 检查 Redis 连接状态
查看应用日志，查找 "Redis连接成功" 或 "Redis连接失败" 消息。

### 使用 Redis CLI 查看缓存
```bash
redis-cli

# 查看所有键
KEYS *

# 查看特定键
GET user:1
GET question:100

# 查看键的过期时间
TTL user:1

# 删除键
DEL user:1
```

## 注意事项

1. **数据一致性**: 缓存会在数据更新时自动清除，但建议定期检查缓存一致性
2. **内存使用**: 根据数据量调整 Redis 的 `maxmemory` 配置
3. **持久化**: 生产环境建议启用 Redis 持久化（RDB 或 AOF）
4. **高可用**: 生产环境建议使用 Redis Sentinel 或 Redis Cluster

## 禁用 Redis

如果不想使用 Redis，只需不启动 Redis 服务即可。系统会自动检测并跳过缓存。


