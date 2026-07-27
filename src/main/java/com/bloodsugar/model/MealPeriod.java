package com.bloodsugar.model;

/**
 * 时间段定义（用于自动归类）
 */
public class MealPeriod {

    private String name;       // 时间段名称，如"空腹"、"餐后2h"
    private int minMinutes;    // 距离用餐的最小分钟数（含）
    private int maxMinutes;    // 距离用餐的最大分钟数（不含），Integer.MAX_VALUE 表示无上限
    private double normalLow;  // 该时段健康血糖下限 mmol/L
    private double normalHigh; // 该时段健康血糖上限 mmol/L

    public MealPeriod(String name, int minMinutes, int maxMinutes, double normalLow, double normalHigh) {
        this.name = name;
        this.minMinutes = minMinutes;
        this.maxMinutes = maxMinutes;
        this.normalLow = normalLow;
        this.normalHigh = normalHigh;
    }

    public String getName() { return name; }
    public int getMinMinutes() { return minMinutes; }
    public int getMaxMinutes() { return maxMinutes; }
    public double getNormalLow() { return normalLow; }
    public double getNormalHigh() { return normalHigh; }

    /**
     * 判断给定分钟数是否属于该时段
     */
    public boolean matches(long minutesAfterMeal) {
        return minutesAfterMeal >= minMinutes && minutesAfterMeal < maxMinutes;
    }
}
