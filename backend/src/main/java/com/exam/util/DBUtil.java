package com.exam.util;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * 数据库连接工具类
 * 使用HikariCP连接池管理数据库连接，提升高并发场景下的性能
 */
public class DBUtil {
    private static final Logger logger = LogManager.getLogger(DBUtil.class);
    
    // HikariCP数据源（单例模式）
    private static HikariDataSource dataSource;
    
    // 配置文件路径
    private static final String CONFIG_FILE = "db.properties";
    
    static {
        initializeDataSource();
    }
    
    /**
     * 初始化HikariCP连接池
     * 从db.properties配置文件读取所有配置，如果配置文件不存在或某项配置缺失，则使用默认值
     */
    private static void initializeDataSource() {
        try {
            HikariConfig config = new HikariConfig();
            
            // 加载配置文件
            Properties props = loadProperties();
            
            // ========== 数据库连接配置 ==========
            String url = getProperty(props, "db.url", 
                "jdbc:mysql://localhost:3306/exam_db?useSSL=false&serverTimezone=UTC&characterEncoding=utf8&allowPublicKeyRetrieval=true");
            String username = getProperty(props, "db.username", "root");
            String password = getProperty(props, "db.password", "123456");
            String driver = getProperty(props, "db.driver", "com.mysql.cj.jdbc.Driver");
            
            config.setJdbcUrl(url);
            config.setUsername(username);
            config.setPassword(password);
            config.setDriverClassName(driver);
            
            // ========== 连接池基本配置 ==========
            config.setMinimumIdle(getIntProperty(props, "hikari.minimumIdle", 5));
            config.setMaximumPoolSize(getIntProperty(props, "hikari.maximumPoolSize", 20));
            config.setConnectionTimeout(getLongProperty(props, "hikari.connectionTimeout", 30000L));
            config.setIdleTimeout(getLongProperty(props, "hikari.idleTimeout", 600000L));
            config.setMaxLifetime(getLongProperty(props, "hikari.maxLifetime", 1800000L));
            config.setLeakDetectionThreshold(getLongProperty(props, "hikari.leakDetectionThreshold", 60000L));
            
            // ========== 连接测试配置 ==========
            String testQuery = getProperty(props, "hikari.connectionTestQuery", "SELECT 1");
            if (testQuery != null && !testQuery.isEmpty()) {
                config.setConnectionTestQuery(testQuery);
            }
            
            String initSql = getProperty(props, "hikari.connectionInitSql", "SET NAMES utf8mb4");
            if (initSql != null && !initSql.isEmpty()) {
                config.setConnectionInitSql(initSql);
            }
            
            // ========== 连接池名称 ==========
            String poolName = getProperty(props, "hikari.poolName", "ExamSystemPool");
            config.setPoolName(poolName);
            
            // ========== MySQL 性能优化配置 ==========
            config.addDataSourceProperty("cachePrepStmts", 
                getBooleanProperty(props, "hikari.cachePrepStmts", true));
            config.addDataSourceProperty("prepStmtCacheSize", 
                getIntProperty(props, "hikari.prepStmtCacheSize", 250));
            config.addDataSourceProperty("prepStmtCacheSqlLimit", 
                getIntProperty(props, "hikari.prepStmtCacheSqlLimit", 2048));
            config.addDataSourceProperty("useServerPrepStmts", 
                getBooleanProperty(props, "hikari.useServerPrepStmts", true));
            config.addDataSourceProperty("useLocalSessionState", 
                getBooleanProperty(props, "hikari.useLocalSessionState", true));
            config.addDataSourceProperty("rewriteBatchedStatements", 
                getBooleanProperty(props, "hikari.rewriteBatchedStatements", true));
            config.addDataSourceProperty("cacheResultSetMetadata", 
                getBooleanProperty(props, "hikari.cacheResultSetMetadata", true));
            config.addDataSourceProperty("cacheServerConfiguration", 
                getBooleanProperty(props, "hikari.cacheServerConfiguration", true));
            config.addDataSourceProperty("elideSetAutoCommits", 
                getBooleanProperty(props, "hikari.elideSetAutoCommits", true));
            config.addDataSourceProperty("maintainTimeStats", 
                getBooleanProperty(props, "hikari.maintainTimeStats", false));
            
            // 创建数据源
            dataSource = new HikariDataSource(config);
            
            logger.info("HikariCP连接池初始化成功 - 最大连接数: {}, 最小空闲连接数: {}, 连接池名称: {}", 
                       config.getMaximumPoolSize(), config.getMinimumIdle(), poolName);
        } catch (Exception e) {
            logger.error("HikariCP连接池初始化失败", e);
            throw new RuntimeException("数据库连接池初始化失败", e);
        }
    }
    
