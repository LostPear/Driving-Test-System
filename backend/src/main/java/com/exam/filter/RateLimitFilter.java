package com.exam.filter;

import com.exam.util.AuthUtil;
import com.exam.util.RateLimiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * API速率限制过滤器
 * 基于IP地址和用户ID进行速率限制
 */
public class RateLimitFilter implements Filter {
    private static final Logger logger = LogManager.getLogger(RateLimitFilter.class);
    
    // 默认速率限制配置（可根据不同路径配置不同限制）
    // 格式：路径 -> {限制次数, 时间窗口(秒)}
    
    // 认证相关接口：更严格的限制（防止暴力破解）
    private static final int AUTH_LIMIT = 10; // 10次
    private static final int AUTH_WINDOW = 60; // 60秒
    
    // 普通API接口：中等限制
    private static final int API_LIMIT = 100; // 100次
    private static final int API_WINDOW = 60; // 60秒
    
    // 数据查询接口：较宽松的限制
    private static final int QUERY_LIMIT = 200; // 200次
    private static final int QUERY_WINDOW = 60; // 60秒
    
    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        logger.info("速率限制过滤器初始化");
    }
    
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;
        
        String path = httpRequest.getRequestURI();
        String method = httpRequest.getMethod();
        
        // 跳过OPTIONS请求（CORS预检请求）
        if ("OPTIONS".equalsIgnoreCase(method)) {
            chain.doFilter(request, response);
            return;
        }
        
        // 获取标识符（优先使用用户ID，否则使用IP地址）
        String identifier = getIdentifier(httpRequest);
        
        // 根据路径选择不同的速率限制策略
        int limit;
        int window;
        
        if (path.startsWith("/api/auth/")) {
            // 认证接口：严格限制
            limit = AUTH_LIMIT;
            window = AUTH_WINDOW;
        } else if (path.startsWith("/api/exams/submit/") || 
                   path.startsWith("/api/questions/") && "POST".equals(method)) {
            // 提交类接口：中等限制
            limit = API_LIMIT;
            window = API_WINDOW;
        } else {
            // 查询类接口：较宽松的限制
            limit = QUERY_LIMIT;
            window = QUERY_WINDOW;
        }
        
        // 检查速率限制
        if (!RateLimiter.isAllowed(identifier, limit, window)) {
            logger.warn("速率限制触发: identifier={}, path={}, limit={}/{}s", identifier, path, limit, window);
            httpResponse.setStatus(429); // HTTP 429 Too Many Requests
            httpResponse.setContentType("application/json;charset=UTF-8");
            httpResponse.getWriter().write("{\"error\": \"请求过于频繁，请稍后再试\"}");
            return;
        }
        
        chain.doFilter(request, response);
    }
    
    /**
     * 获取请求标识符
     * 优先使用用户ID（如果已登录），否则使用IP地址
     */
    private String getIdentifier(HttpServletRequest request) {
        try {
            // 尝试获取用户ID（如果用户已登录）
            if (AuthUtil.isAuthenticated(request)) {
                int userId = AuthUtil.getUserId(request);
                return "user:" + userId;
            }
        } catch (Exception e) {
            // 忽略异常，使用IP地址
        }
        
        // 使用IP地址作为标识符
        String ip = getClientIpAddress(request);
        return "ip:" + ip;
    }
    
    /**
     * 获取客户端IP地址
     */
    private String getClientIpAddress(HttpServletRequest request) {
        String ip = request.getHeader("X-Forwarded-For");
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("WL-Proxy-Client-IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_CLIENT_IP");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getHeader("HTTP_X_FORWARDED_FOR");
        }
        if (ip == null || ip.isEmpty() || "unknown".equalsIgnoreCase(ip)) {
            ip = request.getRemoteAddr();
        }
        
        // 处理多个IP的情况（X-Forwarded-For可能包含多个IP）
        if (ip != null && ip.contains(",")) {
            ip = ip.split(",")[0].trim();
        }
        
        return ip != null ? ip : "unknown";
    }
    
    @Override
    public void destroy() {
        logger.info("速率限制过滤器销毁");
    }
}
