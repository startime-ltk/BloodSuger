package com.bloodsugar.model;

import java.time.LocalDateTime;

/** 一条血糖记录 */
public class BloodSugarRecord {

    private int id;
    private LocalDateTime recordTime; // 测量时间
    private double bloodSugar; // 血糖值 mmol/L
    private LocalDateTime mealTime; // 用餐时间，可能没有
    private String mealPeriod; // 空腹/餐后1h/餐后2h/餐后3h
    private String mealType; // 早餐/午餐/晚餐
    private String note; // 备注

    public BloodSugarRecord() {}

    public BloodSugarRecord(LocalDateTime recordTime, double bloodSugar,
            LocalDateTime mealTime, String mealPeriod, String mealType, String note) {
        this.recordTime = recordTime;
        this.bloodSugar = bloodSugar;
        this.mealTime = mealTime;
        this.mealPeriod = mealPeriod;
        this.mealType = mealType;
        this.note = note;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public LocalDateTime getRecordTime() { return recordTime; }
    public void setRecordTime(LocalDateTime recordTime) { this.recordTime = recordTime; }

    public double getBloodSugar() { return bloodSugar; }
    public void setBloodSugar(double bloodSugar) { this.bloodSugar = bloodSugar; }

    public LocalDateTime getMealTime() { return mealTime; }
    public void setMealTime(LocalDateTime mealTime) { this.mealTime = mealTime; }

    public String getMealPeriod() { return mealPeriod; }
    public void setMealPeriod(String mealPeriod) { this.mealPeriod = mealPeriod; }

    public String getMealType() { return mealType; }
    public void setMealType(String mealType) { this.mealType = mealType; }

    public String getNote() { return note; }
    public void setNote(String note) { this.note = note; }

    @Override
    public String toString() {
        return String.format("%s | %.1f mmol/L | %s | %s",
                recordTime != null ? recordTime.toLocalDate() : "",
                bloodSugar, mealPeriod, mealType);
    }
}
