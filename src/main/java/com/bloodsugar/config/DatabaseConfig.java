package com.bloodsugar.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;

import java.nio.file.Path;
import java.sql.Connection;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * H2 嵌入式数据库连接配置（HikariCP 连接池）。
 * 数据文件放在用户主目录的 .bloodsugar/data 下，免安装。
 */
public class DatabaseConfig {

    private static final Path DB_DIR = Path.of(System.getProperty("user.home"), ".bloodsugar");
    private static final String DB_URL = "jdbc:h2:file:" + DB_DIR.resolve("data").toString().replace('\\', '/')
            + ";AUTO_SERVER=TRUE;MODE=MySQL";

    private static final HikariDataSource dataSource;

    static {
        try {
            DB_DIR.toFile().mkdirs();
        } catch (Exception e) {
            throw new RuntimeException("无法创建数据目录: " + DB_DIR, e);
        }

        HikariConfig config = new HikariConfig();
        config.setJdbcUrl(DB_URL);
        config.setMaximumPoolSize(5);
        config.setMinimumIdle(2);
        config.setConnectionTimeout(5000);
        config.setIdleTimeout(300000);
        dataSource = new HikariDataSource(config);

        // 启动时顺手把表建好
        try (Connection conn = dataSource.getConnection();
             Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE IF NOT EXISTS blood_sugar_records ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "record_time TIMESTAMP, "
                    + "blood_sugar DOUBLE, "
                    + "meal_time TIMESTAMP, "
                    + "meal_period VARCHAR(20), "
                    + "meal_type VARCHAR(20), "
                    + "note VARCHAR(200), "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP"
                    + ")");
        } catch (SQLException e) {
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
