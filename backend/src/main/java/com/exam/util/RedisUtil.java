package com.exam.util;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.Set;

public class RedisUtil {
    private static final Logger logger = LogManager.getLogger(RedisUtil.class);
    
    private static JedisPool jedisPool;
    private static final ObjectMapper objectMapper = new ObjectMapper();
    
    // Redis配置
    private static final String REDIS_HOST = System.getProperty("redis.host", "localhost");
    private static final int REDIS_PORT = Integer.parseInt(System.getProperty("redis.port", "6379"));
    private static final String REDIS_PASSWORD = System.getProperty("redis.password", null);
    private static final int REDIS_DATABASE = Integer.parseInt(System.getProperty("redis.database", "0"));
    
    // 缓存键前缀
    private static final String KEY_PREFIX_USER = "user:";
    private static final String KEY_PREFIX_QUESTION = "question:";
    private static final String KEY_PREFIX_USER_STATS = "user_stats:";
    private static final String KEY_PREFIX_ADMIN_STATS = "admin_stats";
    private static final String KEY_PREFIX_FAVORITES = "favorites:";
    
    static {
        try {
            JedisPoolConfig config = new JedisPoolConfig();
            config.setMaxTotal(200); // 最大连接数
            config.setMaxIdle(20); // 最大空闲连接数
            config.setMinIdle(5); // 最小空闲连接数
            config.setTestOnBorrow(true); // 获取连接时检查有效性
            config.setTestOnReturn(true); // 归还连接时检查有效性
            config.setTestWhileIdle(true); // 空闲时检查有效性
            config.setMaxWaitMillis(3000); // 获取连接最大等待时间
            
            if (REDIS_PASSWORD != null && !REDIS_PASSWORD.isEmpty()) {
                jedisPool = new JedisPool(config, REDIS_HOST, REDIS_PORT, 3000, REDIS_PASSWORD, REDIS_DATABASE);
            } else {
                jedisPool = new JedisPool(config, REDIS_HOST, REDIS_PORT, 3000, null, REDIS_DATABASE);
            }
            
            // 测试连接
            try (Jedis jedis = jedisPool.getResource()) {
                jedis.ping();
                logger.info("Redis连接成功: {}:{}", REDIS_HOST, REDIS_PORT);
            }
        } catch (Exception e) {
            logger.error("Redis连接失败: {}", e.getMessage());
            jedisPool = null;
        }
    }
    
    /**
     * 获取Jedis连接
     */
    private static Jedis getJedis() {
        if (jedisPool == null) {
            return null;
        }
        try {
            return jedisPool.getResource();
        } catch (Exception e) {
            logger.error("获取Redis连接失败: {}", e.getMessage());
            return null;
        }
    }
    
    /**
     * 检查Redis是否可用
     */
    public static boolean isAvailable() {
        if (jedisPool == null) {
            return false;
        }
        try (Jedis jedis = getJedis()) {
            if (jedis == null) {
                return false;
            }
            return "PONG".equals(jedis.ping());
        } catch (Exception e) {
            return false;
        }
    }
    
    /**
     * 设置缓存（带过期时间，单位：秒）
     */
    public static void set(String key, String value, int expireSeconds) {
        if (!isAvailable()) {
            return;
        }
        try (Jedis jedis = getJedis()) {
            if (jedis != null) {
                if (expireSeconds > 0) {
                    jedis.setex(key, expireSeconds, value);
                } else {
                    jedis.set(key, value);
                }
            }
        } catch (Exception e) {
            logger.error("Redis设置缓存失败: key={}, error={}", key, e.getMessage());
        }
    }
    
    /**
     * 设置对象缓存（带过期时间，单位：秒）
     */
    public static void setObject(String key, Object obj, int expireSeconds) {
        try {
            String json = objectMapper.writeValueAsString(obj);
            set(key, json, expireSeconds);
        } catch (Exception e) {
            logger.error("Redis设置对象缓存失败: key={}, error={}", key, e.getMessage());
        }
    }
    
    /**
     * 获取缓存
     */
    public static String get(String key) {
        if (!isAvailable()) {
            return null;
        }
        try (Jedis jedis = getJedis()) {
            if (jedis != null) {
                return jedis.get(key);
            }
        } catch (Exception e) {
            logger.error("Redis获取缓存失败: key={}, error={}", key, e.getMessage());
        }
        return null;
    }
    
    /**
     * 获取对象缓存
     */
    public static <T> T getObject(String key, Class<T> clazz) {
        String json = get(key);
        if (json == null) {
            return null;
        }
        try {
            return objectMapper.readValue(json, clazz);
        } catch (Exception e) {
            logger.error("Redis获取对象缓存失败: key={}, error={}", key, e.getMessage());
            return null;
        }
    }
    
    /**
     * 删除缓存
     */
    public static void delete(String key) {
        if (!isAvailable()) {
            return;
        }
        try (Jedis jedis = getJedis()) {
            if (jedis != null) {
                jedis.del(key);
            }
        } catch (Exception e) {
            logger.error("Redis删除缓存失败: key={}, error={}", key, e.getMessage());
        }
    }
    
