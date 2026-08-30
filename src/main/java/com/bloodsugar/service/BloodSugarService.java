package com.bloodsugar.service;

import com.bloodsugar.dao.BloodSugarDAO;
import com.bloodsugar.model.BloodSugarRecord;
import com.bloodsugar.util.PeriodClassifier;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/** 业务逻辑层，UI 只跟它打交道 */
public class BloodSugarService {

    private final BloodSugarDAO dao = new BloodSugarDAO();

    // 新增记录，时段自动归类
    public void addRecord(LocalDateTime recordTime, double bloodSugar,
            LocalDateTime mealTime, String mealType, String note) throws SQLException {
        String mealPeriod = PeriodClassifier.classify(recordTime, mealTime);
        BloodSugarRecord record = new BloodSugarRecord(recordTime, bloodSugar, mealTime, mealPeriod, mealType, note);
        dao.insert(record);
    }

    // 查全部
    public List<BloodSugarRecord> getAllRecords() throws SQLException {
        return dao.findAll();
    }

    // 按日期范围查
    public List<BloodSugarRecord> getRecordsByDateRange(LocalDateTime from, LocalDateTime to) throws SQLException {
        return dao.findByDateRange(from, to);
    }

    // 删除一条
    public void deleteRecord(int id) throws SQLException {
        dao.delete(id);
    }

    // 更新一条
    public void updateRecord(BloodSugarRecord record) throws SQLException {
        // 重新计算时段
        String mealPeriod = PeriodClassifier.classify(record.getRecordTime(), record.getMealTime());
        record.setMealPeriod(mealPeriod);
        dao.update(record);
    }

    /**
     * 找 before 之前最近的一次用餐时间，用来自动识别餐别。
     * 只查当天（凌晨4点起）内的用餐，当天没吃过就返回 null，算空腹。
     */
    public LocalDateTime getLatestMealTimeBefore(LocalDateTime before) throws SQLException {
        if (before == null) return null;
        java.time.LocalDate businessDate = PeriodClassifier.getBusinessDate(before);
        LocalDateTime dayStart = PeriodClassifier.getBusinessDayStart(businessDate);
        return dao.findLatestMealTimeBefore(dayStart, before);
    }

    /**
     * 保存/覆盖用餐时间：同一业务日同一餐别只保留最新一条，
     * 再次保存时覆盖旧值。业务日按凌晨4点边界计算。
     */
    public void saveMealTime(String mealName, LocalDateTime mealTime) throws SQLException {
        if (mealName == null || mealTime == null) return;
        LocalDate businessDate = PeriodClassifier.getBusinessDate(mealTime);
        dao.upsertMealTime(businessDate, mealName, mealTime);
    }

    /**
     * 查 before 之前最近一条已保存的用餐时间（来自 meal_times 表），
     * 用于餐别自动识别时兜底（面板未填但已保存过的情况）。
     */
    public LocalDateTime getLatestSavedMealTime(LocalDateTime before) throws SQLException {
        if (before == null) return null;
        LocalDate businessDate = PeriodClassifier.getBusinessDate(before);
        return dao.findLatestSavedMealTime(businessDate, before);
    }

    // 有哪些日期有记录
    public List<String> getRecordDates() throws SQLException {
        return dao.findDistinctDates();
    }
}