    /**
     * 从classpath加载db.properties配置文件
     * 如果配置文件不存在，返回空Properties对象（使用默认配置）
     */
    private static Properties loadProperties() {
        Properties props = new Properties();
        try {
            InputStream is = DBUtil.class.getClassLoader().getResourceAsStream(CONFIG_FILE);
            if (is != null) {
                props.load(is);
                is.close();
                logger.info("成功加载{}配置文件", CONFIG_FILE);
            } else {
                logger.warn("未找到{}配置文件，使用默认配置。建议复制{}到src/main/resources目录并修改配置", 
                           CONFIG_FILE, CONFIG_FILE + ".example");
            }
        } catch (Exception e) {
            logger.warn("加载{}配置文件失败，使用默认配置", CONFIG_FILE, e);
        }
        return props;
    }
    
    /**
     * 从Properties中获取字符串属性值，如果不存在则返回默认值
     */
    private static String getProperty(Properties props, String key, String defaultValue) {
        String value = props.getProperty(key);
        return (value != null && !value.trim().isEmpty()) ? value.trim() : defaultValue;
    }
    
    /**
     * 从Properties中获取整数属性值，如果不存在或格式错误则返回默认值
     */
    private static int getIntProperty(Properties props, String key, int defaultValue) {
        String value = props.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Integer.parseInt(value.trim());
            } catch (NumberFormatException e) {
                logger.warn("配置项{}的值'{}'不是有效的整数，使用默认值{}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }
    
    /**
     * 从Properties中获取长整型属性值，如果不存在或格式错误则返回默认值
     */
    private static long getLongProperty(Properties props, String key, long defaultValue) {
        String value = props.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            try {
                return Long.parseLong(value.trim());
            } catch (NumberFormatException e) {
                logger.warn("配置项{}的值'{}'不是有效的长整数，使用默认值{}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }
    
    /**
     * 从Properties中获取布尔属性值，如果不存在则返回默认值
     * 支持 true/false, yes/no, 1/0 等格式
     */
    private static boolean getBooleanProperty(Properties props, String key, boolean defaultValue) {
        String value = props.getProperty(key);
        if (value != null && !value.trim().isEmpty()) {
            String lowerValue = value.trim().toLowerCase();
            if (lowerValue.equals("true") || lowerValue.equals("yes") || lowerValue.equals("1")) {
                return true;
            } else if (lowerValue.equals("false") || lowerValue.equals("no") || lowerValue.equals("0")) {
                return false;
            } else {
                logger.warn("配置项{}的值'{}'不是有效的布尔值，使用默认值{}", key, value, defaultValue);
            }
        }
        return defaultValue;
    }
    
    /**
     * 从连接池获取数据库连接
     * 
     * @return 数据库连接对象
     * @throws SQLException 如果获取连接失败
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("数据源未初始化");
        }
        try {
            Connection conn = dataSource.getConnection();
            logger.debug("从连接池获取连接成功");
            return conn;
        } catch (SQLException e) {
            logger.error("从连接池获取连接失败", e);
            throw e;
        }
    }
    
    /**
     * 将连接归还到连接池（不真正关闭连接）
     * 
     * @param conn 要归还的数据库连接
     */
    public static void closeConnection(Connection conn) {
        if (conn != null) {
            try {
                // HikariCP会自动将连接归还到连接池
                conn.close();
                logger.debug("连接已归还到连接池");
            } catch (SQLException e) {
                logger.error("归还连接到连接池时发生错误", e);
            }
        }
    }
    
    /**
     * 关闭连接池（通常在应用关闭时调用）
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("HikariCP连接池已关闭");
        }
    }
    
    /**
     * 获取连接池状态信息（用于监控）
     */
    public static String getPoolStatus() {
        if (dataSource == null || dataSource.isClosed()) {
            return "连接池未初始化或已关闭";
        }
        return String.format("连接池状态 - 活跃连接: %d, 空闲连接: %d, 等待线程: %d, 总连接: %d",
            dataSource.getHikariPoolMXBean().getActiveConnections(),
            dataSource.getHikariPoolMXBean().getIdleConnections(),
            dataSource.getHikariPoolMXBean().getThreadsAwaitingConnection(),
            dataSource.getHikariPoolMXBean().getTotalConnections());
    }
}

