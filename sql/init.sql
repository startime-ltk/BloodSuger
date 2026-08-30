-- 血糖记录系统 数据库初始化脚本
CREATE DATABASE IF NOT EXISTS blood_sugar DEFAULT CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE blood_sugar;

CREATE TABLE IF NOT EXISTS blood_sugar_records (
    id INT AUTO_INCREMENT PRIMARY KEY,
    record_time DATETIME NOT NULL COMMENT '测量时间',
    blood_sugar DECIMAL(5,2) NOT NULL COMMENT '血糖值(mmol/L)',
    meal_time DATETIME DEFAULT NULL COMMENT '用餐时间',
    meal_period VARCHAR(20) DEFAULT NULL COMMENT '时间段(空腹/餐后1h/餐后2h/餐后3h)',
    meal_type VARCHAR(10) DEFAULT NULL COMMENT '餐别(早餐/午餐/晚餐)',
    note VARCHAR(200) DEFAULT '' COMMENT '备注',
    insulin DOUBLE DEFAULT 0 COMMENT '胰岛素剂量(U)',
    carbs DOUBLE DEFAULT 0 COMMENT '碳水化合物(g)',
    activity DOUBLE DEFAULT 0 COMMENT '运动时长(分钟)',
    weight DOUBLE DEFAULT 0 COMMENT '体重(kg)',
    pulse DOUBLE DEFAULT 0 COMMENT '脉搏(次/分)',
    blood_pressure VARCHAR(20) DEFAULT NULL COMMENT '血压(如120/80)',
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP COMMENT '记录创建时间'
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COMMENT='血糖记录表';

-- 兼容旧库：老版本没有健康维度字段，用 ALTER TABLE ADD COLUMN 补齐（不丢已有数据）
ALTER TABLE blood_sugar_records ADD COLUMN IF NOT EXISTS insulin DOUBLE DEFAULT 0 COMMENT '胰岛素剂量(U)';
ALTER TABLE blood_sugar_records ADD COLUMN IF NOT EXISTS carbs DOUBLE DEFAULT 0 COMMENT '碳水化合物(g)';
ALTER TABLE blood_sugar_records ADD COLUMN IF NOT EXISTS activity DOUBLE DEFAULT 0 COMMENT '运动时长(分钟)';
ALTER TABLE blood_sugar_records ADD COLUMN IF NOT EXISTS weight DOUBLE DEFAULT 0 COMMENT '体重(kg)';
ALTER TABLE blood_sugar_records ADD COLUMN IF NOT EXISTS pulse DOUBLE DEFAULT 0 COMMENT '脉搏(次/分)';
ALTER TABLE blood_sugar_records ADD COLUMN IF NOT EXISTS blood_pressure VARCHAR(20) DEFAULT NULL COMMENT '血压(如120/80)';
