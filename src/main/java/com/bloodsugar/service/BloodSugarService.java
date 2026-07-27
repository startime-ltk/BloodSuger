package com.bloodsugar.service;

import com.bloodsugar.dao.BloodSugarDAO;
import com.bloodsugar.model.BloodSugarRecord;
import com.bloodsugar.util.PeriodClassifier;

import java.sql.SQLException;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 业务逻辑层
 */
public class BloodSugarService {

    private final BloodSugarDAO dao = new BloodSugarDAO();

    /**
     * 添加血糖记录，自动归类时间段
     */
    public void addRecord(LocalDateTime recordTime, double bloodSugar,
            LocalDateTime mealTime, String mealType, String note) throws SQLException {
        String mealPeriod = PeriodClassifier.classify(recordTime, mealTime);
        BloodSugarRecord record = new BloodSugarRecord(recordTime, bloodSugar, mealTime, mealPeriod, mealType, note);
        dao.insert(record);
    }

    /**
     * 获取所有记录
     */
    public List<BloodSugarRecord> getAllRecords() throws SQLException {
        return dao.findAll();
    }

    /**
     * 按日期范围获取记录
     */
    public List<BloodSugarRecord> getRecordsByDateRange(LocalDateTime from, LocalDateTime to) throws SQLException {
        return dao.findByDateRange(from, to);
    }

    /**
     * 删除记录
     */
    public void deleteRecord(int id) throws SQLException {
        dao.delete(id);
    }

    /**
     * 更新记录
     */
    public void updateRecord(BloodSugarRecord record) throws SQLException {
        // 重新计算时段
        String mealPeriod = PeriodClassifier.classify(record.getRecordTime(), record.getMealTime());
        record.setMealPeriod(mealPeriod);
        dao.update(record);
    }

    /**
     * 查询指定时间之前最近一次用餐时间（用于餐别自动识别）
     */
    public LocalDateTime getLatestMealTimeBefore(LocalDateTime before) throws SQLException {
        return dao.findLatestMealTimeBefore(before);
    }

    /**
     * 获取所有有记录的日期
     */
    public List<String> getRecordDates() throws SQLException {
        return dao.findDistinctDates();
    }
}
