package com.bloodsugar.util;

import com.bloodsugar.model.MealPeriod;

import java.time.Duration;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间段自动归类工具
 * 根据测量时间与用餐时间差，自动匹配时间段及对应的健康血糖区间
 */
public class PeriodClassifier {

    /** 一天的边界时间：凌晨 4:00（4点之后的数据归属第二天） */
    public static final LocalTime DAY_BOUNDARY = LocalTime.of(4, 0);

    private static final List<MealPeriod> PERIODS = new ArrayList<>();

    static {
        // 没有用餐时间或距离用餐超过3小时 → 空腹
        PERIODS.add(new MealPeriod("空腹", 180, Integer.MAX_VALUE, 3.9, 6.1));
        // 餐后 0 - 1 小时
        PERIODS.add(new MealPeriod("餐后1h", 0, 60, 3.9, 8.9));
        // 餐后 1 - 2 小时
        PERIODS.add(new MealPeriod("餐后2h", 60, 120, 3.9, 7.8));
        // 餐后 2 - 3 小时
        PERIODS.add(new MealPeriod("餐后3h", 120, 180, 3.9, 7.8));
    }

    /**
     * 计算"业务日期"：以凌晨 4:00 为一天边界。
     * 凌晨 0:00 - 3:59 的记录归属前一天。
     */
    public static LocalDate getBusinessDate(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        LocalDate date = dateTime.toLocalDate();
        if (dateTime.toLocalTime().isBefore(DAY_BOUNDARY)) {
            return date.minusDays(1);
        }
        return date;
    }

    /**
     * 业务日期起始时间（当天凌晨 4:00）
     */
    public static LocalDateTime getBusinessDayStart(LocalDate date) {
        return LocalDateTime.of(date, DAY_BOUNDARY);
    }

    /**
     * 业务日期结束时间（次日凌晨 4:00，不含）
     */
    public static LocalDateTime getBusinessDayEnd(LocalDate date) {
        return LocalDateTime.of(date.plusDays(1), DAY_BOUNDARY);
    }

    /**
     * 根据测量时间和用餐时间自动归类
     * @return 匹配到的时间段名称
     */
    public static String classify(LocalDateTime recordTime, LocalDateTime mealTime) {
        if (mealTime == null || recordTime == null) return "空腹";

        long minutes = Duration.between(mealTime, recordTime).toMinutes();
        // 测量时间早于用餐时间 → 空腹
        if (minutes < 0) return "空腹";

        for (MealPeriod period : PERIODS) {
            if (period.matches(minutes)) {
                return period.getName();
            }
        }
        return "空腹";
    }

    /**
     * 根据血糖测量时间与最近用餐时间自动识别餐别（空腹/餐前/餐后/睡前）
     * 规则（基于凌晨 4:00 的新一天边界）：
     *   1. 新的一天（凌晨4点起）内无用餐数据 → 空腹
     *   2. 晚上 22:00 以后 → 睡前
     *   3. 餐后 2 小时内 → 餐后
     *   4. 餐后 2-5 小时 → 餐前（距下一餐）
     *   5. 距上次用餐超过 5 小时 → 空腹
     * @param recordTime 血糖测量时间
     * @param mealTime   最近一次用餐时间（可为 null）
     * @return 自动识别的餐别
     */
    public static String classifyMealType(LocalDateTime recordTime, LocalDateTime mealTime) {
        if (recordTime == null) return "空腹";
        // 无用餐记录，或用餐不在同一天（凌晨4点边界）内 → 空腹
        if (mealTime == null || !getBusinessDate(mealTime).equals(getBusinessDate(recordTime))) {
            return "空腹";
        }
        // 晚上 22:00 以后 → 睡前
        if (recordTime.toLocalTime().getHour() >= 22) return "睡前";
        long minutes = Duration.between(mealTime, recordTime).toMinutes();
        // 测量时间早于用餐时间 → 空腹
        if (minutes < 0) return "空腹";
        // 餐后 2 小时内 → 餐后
        if (minutes <= 120) return "餐后";
        // 餐后 2-5 小时 → 餐前（距下一餐）
        if (minutes <= 300) return "餐前";
        // 距上次用餐超过 5 小时（含超过 6 小时）→ 空腹
        return "空腹";
    }

    /**
     * 根据时间段名称获取对应的健康血糖区间
     * @return [下限, 上限]，未匹配到返回空腹区间
     */
    public static double[] getNormalRange(String periodName) {
        for (MealPeriod period : PERIODS) {
            if (period.getName().equals(periodName)) {
                return new double[]{period.getNormalLow(), period.getNormalHigh()};
            }
        }
        return new double[]{3.9, 6.1}; // 默认空腹
    }

    /**
     * 判断血糖值是否在正常区间内
     */
    public static boolean isNormal(String periodName, double bloodSugar) {
        double[] range = getNormalRange(periodName);
        return bloodSugar >= range[0] && bloodSugar <= range[1];
    }

    /**
     * 获取所有预定义的时间段列表
     */
    public static List<MealPeriod> getAllPeriods() {
        return PERIODS;
    }
}
