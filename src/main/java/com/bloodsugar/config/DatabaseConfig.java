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
            // 用餐时间表：同一业务日（凌晨4点边界）同一餐别只保留一条，重复保存覆盖旧值
            stmt.execute("CREATE TABLE IF NOT EXISTS meal_times ("
                    + "id INT AUTO_INCREMENT PRIMARY KEY, "
                    + "business_date DATE NOT NULL, "
                    + "meal_name VARCHAR(20) NOT NULL, "
                    + "meal_time TIMESTAMP NOT NULL, "
                    + "created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP, "
                    + "CONSTRAINT uk_meal_business UNIQUE (business_date, meal_name)"
                    + ")");
        } catch (SQLException e) {
            throw new RuntimeException("数据库初始化失败", e);
        }
    }

    public static Connection getConnection() throws SQLException {
        return dataSource.getConnection();
    }
}
