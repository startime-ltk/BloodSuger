package com.bloodsugar.util;

import com.bloodsugar.model.MealPeriod;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * 时间段自动归类工具
 * 根据测量时间与用餐时间差，自动匹配时间段及对应的健康血糖区间
 */
public class PeriodClassifier {

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
     * 规则：
     *   1. 晚上 22:00 以后 → 睡前
     *   2. 餐后 2 小时内 → 餐后
     *   3. 餐后 2-5 小时 → 餐前（距下一餐）
     *   4. 无用餐记录或距上次用餐超过 5 小时 → 空腹
     * @param recordTime 血糖测量时间
     * @param mealTime   最近一次用餐时间（可为 null）
     * @return 自动识别的餐别
     */
    public static String classifyMealType(LocalDateTime recordTime, LocalDateTime mealTime) {
        if (recordTime == null) return "空腹";
        // 晚上 22:00 以后 → 睡前
        if (recordTime.toLocalTime().getHour() >= 22) return "睡前";
        // 无用餐记录 → 空腹
        if (mealTime == null) return "空腹";
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
