# 糖伴SugarPal (Sugar Pal Blood Sugar Tracker)

基于 JavaFX 的桌面血糖管理工具，支持记录血糖、趋势图表、自动餐别识别、糖尿病风险评估。

## 技术栈

| 组件 | 版本 |
|------|------|
| JDK | 21 (Eclipse Adoptium) |
| JavaFX | 21 |
| H2 Database | 2.2.224 (嵌入式，零安装) |
| HikariCP | 5.1.0 |
| Maven | 3.9+ |
| 构建 | Maven Shade + Assembly |

## 功能

- **血糖记录**：添加/修改/删除血糖测量数据（日期、时间、血糖值、备注）
- **餐别自动识别**：根据「今日用餐时间」面板推算精确餐别描述（如"早餐后 2小时35分钟""空腹""睡前"）
- **趋势曲线**：血糖折线图，正常值绿色、偏高值红色，含正常区间上下限
- **时间段分类**：自动区分空腹/餐后1h/2h/3h，用于区间判定
- **日期筛选**：按日期过滤历史记录
- **健康建议**：每次记录后自动给出饮食/运动建议
- **总结报告**：统计平均值/最高/最低/正常率，糖尿病风险与胰岛素抵抗评估
- **本地数据库**：H2 嵌入式数据库，数据存储在 `%USERPROFILE%\.bloodsugar\data`

## 环境要求

- JDK 21
- Maven 3.9+
- Windows / macOS / Linux

## 快速开始

```bash
# 克隆项目
git clone <your-repo-url>
cd sugarpal

# 编译
mvn compile

# 运行
mvn javafx:run

# 打包为可执行 JAR
mvn package
# fat JAR 位于 target/SugarPal.jar
```

## 项目结构

```
sugarpal/
├── pom.xml
├── sql/
│   └── init.sql                        # 数据库建表参考
├── src/
│   ├── main/
│   │   ├── assembly/
│   │   │   └── assembly.xml            # 分发包配置
│   │   ├── java/com/bloodsugar/
│   │   │   ├── BloodSugarApp.java      # 应用入口
│   │   │   ├── config/
│   │   │   │   └── DatabaseConfig.java # H2 连接池配置
│   │   │   ├── dao/
│   │   │   │   └── BloodSugarDAO.java  # 数据访问层
│   │   │   ├── model/
│   │   │   │   ├── BloodSugarRecord.java
│   │   │   │   └── MealPeriod.java
│   │   │   ├── service/
│   │   │   │   └── BloodSugarService.java
│   │   │   ├── ui/
│   │   │   │   └── MainUI.java         # 主界面（~980行）
│   │   │   └── util/
│   │   │       └── PeriodClassifier.java  # 时段分类与正常区间
│   │   └── resources/
│   │       ├── icon.png
│   │       └── icon.ico
│   └── test/
└── .vscode/                            # VS Code 运行配置
```

## 数据库

使用 H2 嵌入式数据库，首次运行自动创建。数据文件路径：

```
%USERPROFILE%\.bloodsugar\data
```

表结构（`blood_sugar_records`）：

| 字段 | 类型 | 说明 |
|------|------|------|
| id | INT AUTO_INCREMENT | 主键 |
| record_time | DATETIME | 测量时间 |
| blood_sugar | DECIMAL(5,2) | 血糖值 (mmol/L) |
| meal_time | DATETIME | 用餐时间 |
| meal_period | VARCHAR(20) | 时段（空腹/餐后1h/2h/3h） |
| meal_type | VARCHAR(40) | 餐别描述（自动识别） |
| note | VARCHAR(200) | 备注 |
| created_at | TIMESTAMP | 创建时间 |

## 正常血糖范围

| 时段 | 正常下限 | 正常上限 |
|------|---------|---------|
| 空腹 | 3.9 | 6.1 |
| 餐后1h | 3.9 | 11.1 |
| 餐后2h | 3.9 | 7.8 |
| 餐后3h | 3.9 | 7.8 |
| 睡前 | 3.9 | 7.8 |

## 使用说明

1. 启动后，在左侧「今日用餐时间」面板输入当天的早餐/午餐/晚餐/加餐时间，点击保存
2. 点击「+ 添加记录」，输入测量日期、时间和血糖值
3. 餐别会根据用餐时间自动计算（如早餐 7:00，9:35 测量 → "早餐后 2小时35分钟"）
4. 右侧趋势图直观展示血糖变化，绿色点在正常范围内，红色点偏高
5. 点击「生成总结」查看统计报告与风险评估

## 许可

MIT
