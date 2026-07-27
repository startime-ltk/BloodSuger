package com.bloodsugar.dao;

import com.bloodsugar.config.DatabaseConfig;
import com.bloodsugar.model.BloodSugarRecord;

import java.sql.*;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 血糖记录数据访问层
 */
public class BloodSugarDAO {

    /**
     * 插入一条血糖记录
     */
    public void insert(BloodSugarRecord record) throws SQLException {
        String sql = "INSERT INTO blood_sugar_records (record_time, blood_sugar, meal_time, meal_period, meal_type, note) VALUES (?, ?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, record.getRecordTime());
            ps.setDouble(2, record.getBloodSugar());
            ps.setObject(3, record.getMealTime());
            ps.setString(4, record.getMealPeriod());
            ps.setString(5, record.getMealType());
            ps.setString(6, record.getNote());
            ps.executeUpdate();
        }
    }

    /**
     * 查询所有记录，按测量时间倒序
     */
    public List<BloodSugarRecord> findAll() throws SQLException {
        String sql = "SELECT * FROM blood_sugar_records ORDER BY record_time DESC";
        List<BloodSugarRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                list.add(mapRow(rs));
            }
        }
        return list;
    }

    /**
     * 按日期范围查询
     */
    public List<BloodSugarRecord> findByDateRange(LocalDateTime from, LocalDateTime to) throws SQLException {
        String sql = "SELECT * FROM blood_sugar_records WHERE record_time BETWEEN ? AND ? ORDER BY record_time ASC";
        List<BloodSugarRecord> list = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, from);
            ps.setObject(2, to);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    list.add(mapRow(rs));
                }
            }
        }
        return list;
    }

    /**
     * 删除一条记录
     */
    public void delete(int id) throws SQLException {
        String sql = "DELETE FROM blood_sugar_records WHERE id = ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, id);
            ps.executeUpdate();
        }
    }

    /**
     * 更新一条记录
     */
    public void update(BloodSugarRecord record) throws SQLException {
        String sql = "UPDATE blood_sugar_records SET record_time=?, blood_sugar=?, meal_time=?, meal_period=?, meal_type=?, note=? WHERE id=?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, record.getRecordTime());
            ps.setDouble(2, record.getBloodSugar());
            ps.setObject(3, record.getMealTime());
            ps.setString(4, record.getMealPeriod());
            ps.setString(5, record.getMealType());
            ps.setString(6, record.getNote());
            ps.setInt(7, record.getId());
            ps.executeUpdate();
        }
    }

    /**
     * 查询指定时间之前（含）最近一次用餐时间
     * @param before 参照时间（一般为血糖测量时间）
     * @return 最近的用餐时间，无记录返回 null
     */
    public LocalDateTime findLatestMealTimeBefore(LocalDateTime before) throws SQLException {
        String sql = "SELECT MAX(meal_time) FROM blood_sugar_records WHERE meal_time IS NOT NULL AND meal_time <= ?";
        try (Connection conn = DatabaseConfig.getConnection();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setObject(1, before);
            try (ResultSet rs = ps.executeQuery()) {
                if (rs.next()) {
                    Timestamp ts = rs.getTimestamp(1);
                    return ts != null ? ts.toLocalDateTime() : null;
                }
            }
        }
        return null;
    }

    /**
     * 获取所有有记录的日期（用于日历筛选）
     */
    public List<String> findDistinctDates() throws SQLException {
        String sql = "SELECT DISTINCT DATE(record_time) AS d FROM blood_sugar_records ORDER BY d DESC";
        List<String> dates = new ArrayList<>();
        try (Connection conn = DatabaseConfig.getConnection();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {
            while (rs.next()) {
                dates.add(rs.getString("d"));
            }
        }
        return dates;
    }

    private BloodSugarRecord mapRow(ResultSet rs) throws SQLException {
        BloodSugarRecord r = new BloodSugarRecord();
        r.setId(rs.getInt("id"));
        Timestamp rt = rs.getTimestamp("record_time");
        if (rt != null) r.setRecordTime(rt.toLocalDateTime());
        r.setBloodSugar(rs.getDouble("blood_sugar"));
        Timestamp mt = rs.getTimestamp("meal_time");
        if (mt != null) r.setMealTime(mt.toLocalDateTime());
        r.setMealPeriod(rs.getString("meal_period"));
        r.setMealType(rs.getString("meal_type"));
        r.setNote(rs.getString("note"));
        return r;
    }
}
