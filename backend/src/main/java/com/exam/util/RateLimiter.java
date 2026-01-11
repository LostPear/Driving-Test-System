package com.exam.util;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import redis.clients.jedis.Jedis;

/**
 * 基于Redis的速率限制器
 * 使用滑动窗口算法实现速率限制
 */
public class RateLimiter {
    private static final Logger logger = LogManager.getLogger(RateLimiter.class);
    private static final String RATE_LIMIT_KEY_PREFIX = "rate_limit:";
    
    /**
     * 检查是否超过速率限制
     * 
     * @param identifier 标识符（如IP地址或用户ID）
     * @param limit 限制次数
     * @param windowSeconds 时间窗口（秒）
     * @return true表示未超过限制，false表示超过限制
     */
    public static boolean isAllowed(String identifier, int limit, int windowSeconds) {
        if (!RedisUtil.isAvailable()) {
            // 如果Redis不可用，允许请求通过（降级处理）
            return true;
        }
        
        Jedis jedis = null;
        try {
            String key = RATE_LIMIT_KEY_PREFIX + identifier;
            long now = System.currentTimeMillis();
            long windowStart = now - (windowSeconds * 1000L);
            
            jedis = RedisUtil.getJedis();
            if (jedis == null) {
                return true;
            }
            
            // 移除窗口外的记录
            jedis.zremrangeByScore(key, 0, windowStart);
            
            // 获取当前窗口内的请求数
            Long count = jedis.zcard(key);
            
            if (count == null || count < limit) {
                // 未超过限制，添加当前请求
                jedis.zadd(key, now, String.valueOf(now));
                jedis.expire(key, windowSeconds);
                return true;
            } else {
                // 超过限制
                return false;
            }
        } catch (Exception e) {
            logger.error("速率限制检查失败: identifier={}, error={}", identifier, e.getMessage());
            // 发生错误时允许请求通过（降级处理）
            return true;
        } finally {
            RedisUtil.returnJedis(jedis);
        }
    }
    
    /**
     * 获取当前窗口内的请求数
     * 
     * @param identifier 标识符
     * @param windowSeconds 时间窗口（秒）
     * @return 当前请求数
     */
    public static long getCurrentCount(String identifier, int windowSeconds) {
        if (!RedisUtil.isAvailable()) {
            return 0;
        }
        
        Jedis jedis = null;
        try {
            String key = RATE_LIMIT_KEY_PREFIX + identifier;
            long now = System.currentTimeMillis();
            long windowStart = now - (windowSeconds * 1000L);
            
            jedis = RedisUtil.getJedis();
            if (jedis == null) {
                return 0;
            }
            
            // 移除窗口外的记录
            jedis.zremrangeByScore(key, 0, windowStart);
            
            // 获取当前窗口内的请求数
            Long count = jedis.zcard(key);
            return count != null ? count : 0;
        } catch (Exception e) {
            logger.error("获取速率限制计数失败: identifier={}, error={}", identifier, e.getMessage());
            return 0;
        } finally {
            RedisUtil.returnJedis(jedis);
        }
    }
}
