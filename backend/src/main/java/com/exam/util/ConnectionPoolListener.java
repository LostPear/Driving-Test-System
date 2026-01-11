package com.exam.util;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 连接池监听器
 * 在应用关闭时优雅地关闭HikariCP连接池
 */
public class ConnectionPoolListener implements ServletContextListener {
    private static final Logger logger = LogManager.getLogger(ConnectionPoolListener.class);
    
    @Override
    public void contextInitialized(ServletContextEvent sce) {
        logger.info("应用启动，HikariCP连接池将在首次使用时初始化");
    }
    
    @Override
    public void contextDestroyed(ServletContextEvent sce) {
        logger.info("应用关闭，开始关闭HikariCP连接池...");
        DBUtil.shutdown();
        logger.info("HikariCP连接池已关闭");
    }
}