    /**
     * 删除匹配的缓存
     */
    public static void deletePattern(String pattern) {
        if (!isAvailable()) {
            return;
        }
        try (Jedis jedis = getJedis()) {
            if (jedis != null) {
                Set<String> keys = jedis.keys(pattern);
                if (keys != null && !keys.isEmpty()) {
                    jedis.del(keys.toArray(new String[0]));
                }
            }
        } catch (Exception e) {
            logger.error("Redis删除匹配缓存失败: pattern={}, error={}", pattern, e.getMessage());
        }
    }
    
    /**
     * 检查缓存是否存在
     */
    public static boolean exists(String key) {
        if (!isAvailable()) {
            return false;
        }
        try (Jedis jedis = getJedis()) {
            if (jedis != null) {
                return jedis.exists(key);
            }
        } catch (Exception e) {
            logger.error("Redis检查缓存失败: key={}, error={}", key, e.getMessage());
        }
        return false;
    }
    
    /**
     * 设置过期时间
     */
    public static void expire(String key, int seconds) {
        if (!isAvailable()) {
            return;
        }
        try (Jedis jedis = getJedis()) {
            if (jedis != null) {
                jedis.expire(key, seconds);
            }
        } catch (Exception e) {
            logger.error("Redis设置过期时间失败: key={}, error={}", key, e.getMessage());
        }
    }
    
    // ========== 用户相关缓存 ==========
    
    /**
     * 获取用户缓存键
     */
    public static String getUserKey(int userId) {
        return KEY_PREFIX_USER + userId;
    }
    
    /**
     * 设置用户缓存（30分钟过期）
     */
    public static void setUser(int userId, Object user) {
        setObject(getUserKey(userId), user, 1800);
    }
    
    /**
     * 获取用户缓存
     */
    public static <T> T getUser(int userId, Class<T> clazz) {
        return getObject(getUserKey(userId), clazz);
    }
    
    /**
     * 删除用户缓存
     */
    public static void deleteUser(int userId) {
        delete(getUserKey(userId));
    }
    
    // ========== 题目相关缓存 ==========
    
    /**
     * 获取题目缓存键
     */
    public static String getQuestionKey(int questionId) {
        return KEY_PREFIX_QUESTION + questionId;
    }
    
    /**
     * 设置题目缓存（1小时过期）
     */
    public static void setQuestion(int questionId, Object question) {
        setObject(getQuestionKey(questionId), question, 3600);
    }
    
    /**
     * 获取题目缓存
     */
    public static <T> T getQuestion(int questionId, Class<T> clazz) {
        return getObject(getQuestionKey(questionId), clazz);
    }
    
    /**
     * 删除题目缓存
     */
    public static void deleteQuestion(int questionId) {
        delete(getQuestionKey(questionId));
    }
    
    /**
     * 删除所有题目缓存
     */
    public static void deleteAllQuestions() {
        deletePattern(KEY_PREFIX_QUESTION + "*");
    }
    
    // ========== 统计数据相关缓存 ==========
    
    /**
     * 获取用户统计数据缓存键
     */
    public static String getUserStatsKey(int userId) {
        return KEY_PREFIX_USER_STATS + userId;
    }
    
    /**
     * 设置用户统计数据缓存（5分钟过期）
     */
    public static void setUserStats(int userId, Object stats) {
        setObject(getUserStatsKey(userId), stats, 300);
    }
    
    /**
     * 获取用户统计数据缓存
     */
    public static <T> T getUserStats(int userId, Class<T> clazz) {
        return getObject(getUserStatsKey(userId), clazz);
    }
    
    /**
     * 删除用户统计数据缓存
     */
    public static void deleteUserStats(int userId) {
        delete(getUserStatsKey(userId));
    }
    
    /**
     * 获取管理员统计数据缓存键
     */
    public static String getAdminStatsKey() {
        return KEY_PREFIX_ADMIN_STATS;
    }
    
    /**
     * 设置管理员统计数据缓存（5分钟过期）
     */
    public static void setAdminStats(Object stats) {
        setObject(getAdminStatsKey(), stats, 300);
    }
    
    /**
     * 获取管理员统计数据缓存
     */
    public static <T> T getAdminStats(Class<T> clazz) {
        return getObject(getAdminStatsKey(), clazz);
    }
    
    /**
     * 删除管理员统计数据缓存
     */
    public static void deleteAdminStats() {
        delete(getAdminStatsKey());
    }
    
    // ========== 收藏相关缓存 ==========
    
    /**
     * 获取用户收藏列表缓存键
     */
    public static String getFavoritesKey(int userId) {
        return KEY_PREFIX_FAVORITES + userId;
    }
    
    /**
     * 设置用户收藏列表缓存（10分钟过期）
     */
    public static void setFavorites(int userId, Object favorites) {
        setObject(getFavoritesKey(userId), favorites, 600);
    }
    
    /**
     * 获取用户收藏列表缓存
     */
    public static <T> T getFavorites(int userId, Class<T> clazz) {
        return getObject(getFavoritesKey(userId), clazz);
    }
    
    /**
     * 删除用户收藏列表缓存
     */
    public static void deleteFavorites(int userId) {
        delete(getFavoritesKey(userId));
    }
    
    /**
     * 关闭连接池
     */
    public static void close() {
        if (jedisPool != null && !jedisPool.isClosed()) {
            jedisPool.close();
        }
    }
}

