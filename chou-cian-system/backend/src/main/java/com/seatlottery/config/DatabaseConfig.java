package com.seatlottery.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.io.InputStream;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.Properties;

public class DatabaseConfig {
    private static final Logger logger = LoggerFactory.getLogger(DatabaseConfig.class);
    private static HikariDataSource dataSource;

    private static final String CONFIG_FILE = "/database.properties";

    static {
        try {
            // 加載配置文件
            Properties props = loadProperties();

            // 創建 HikariCP 配置
            HikariConfig config = new HikariConfig();

            // 設置數據庫連接參數
            config.setDriverClassName(props.getProperty("db.driver"));
            config.setJdbcUrl(props.getProperty("db.url"));
            config.setUsername(props.getProperty("db.username"));
            config.setPassword(props.getProperty("db.password"));

            // 設置連接池配置
            config.setMaximumPoolSize(Integer.parseInt(props.getProperty("db.pool.maxSize", "10")));
            config.setMinimumIdle(Integer.parseInt(props.getProperty("db.pool.minIdle", "5")));
            config.setIdleTimeout(Long.parseLong(props.getProperty("db.pool.idleTimeout", "30000")));
            config.setConnectionTimeout(Long.parseLong(props.getProperty("db.pool.connectionTimeout", "30000")));

            // 設置 PreparedStatement 緩存
            config.addDataSourceProperty("cachePrepStmts", props.getProperty("db.pool.cachePrepStmts", "true"));
            config.addDataSourceProperty("prepStmtCacheSize", props.getProperty("db.pool.prepStmtCacheSize", "250"));
            config.addDataSourceProperty("prepStmtCacheSqlLimit", props.getProperty("db.pool.prepStmtCacheSqlLimit", "2048"));

            // 創建數據源
            dataSource = new HikariDataSource(config);

            logger.info("成功初始化數據庫連接池");
        } catch (IOException | RuntimeException e) {
            logger.error("初始化資料庫連接池失敗", e);
            throw new RuntimeException("初始化資料庫連接池失敗", e);
        }
    }

    /**
     * 從配置文件加載屬性
     *
     * @return Properties 包含數據庫配置參數
     * @throws IOException 如果無法讀取配置文件
     */
    private static Properties loadProperties() throws IOException {
        Properties props = new Properties();

        try (InputStream input = DatabaseConfig.class.getResourceAsStream(CONFIG_FILE)) {
            if (input == null) {
                throw new IOException("無法找到配置文件: " + CONFIG_FILE);
            }
            props.load(input);
        }

        return props;
    }

    /**
     * 獲取數據庫連接
     *
     * @return Connection 數據庫連接
     * @throws SQLException 如果獲取連接失敗
     */
    public static Connection getConnection() throws SQLException {
        if (dataSource == null) {
            throw new SQLException("數據源尚未初始化");
        }
        return dataSource.getConnection();
    }

    /**
     * 關閉數據庫連接
     *
     * @param connection 要關閉的連接
     */
    public static void closeConnection(Connection connection) {
        if (connection != null) {
            try {
                connection.close();
            } catch (SQLException e) {
                logger.error("關閉數據庫連接失敗", e);
            }
        }
    }

    /**
     * 關閉數據源（在應用關閉時調用）
     */
    public static void shutdown() {
        if (dataSource != null && !dataSource.isClosed()) {
            dataSource.close();
            logger.info("數據庫連接池已關閉");
        }
    }
}