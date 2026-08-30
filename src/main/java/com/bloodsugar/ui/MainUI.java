package com.bloodsugar.ui;

import com.bloodsugar.model.BloodSugarRecord;
import com.bloodsugar.service.BloodSugarService;
import com.bloodsugar.util.PeriodClassifier;

import javafx.application.Platform;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTableCell;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.paint.CycleMethod;
import javafx.scene.paint.LinearGradient;
import javafx.scene.paint.Stop;
import javafx.scene.shape.Circle;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.sql.SQLException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class MainUI {

    private final BloodSugarService service = new BloodSugarService();

    private TableView<BloodSugarRecord> table;
    private LineChart<String, Number> chart;
    private NumberAxis yAxis;
    private ComboBox<String> dateFilterCombo;
    private Label statusLabel;

    private TextField breakfastField, lunchField, dinnerField, extraMealField;

    // 记录 X 轴标签 → 记录 ID 的映射，用于图表右键删除
    private Map<String, Integer> chartLabelToId = new HashMap<>();
    private List<BloodSugarRecord> currentChartRecords = new ArrayList<>();

    // 表格复选框选中状态
    private Map<Integer, BooleanProperty> rowSelected = new HashMap<>();

    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FMT = DateTimeFormatter.ofPattern("HH:mm");
    private static final DateTimeFormatter CHART_FMT = DateTimeFormatter.ofPattern("MM-dd HH:mm");

    // ==================== 糖果马卡龙配色常量（鲜艳可爱，摆脱灰蒙） ====================
    private static final String COLOR_BG = "#EAFCF0";          // 主背景 薄荷糖白
    private static final String COLOR_PANEL = "#FFFFFF";       // 面板 纯白
    private static final String COLOR_BORDER = "#B8EED0";      // 边框 亮薄荷绿
    private static final String COLOR_TEXT = "#3A4A42";        // 正文 深灰绿
    private static final String COLOR_TITLE = "#2BB673";       // 标题 糖果绿
    private static final String COLOR_TITLE_DARK = "#12A35C";  // 标题渐变深端
    private static final String COLOR_TITLE_LIGHT = "#7FE8B0"; // 标题渐变浅端
    private static final String COLOR_GREEN = "#3BC96E";       // 主按钮 亮糖绿
    private static final String COLOR_GREEN_LIGHT = "#7DE3A4"; // 主按钮浅端
    private static final String COLOR_GREEN_DARK = "#22A856";  // 主按钮深端
    private static final String COLOR_BLUE = "#3FB8FF";        // 次按钮 天蓝
    private static final String COLOR_BLUE_LIGHT = "#7FD3FF";  // 次按钮浅端
    private static final String COLOR_BLUE_DARK = "#1E9FE8";   // 次按钮深端
    private static final String COLOR_ORANGE = "#FFA45E";      // 刷新按钮 蜜桃橙
    private static final String COLOR_ORANGE_LIGHT = "#FFC48C";// 刷新按钮浅端
    private static final String COLOR_ORANGE_DARK = "#F58B36"; // 刷新按钮深端
    private static final String COLOR_PINK = "#FF8FB3";        // 樱粉 装饰
    private static final String COLOR_PINK_LIGHT = "#FFC4D9";  // 樱粉浅 装饰
    private static final String COLOR_MEAL_BG = "#FFF6E9";     // 用餐卡片 奶油蜜桃
    private static final String COLOR_MEAL_BG2 = "#FFE3C9";    // 用餐卡片渐变浅端
    private static final String COLOR_MEAL_BORDER = "#FFD2A8"; // 用餐卡片边框
    private static final String COLOR_NORMAL = "#2FC86B";      // 正常血糖 清新亮绿
    private static final String COLOR_HIGH = "#FF8A65";        // 偏高血糖 蜜桃珊瑚
    private static final String COLOR_HEADER = "#D9F9E4";      // 表头 亮薄荷
    private static final String COLOR_STATUS = "#EFFBF2";      // 状态栏 薄荷白

    /** 绿色系渐变文字（艺术字标题填充） */
    private static final LinearGradient GREEN_GRADIENT = new LinearGradient(0, 0, 1, 1, true,
            CycleMethod.NO_CYCLE,
            new Stop(0, Color.web(COLOR_TITLE_DARK)),
            new Stop(0.55, Color.web(COLOR_TITLE)),
            new Stop(1, Color.web(COLOR_TITLE_LIGHT)));

    /** 糖果系渐变文字（主标题艺术字填充：樱粉→蜜桃橙→亮糖绿） */
    private static final LinearGradient CANDY_GRADIENT = new LinearGradient(0, 0, 1, 1, true,
            CycleMethod.NO_CYCLE,
            new Stop(0, Color.web("#FF7EB3")),
            new Stop(0.5, Color.web("#FFA45E")),
            new Stop(1, Color.web("#2FC86B")));

    public Scene createScene(Stage stage) {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: linear-gradient(to bottom right, #F4FEF8, " + COLOR_BG + ", #CFF7E2);");
        root.setTop(buildToolbar(stage));
        root.setLeft(buildLeftPanel());
        root.setCenter(buildChartPanel());
        root.setBottom(buildStatusBar());
        refreshAll();
        Scene scene = new Scene(root, 1150, 720);
        // 表格渲染完成后设置表头糖果渐变圆角背景
        Platform.runLater(() -> {
            Node header = table.lookup(".column-header-background");
            if (header != null) {
                header.setStyle("-fx-background-color: linear-gradient(to bottom, " + COLOR_HEADER + ", #B5F0CE); "
                        + "-fx-background-radius: 18 18 0 0; -fx-border-color: transparent;");
            }
        });
        return scene;
    }

    // ==================== 工具栏 ====================
    private HBox buildToolbar(Stage stage) {
        HBox toolbar = new HBox(10);
        toolbar.setPadding(new Insets(12, 16, 12, 16));
        toolbar.setStyle("-fx-background-color: linear-gradient(to right, #FFFFFF, #E3FBF0, #FFF0F6); "
                + "-fx-border-color: " + COLOR_BORDER + "; -fx-border-width: 0 0 2 0;");
        toolbar.setAlignment(Pos.CENTER_LEFT);

        // 糖果圆点装饰条
        Circle dot1 = new Circle(5, Color.web(COLOR_PINK));
        Circle dot2 = new Circle(5, Color.web(COLOR_ORANGE));
        Circle dot3 = new Circle(5, Color.web(COLOR_GREEN));
        HBox dots = new HBox(4, dot1, dot2, dot3);
        dots.setAlignment(Pos.CENTER);

        // 艺术字主标题：糖果渐变 + 白色描边 + 彩色立体阴影
        Text title = new Text("血糖记录系统");
        title.setFont(Font.font("YouYuan", FontWeight.BOLD, 24));
        title.setFill(CANDY_GRADIENT);
        title.setStroke(Color.WHITE);
        title.setStrokeWidth(1.2);
        title.setEffect(new DropShadow(6, 3, 3, Color.web("#8FE8B8")));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button addBtn = styledButton("+ 添加记录", COLOR_GREEN_LIGHT, COLOR_GREEN_DARK, "#A9F0C4", COLOR_GREEN_LIGHT, "#1E9E50", "#7DE3A4");
        addBtn.setOnAction(e -> showAddDialog(stage));

        Button summaryBtn = styledButton("生成总结", COLOR_BLUE_LIGHT, COLOR_BLUE_DARK, "#B5E7FF", COLOR_BLUE_LIGHT, "#1899E0", "#7FD3FF");
        summaryBtn.setOnAction(e -> showSummaryDialog(stage));

        Button refreshBtn = styledButton("刷新", COLOR_ORANGE_LIGHT, COLOR_ORANGE_DARK, "#FFDDB8", COLOR_ORANGE_LIGHT, "#F08A33", "#FFC48C");
        refreshBtn.setOnAction(e -> refreshAll());

        toolbar.getChildren().addAll(dots, title, spacer, addBtn, summaryBtn, refreshBtn);
        return toolbar;
    }

    /** 胖乎乎糖果按钮样式：亮渐变 + 大圆角 + 底部深色立体边 */
    private String buttonStyle(String from, String to, String border) {
        return "-fx-background-color: linear-gradient(to bottom, " + from + ", " + to + "); "
                + "-fx-text-fill: white; -fx-font-size: 14px; -fx-padding: 11 22; "
                + "-fx-background-radius: 20; -fx-border-radius: 20; "
                + "-fx-border-color: " + border + "; -fx-border-width: 0 0 4 0; "
                + "-fx-cursor: hand; -fx-font-weight: bold;";
    }

    /** 糖果按钮：胖圆角 + 彩色立体阴影 + 悬停变亮 */
    private Button styledButton(String text, String from, String to, String hoverFrom, String hoverTo,
                                String border, String shadow) {
        Button btn = new Button(text);
        btn.setStyle(buttonStyle(from, to, border));
        btn.setEffect(new DropShadow(5, 3, 4, Color.web(shadow)));
        btn.setOnMouseEntered(e -> btn.setStyle(buttonStyle(hoverFrom, hoverTo, border)));
        btn.setOnMouseExited(e -> btn.setStyle(buttonStyle(from, to, border)));
        return btn;
    }

    /** 艺术字子标题：糖果渐变填充 + 柔和彩色阴影 */
    private Label artLabel(String text, double size) {
        Label label = new Label(text);
        label.setFont(Font.font("YouYuan", FontWeight.BOLD, size));
        label.setTextFill(CANDY_GRADIENT);
        label.setEffect(new DropShadow(2, 1, 1, Color.web("#FFD3E2")));
        return label;
    }

    /** 统一对话框：糖果奶油渐变背景 + 大圆角 + 糖果渐变按钮 */
    private void styleDialogPane(DialogPane pane) {
        pane.setStyle("-fx-background-color: linear-gradient(to bottom right, #FFF6E9, " + COLOR_BG + ", #FFF0F6); "
                + "-fx-background-radius: 20; -fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 20;");
        Button ok = (Button) pane.lookupButton(ButtonType.OK);
        if (ok != null) {
            ok.setStyle(buttonStyle(COLOR_GREEN_LIGHT, COLOR_GREEN_DARK, "#1E9E50"));
            ok.setEffect(new DropShadow(4, 2, 3, Color.web("#7DE3A4")));
            ok.setOnMouseEntered(e -> ok.setStyle(buttonStyle("#A9F0C4", COLOR_GREEN_LIGHT, "#1E9E50")));
            ok.setOnMouseExited(e -> ok.setStyle(buttonStyle(COLOR_GREEN_LIGHT, COLOR_GREEN_DARK, "#1E9E50")));
        }
        Button cancel = (Button) pane.lookupButton(ButtonType.CANCEL);
        if (cancel != null) {
            String normal = "-fx-background-color: linear-gradient(to bottom, #FFD3E2, #FFA8C5); "
                    + "-fx-text-fill: #8C3B52; -fx-font-size: 13px; -fx-padding: 8 20; "
                    + "-fx-background-radius: 18; -fx-border-radius: 18; "
                    + "-fx-border-color: #E87FA5; -fx-border-width: 0 0 3 0; "
                    + "-fx-cursor: hand; -fx-font-weight: bold;";
            String hover = "-fx-background-color: linear-gradient(to bottom, #FFE0EA, #FFB9D1); "
                    + "-fx-text-fill: #8C3B52; -fx-font-size: 13px; -fx-padding: 8 20; "
                    + "-fx-background-radius: 18; -fx-border-radius: 18; "
                    + "-fx-border-color: #E87FA5; -fx-border-width: 0 0 3 0; "
                    + "-fx-cursor: hand; -fx-font-weight: bold;";
            cancel.setStyle(normal);
            cancel.setEffect(new DropShadow(4, 2, 3, Color.web("#FFC4D9")));
            cancel.setOnMouseEntered(e -> cancel.setStyle(hover));
            cancel.setOnMouseExited(e -> cancel.setStyle(normal));
        }
    }

    // ==================== 左侧面板 ====================
    private VBox buildLeftPanel() {
        VBox left = new VBox(8);
        left.setPadding(new Insets(12));
        left.setPrefWidth(400);
        left.setStyle("-fx-background-color: " + COLOR_PANEL + "; "
                + "-fx-border-color: " + COLOR_BORDER + "; -fx-border-width: 0 2 0 0;");

        // 糖果圆点装饰条
        HBox deco = new HBox(5);
        deco.getChildren().addAll(
                new Circle(4, Color.web(COLOR_PINK)),
                new Circle(4, Color.web(COLOR_ORANGE)),
                new Circle(4, Color.web(COLOR_GREEN)),
                new Circle(4, Color.web(COLOR_BLUE)));
        Label filterLabel = artLabel("日期筛选", 13);
        dateFilterCombo = new ComboBox<>();
        dateFilterCombo.setPromptText("全部日期");
        dateFilterCombo.setPrefWidth(Double.MAX_VALUE);
        dateFilterCombo.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; "
                + "-fx-border-color: " + COLOR_MEAL_BORDER + "; -fx-background-color: #FFFFFF;");
        dateFilterCombo.setOnAction(e -> refreshTableAndChart());

        Label tableLabel = artLabel("血糖记录", 13);

        table = new TableView<>();
        table.setEditable(true);
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        table.setStyle("-fx-background-color: transparent; -fx-background-radius: 12; "
                + "-fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 12; "
                + "-fx-border-width: 1; -fx-control-inner-background: #FFFFFF; "
                + "-fx-table-cell-border-color: transparent;");

        TableColumn<BloodSugarRecord, Boolean> checkCol = new TableColumn<>("");
        checkCol.setCellValueFactory(p -> {
            BloodSugarRecord rec = p.getValue();
            BooleanProperty prop = rowSelected.computeIfAbsent(rec.getId(), k -> new SimpleBooleanProperty(false));
            // 列表数据变化时清理孤立选项
            prop.addListener((obs, oldV, newV) -> {});
            return prop;
        });
        checkCol.setCellFactory(CheckBoxTableCell.forTableColumn(checkCol));
        checkCol.setPrefWidth(32);
        checkCol.setSortable(false);

        TableColumn<BloodSugarRecord, String> timeCol = new TableColumn<>("时间");
        timeCol.setCellValueFactory(p -> {
            LocalDateTime t = p.getValue().getRecordTime();
            return new javafx.beans.property.SimpleStringProperty(
                    t != null ? t.format(CHART_FMT) : "");
        });
        timeCol.setPrefWidth(85);

        TableColumn<BloodSugarRecord, Double> sugarCol = new TableColumn<>("血糖");
        sugarCol.setCellValueFactory(new PropertyValueFactory<>("bloodSugar"));
        sugarCol.setPrefWidth(55);
        sugarCol.setCellFactory(col -> new TableCell<>() {
            @Override
            protected void updateItem(Double val, boolean empty) {
                super.updateItem(val, empty);
                if (empty || val == null) { setText(null); setStyle(""); return; }
                setText(String.format("%.1f", val));
                BloodSugarRecord row = getTableView().getItems().get(getIndex());
                boolean normal = PeriodClassifier.isNormal(row.getMealPeriod(), val);
                setTextFill(normal ? Color.web(COLOR_NORMAL) : Color.web(COLOR_HIGH));
                setStyle("-fx-font-weight: bold;");
            }
        });

        TableColumn<BloodSugarRecord, String> periodCol = new TableColumn<>("时段");
        periodCol.setCellValueFactory(new PropertyValueFactory<>("mealPeriod"));
        periodCol.setPrefWidth(70);

        TableColumn<BloodSugarRecord, String> typeCol = new TableColumn<>("餐别");
        typeCol.setCellValueFactory(new PropertyValueFactory<>("mealType"));
        typeCol.setPrefWidth(55);

        TableColumn<BloodSugarRecord, String> noteCol = new TableColumn<>("备注");
        noteCol.setCellValueFactory(new PropertyValueFactory<>("note"));
        noteCol.setPrefWidth(105);

        table.getColumns().addAll(checkCol, timeCol, sugarCol, periodCol, typeCol, noteCol);

        HBox btnBar = new HBox(6);
        Button editBtn = new Button("修改");
        editBtn.setStyle("-fx-text-fill: " + COLOR_BLUE + "; -fx-font-size: 12px; -fx-cursor: hand; -fx-font-weight: bold;");
        editBtn.setOnAction(e -> editSelected());

        Button deleteBtn = new Button("删除");
        deleteBtn.setStyle("-fx-text-fill: " + COLOR_HIGH + "; -fx-font-size: 12px; -fx-cursor: hand; -fx-font-weight: bold;");
        deleteBtn.setOnAction(e -> deleteSelected());
        btnBar.getChildren().addAll(editBtn, deleteBtn);

        VBox mealPanel = buildMealPanel();
        VBox.setVgrow(table, Priority.ALWAYS);
        left.getChildren().addAll(deco, filterLabel, dateFilterCombo, tableLabel, table, btnBar, mealPanel);
        return left;
    }

    private VBox buildMealPanel() {
        VBox panel = new VBox(6);
        panel.setPadding(new Insets(12));
        panel.setStyle("-fx-background-color: linear-gradient(to bottom, " + COLOR_MEAL_BG + ", " + COLOR_MEAL_BG2 + "); "
                + "-fx-background-radius: 20; "
                + "-fx-border-color: " + COLOR_MEAL_BORDER + "; -fx-border-radius: 20; "
                + "-fx-border-width: 2;");
        panel.setEffect(new DropShadow(6, 3, 4, Color.web("#FFD2A8")));

        Label title = artLabel("今日用餐时间", 13);

        GridPane grid = new GridPane();
        grid.setHgap(8);
        grid.setVgap(6);

        breakfastField = createMealTimeField("早餐");
        lunchField = createMealTimeField("午餐");
        dinnerField = createMealTimeField("晚餐");
        extraMealField = createMealTimeField("加餐");

        Label breakfastLb = new Label("早餐");
        breakfastLb.setTextFill(Color.web(COLOR_TITLE));
        Label lunchLb = new Label("午餐");
        lunchLb.setTextFill(Color.web(COLOR_TITLE));
        Label dinnerLb = new Label("晚餐");
        dinnerLb.setTextFill(Color.web(COLOR_TITLE));
        Label extraLb = new Label("加餐");
        extraLb.setTextFill(Color.web(COLOR_TITLE));

        grid.add(breakfastLb, 0, 0);
        grid.add(breakfastField, 1, 0);
        grid.add(lunchLb, 0, 1);
        grid.add(lunchField, 1, 1);
        grid.add(dinnerLb, 0, 2);
        grid.add(dinnerField, 1, 2);
        grid.add(extraLb, 0, 3);
        grid.add(extraMealField, 1, 3);

        Button saveMealBtn = styledButton("保存用餐时间", COLOR_GREEN_LIGHT, COLOR_GREEN_DARK, "#A9F0C4", COLOR_GREEN_LIGHT, "#1E9E50", "#7DE3A4");
        saveMealBtn.setStyle(saveMealBtn.getStyle() + " -fx-font-size: 12px; -fx-padding: 9 18;");
        String mealNormal = saveMealBtn.getStyle();
        String mealHover = buttonStyle("#A9F0C4", COLOR_GREEN_LIGHT, "#1E9E50") + " -fx-font-size: 12px; -fx-padding: 9 18;";
        saveMealBtn.setOnMouseEntered(e -> saveMealBtn.setStyle(mealHover));
        saveMealBtn.setOnMouseExited(e -> saveMealBtn.setStyle(mealNormal));
        saveMealBtn.setOnAction(e -> statusLabel.setText("用餐时间已保存"));

        panel.getChildren().addAll(title, grid, saveMealBtn);
        return panel;
    }

    private TextField createMealTimeField(String placeholder) {
        TextField tf = new TextField();
        tf.setPromptText(placeholder + " HH:mm");
        tf.setPrefWidth(80);
        tf.setStyle("-fx-background-radius: 8; -fx-border-radius: 8; "
                + "-fx-border-color: " + COLOR_MEAL_BORDER + "; -fx-background-color: #FFFFFF;");
        return tf;
    }

    /**
     * 用餐时间信息：记录时间 + 餐名
     */
    private static class MealTimeInfo {
        final LocalDateTime time;
        final String mealName; // "早餐" / "午餐" / "晚餐" / "加餐"
        MealTimeInfo(LocalDateTime time, String mealName) {
            this.time = time;
            this.mealName = mealName;
        }
    }

    /**
     * 综合左侧面板的今日用餐时间（优先）和数据库历史记录，
     * 返回指定时间之前最近一次用餐时间及其餐名。
     */
    private MealTimeInfo resolveNearestMealTime(LocalDateTime recordTime) {
        MealTimeInfo best = null;

        // 1. 依次检查四个餐别的面板时间（面板时间带餐名，优先）
        best = updateBest(best, parseMealField(breakfastField), "早餐", recordTime);
        best = updateBest(best, parseMealField(lunchField), "午餐", recordTime);
        best = updateBest(best, parseMealField(dinnerField), "晚餐", recordTime);
        best = updateBest(best, parseMealField(extraMealField), "加餐", recordTime);

        // 2. 从数据库查询记录时间所在业务日（凌晨4点起）内的历史用餐记录（无明确餐名，降级为"用餐"）
        //    业务日内无用餐记录时返回 null，最终餐别显示"空腹"
        try {
            LocalDateTime dbMealTime = service.getLatestMealTimeBefore(recordTime);
            if (dbMealTime != null) {
                best = updateBest(best, dbMealTime, "用餐", recordTime);
            }
        } catch (SQLException ignored) { }

        return best;
    }

    private MealTimeInfo updateBest(MealTimeInfo best, LocalDateTime mt, String name, LocalDateTime recordTime) {
        if (mt == null) return best;
        if (mt.isAfter(recordTime)) return best;
        if (best == null || mt.isAfter(best.time)) {
            return new MealTimeInfo(mt, name);
        }
        return best;
    }

    /**
     * 根据测量时间和最近用餐信息，生成精确的餐别描述。
     * 如：早餐后 2小时35分钟、空腹、睡前
     * 规则：新的一天（凌晨4点起）内无任何用餐数据 → 空腹，不计算距上一餐时间。
     */
    private String computeMealTypeDescription(LocalDateTime recordTime, MealTimeInfo mealInfo) {
        if (recordTime == null) return "空腹";
        // 新的一天（凌晨4点起）内无任何用餐数据 → 空腹
        if (mealInfo == null) return "空腹";
        // 晚上 22:00 以后 → 睡前
        if (recordTime.toLocalTime().getHour() >= 22) return "睡前";

        long minutes = java.time.Duration.between(mealInfo.time, recordTime).toMinutes();
        if (minutes < 0) return "空腹"; // 记录时间早于用餐时间

        long hours = minutes / 60;
        long mins = minutes % 60;

        if (hours == 0) {
            return mealInfo.mealName + "后 " + mins + "分钟";
        }
        if (mins == 0) {
            return mealInfo.mealName + "后 " + hours + "小时";
        }
        return mealInfo.mealName + "后 " + hours + "小时" + mins + "分钟";
    }

    private LocalDateTime parseMealField(TextField field) {
        String s = field.getText().trim();
        if (s.isEmpty()) return null;
        try {
            // 面板用餐时间按"今日业务日"（凌晨4点边界）解析：凌晨 0-4 点归属前一天
            LocalDate businessToday = PeriodClassifier.getBusinessDate(LocalDateTime.now());
            return LocalDateTime.of(businessToday, LocalTime.parse(s.replace('.', ':')));
        } catch (Exception e) {
            return null;
        }
    }

    // ==================== 图表面板 ====================
    private VBox buildChartPanel() {
        VBox panel = new VBox(8);
        panel.setPadding(new Insets(14));
        panel.setStyle("-fx-background-color: " + COLOR_PANEL + "; -fx-background-radius: 20; "
                + "-fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 20; -fx-border-width: 2;");
        panel.setEffect(new DropShadow(7, 3, 4, Color.web("#8FE8B8")));

        // 糖果圆点装饰条
        HBox deco = new HBox(5);
        deco.getChildren().addAll(
                new Circle(4, Color.web(COLOR_GREEN)),
                new Circle(4, Color.web(COLOR_BLUE)),
                new Circle(4, Color.web(COLOR_ORANGE)),
                new Circle(4, Color.web(COLOR_PINK)));

        Label chartTitle = artLabel("血糖趋势曲线", 14);

        yAxis = new NumberAxis("血糖 (mmol/L)", 0, 20, 1);
        javafx.scene.chart.CategoryAxis xAxis = new javafx.scene.chart.CategoryAxis();
        xAxis.setLabel("时间");

        chart = new LineChart<>(xAxis, yAxis);
        chart.setPrefHeight(520);
        chart.setAnimated(false);
        chart.setLegendVisible(true);
        chart.setCreateSymbols(true);
        chart.setLegendSide(javafx.geometry.Side.TOP);
        chart.setStyle("-fx-background-color: #FDFEFB; -fx-background-radius: 16; "
                + "-fx-border-color: " + COLOR_BORDER + "; -fx-border-radius: 16; "
                + "-fx-border-width: 1;");
        // 数据点右键菜单
        chart.setOnMouseClicked(e -> {
            if (e.getButton() == MouseButton.SECONDARY) {
                handleChartRightClick();
            }
        });

        panel.getChildren().addAll(deco, chartTitle, chart);
        return panel;
    }

    private void handleChartRightClick() {
        // 遍历所有数据点，找到鼠标所在的数据点
        for (XYChart.Series<String, Number> series : chart.getData()) {
            for (XYChart.Data<String, Number> data : series.getData()) {
                Node node = data.getNode();
                if (node != null && node.isHover()) {
                    String label = data.getXValue();
                    Integer id = chartLabelToId.get(label);
                    if (id != null) {
                        showChartDeleteMenu(id, label, data.getYValue().doubleValue(), node);
                    }
                    return;
                }
            }
        }
    }

    private void showChartDeleteMenu(int recordId, String label, double bloodSugar, Node anchor) {
        ContextMenu menu = new ContextMenu();
        MenuItem deleteItem = new MenuItem(String.format("删除 %s (%.1f mmol/L)", label, bloodSugar));
        deleteItem.setStyle("-fx-text-fill: " + COLOR_HIGH + ";");
        deleteItem.setOnAction(e -> {
            try {
                service.deleteRecord(recordId);
                refreshAll();
                statusLabel.setText("已删除记录");
            } catch (SQLException ex) {
                showAlert("删除失败：" + ex.getMessage());
            }
        });
        menu.getItems().add(deleteItem);
        menu.show(anchor, javafx.geometry.Side.BOTTOM, 0, 0);
    }

    // ==================== 状态栏 ====================
    private HBox buildStatusBar() {
        HBox bar = new HBox();
        bar.setPadding(new Insets(8, 16, 8, 16));
        bar.setStyle("-fx-background-color: linear-gradient(to right, " + COLOR_STATUS + ", #D9FBE9, #FFE9F1); "
                + "-fx-border-color: " + COLOR_BORDER + "; -fx-border-width: 2 0 0 0;");
        statusLabel = new Label("就绪");
        statusLabel.setFont(Font.font("YouYuan", 12));
        statusLabel.setTextFill(Color.web(COLOR_TEXT));
        bar.getChildren().add(statusLabel);
        return bar;
    }

    // ==================== 添加记录 ====================
    private void showAddDialog(Stage owner) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("添加血糖记录");
        dialog.setHeaderText("请输入血糖测量信息");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        DatePicker datePicker = new DatePicker(LocalDate.now());
        grid.add(new Label("测量日期:"), 0, 0);
        grid.add(datePicker, 1, 0);

        TextField timeField = new TextField(LocalTime.now().format(TIME_FMT));
        timeField.setPromptText("HH:mm");
        // 用户输入 "." 时自动替换为 ":"
        timeField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getText().contains(".")) {
                change.setText(change.getText().replace('.', ':'));
            }
            return change;
        }));
        grid.add(new Label("测量时间:"), 0, 1);
        grid.add(timeField, 1, 1);

        TextField sugarField = new TextField();
        sugarField.setPromptText("例如 5.6");
        grid.add(new Label("血糖值 (mmol/L):"), 0, 2);
        grid.add(sugarField, 1, 2);

        // 餐别自动识别：使用 Label 显示，对话框打开时实时计算
        Label mealTypeLabel = new Label("空腹");
        mealTypeLabel.setStyle("-fx-background-color: linear-gradient(to bottom, " + COLOR_GREEN_LIGHT + ", " + COLOR_GREEN_DARK + "); "
                + "-fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 5 14; -fx-background-radius: 14;");
        grid.add(new Label("餐别(自动):"), 0, 3);
        grid.add(mealTypeLabel, 1, 3);

        TextField noteField = new TextField();
        noteField.setPromptText("选填");
        grid.add(new Label("备注:"), 0, 4);
        grid.add(noteField, 1, 4);

        Label hintLabel = new Label("餐别根据最近用餐记录自动识别，无需手动选择");
        hintLabel.setFont(Font.font(11));
        hintLabel.setTextFill(Color.web("#8A8578"));
        grid.add(hintLabel, 1, 5);

        // 测量日期/时间变化时，实时刷新自动识别的餐别
        Runnable refreshMealType = () -> {
            try {
                LocalDate d = datePicker.getValue();
                LocalTime t = LocalTime.parse(timeField.getText().trim().replace('.', ':'));
                LocalDateTime rt = LocalDateTime.of(d, t);
                MealTimeInfo info = resolveNearestMealTime(rt);
                mealTypeLabel.setText(computeMealTypeDescription(rt, info));
            } catch (Exception ex) {
                mealTypeLabel.setText("空腹");
            }
        };
        datePicker.valueProperty().addListener((obs, o, n) -> refreshMealType.run());
        timeField.textProperty().addListener((obs, o, n) -> refreshMealType.run());

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleDialogPane(dialog.getDialogPane());

        // 对话框展示后立即执行一次自动识别
        dialog.setOnShown(e -> refreshMealType.run());

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            try {
                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.parse(timeField.getText().trim().replace('.', ':'));
                LocalDateTime recordTime = LocalDateTime.of(date, time);
                double bloodSugar = Double.parseDouble(sugarField.getText().trim());
                if (bloodSugar <= 0 || bloodSugar > 50) {
                    showAlert("血糖值应在 0 ~ 50 mmol/L 之间");
                    return null;
                }
                MealTimeInfo mealInfo = resolveNearestMealTime(recordTime);
                LocalDateTime mealTime = mealInfo != null ? mealInfo.time : null;
                String mealType = computeMealTypeDescription(recordTime, mealInfo);
                service.addRecord(recordTime, bloodSugar, mealTime, mealType, noteField.getText().trim());
                refreshAll();

                String period = com.bloodsugar.util.PeriodClassifier.classify(recordTime, mealTime);
                showAdviceDialog(owner, bloodSugar, period);
                return btn;
            } catch (Exception ex) {
                showAlert("输入格式有误：" + ex.getMessage());
                return null;
            }
        });
        dialog.showAndWait();
    }

    // ==================== 健康建议 ====================
    private void showAdviceDialog(Stage owner, double bloodSugar, String period) {
        String advice = generateAdvice(bloodSugar, period);
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle("血糖建议");
        alert.setHeaderText(String.format("测量结果：%.1f mmol/L  [%s]", bloodSugar, period));
        Label content = new Label(advice);
        content.setFont(Font.font("Microsoft YaHei", 14));
        content.setWrapText(true);
        content.setMaxWidth(400);
        alert.getDialogPane().setContent(content);
        alert.getDialogPane().setStyle("-fx-background-color: linear-gradient(to bottom right, " + COLOR_MEAL_BG + ", #FFFFFF, #FFF0F6); "
                + "-fx-background-radius: 20; -fx-border-color: " + COLOR_MEAL_BORDER + "; -fx-border-radius: 20;");
        styleDialogPane(alert.getDialogPane());
        alert.showAndWait();
    }

    private String generateAdvice(double bloodSugar, String period) {
        double[] range = PeriodClassifier.getNormalRange(period);
        if (bloodSugar < 3.0)
            return "⚠ 血糖严重偏低，请立即补充糖分（喝果汁、吃糖果），15分钟后复测。如持续偏低请就医。";
        if (bloodSugar < 3.9)
            return "血糖偏低，建议立即进食含碳水化合物的食物（饼干、面包等），稍后复测。";
        if (bloodSugar >= 3.9 && bloodSugar <= range[1]) {
            if (period != null && period.contains("空腹"))
                return "空腹血糖正常，控制良好。建议继续保持规律饮食和运动。";
            return "餐后血糖在正常范围内，饮食控制得当。建议餐后适度散步有助于血糖平稳。";
        }
        if (bloodSugar > range[1] && bloodSugar <= range[1] + 2.0)
            return "血糖略偏高，注意本次饮食中的碳水化合物摄入量（米饭/面食/甜食），建议餐后散步30分钟。";
        return String.format("血糖明显偏高（正常上限 %.1f mmol/L），请避免高糖食物，增加运动量。如多次偏高请咨询医生。", range[1]);
    }

    // ==================== 删除 ====================
    private void deleteSelected() {
        List<BloodSugarRecord> selected = getCheckedRecords();
        if (selected.isEmpty()) {
            showAlert("请先勾选要删除的记录");
            return;
        }
        String names = selected.stream()
                .map(r -> r.getRecordTime() != null ? r.getRecordTime().format(TIME_FMT) + " " + r.getBloodSugar() : "")
                .collect(Collectors.joining("、"));
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("确认删除");
        confirm.setHeaderText("确定要删除这 " + selected.size() + " 条记录吗？");
        confirm.setContentText(names);
        confirm.showAndWait().ifPresent(r -> {
            if (r == ButtonType.OK) {
                try {
                    for (BloodSugarRecord rec : selected) {
                        service.deleteRecord(rec.getId());
                    }
                    refreshAll();
                    statusLabel.setText("已删除 " + selected.size() + " 条");
                } catch (SQLException e) {
                    showAlert("删除失败：" + e.getMessage());
                }
            }
        });
    }

    private void editSelected() {
        List<BloodSugarRecord> selected = getCheckedRecords();
        if (selected.isEmpty()) {
            showAlert("请先勾选一条记录进行修改");
            return;
        }
        if (selected.size() > 1) {
            showAlert("一次只能修改一条记录，请只勾选一条");
            return;
        }
        Stage stage = (Stage) table.getScene().getWindow();
        showEditDialog(stage, selected.get(0));
    }

    private List<BloodSugarRecord> getCheckedRecords() {
        return table.getItems().stream()
                .filter(r -> {
                    BooleanProperty prop = rowSelected.get(r.getId());
                    return prop != null && prop.get();
                })
                .collect(Collectors.toList());
    }

    private void showEditDialog(Stage owner, BloodSugarRecord rec) {
        Dialog<ButtonType> dialog = new Dialog<>();
        dialog.initOwner(owner);
        dialog.setTitle("修改血糖记录");

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(12);
        grid.setPadding(new Insets(20));

        LocalDateTime rt = rec.getRecordTime();
        DatePicker datePicker = new DatePicker(
                rt != null ? rt.toLocalDate() : LocalDate.now());
        grid.add(new Label("测量日期:"), 0, 0);
        grid.add(datePicker, 1, 0);

        TextField timeField = new TextField(
                rt != null ? rt.format(TIME_FMT) : LocalTime.now().format(TIME_FMT));
        timeField.setPromptText("HH:mm");
        // 用户输入 "." 时自动替换为 ":"
        timeField.setTextFormatter(new TextFormatter<String>(change -> {
            if (change.getText().contains(".")) {
                change.setText(change.getText().replace('.', ':'));
            }
            return change;
        }));
        grid.add(new Label("测量时间:"), 0, 1);
        grid.add(timeField, 1, 1);

        TextField sugarField = new TextField(String.valueOf(rec.getBloodSugar()));
        sugarField.setPromptText("例如 5.6");
        grid.add(new Label("血糖值 (mmol/L):"), 0, 2);
        grid.add(sugarField, 1, 2);

        // 餐别自动识别（编辑时仍按新时间重新计算）
        Label editMealTypeLabel = new Label("空腹");
        editMealTypeLabel.setStyle("-fx-background-color: linear-gradient(to bottom, " + COLOR_GREEN_LIGHT + ", " + COLOR_GREEN_DARK + "); "
                + "-fx-text-fill: white; "
                + "-fx-font-weight: bold; -fx-padding: 5 14; -fx-background-radius: 14;");
        grid.add(new Label("餐别(自动):"), 0, 3);
        grid.add(editMealTypeLabel, 1, 3);

        TextField noteField = new TextField(rec.getNote() != null ? rec.getNote() : "");
        noteField.setPromptText("选填");
        grid.add(new Label("备注:"), 0, 4);
        grid.add(noteField, 1, 4);

        dialog.getDialogPane().setContent(grid);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);
        styleDialogPane(dialog.getDialogPane());

        // 日期/时间变化时实时刷新餐别
        Runnable refreshEditMealType = () -> {
            try {
                LocalDate d = datePicker.getValue();
                LocalTime t = LocalTime.parse(timeField.getText().trim().replace('.', ':'));
                LocalDateTime newRt = LocalDateTime.of(d, t);
                MealTimeInfo info = resolveNearestMealTime(newRt);
                editMealTypeLabel.setText(computeMealTypeDescription(newRt, info));
            } catch (Exception ex) {
                editMealTypeLabel.setText("空腹");
            }
        };
        datePicker.valueProperty().addListener((obs, o, n) -> refreshEditMealType.run());
        timeField.textProperty().addListener((obs, o, n) -> refreshEditMealType.run());

        // 对话框展示后立即执行一次自动识别
        dialog.setOnShown(e -> refreshEditMealType.run());

        dialog.setResultConverter(btn -> {
            if (btn != ButtonType.OK) return null;
            try {
                LocalDate date = datePicker.getValue();
                LocalTime time = LocalTime.parse(timeField.getText().trim().replace('.', ':'));
                double bloodSugar = Double.parseDouble(sugarField.getText().trim());
                if (bloodSugar <= 0 || bloodSugar > 50) {
                    showAlert("血糖值应在 0 ~ 50 mmol/L 之间");
                    return null;
                }
                LocalDateTime recordTime = LocalDateTime.of(date, time);
                MealTimeInfo mealInfo = resolveNearestMealTime(recordTime);
                rec.setRecordTime(recordTime);
                rec.setBloodSugar(bloodSugar);
                rec.setMealTime(mealInfo != null ? mealInfo.time : null);
                rec.setMealType(computeMealTypeDescription(recordTime, mealInfo));
                rec.setNote(noteField.getText().trim());
                service.updateRecord(rec);
                // 更新后取消勾选
                BooleanProperty prop = rowSelected.get(rec.getId());
                if (prop != null) prop.set(false);
                refreshAll();
                statusLabel.setText("已更新");
                return btn;
            } catch (Exception ex) {
                showAlert("输入格式有误：" + ex.getMessage());
                return null;
            }
        });
        dialog.showAndWait();
    }

    // ==================== 总结报告 ====================
    private void showSummaryDialog(Stage owner) {
        if (currentChartRecords.isEmpty()) {
            showAlert("暂无记录，请先添加血糖数据");
            return;
        }

        List<BloodSugarRecord> records = currentChartRecords;
        int total = records.size();

        // 基础统计
        DoubleSummaryStatistics stats = records.stream()
                .mapToDouble(BloodSugarRecord::getBloodSugar).summaryStatistics();

        long normalCount = records.stream()
                .filter(r -> PeriodClassifier.isNormal(r.getMealPeriod(), r.getBloodSugar())).count();

        int highCount = (int) (total - normalCount);

        // 按空腹/餐后分组
        List<BloodSugarRecord> fasting = records.stream()
                .filter(r -> "空腹".equals(r.getMealPeriod()))
                .sorted(Comparator.comparing(BloodSugarRecord::getRecordTime))
                .collect(Collectors.toList());

        List<BloodSugarRecord> post2h = records.stream()
                .filter(r -> "餐后2h".equals(r.getMealPeriod()))
                .sorted(Comparator.comparing(BloodSugarRecord::getRecordTime))
                .collect(Collectors.toList());

        List<BloodSugarRecord> post1h = records.stream()
                .filter(r -> "餐后1h".equals(r.getMealPeriod()))
                .collect(Collectors.toList());

        List<BloodSugarRecord> post3h = records.stream()
                .filter(r -> "餐后3h".equals(r.getMealPeriod()))
                .collect(Collectors.toList());

        // 糖尿病风险评估
        String diabetesRisk = assessDiabetesRisk(fasting, post2h, post1h, records);
        String insulinRisk = assessInsulinResistanceRisk(fasting, post1h, post2h, records);

        // 构建总结文本
        StringBuilder sb = new StringBuilder();
        sb.append(String.format("记录总数：%d 条\n", total));
        sb.append(String.format("测量时间范围：%s  ~  %s\n\n",
                records.get(0).getRecordTime().format(CHART_FMT),
                records.get(records.size() - 1).getRecordTime().format(CHART_FMT)));

        sb.append("── 血糖统计 ──\n");
        sb.append(String.format("  平均值：%.1f mmol/L\n", stats.getAverage()));
        sb.append(String.format("  最高值：%.1f mmol/L\n", stats.getMax()));
        sb.append(String.format("  最低值：%.1f mmol/L\n", stats.getMin()));
        sb.append(String.format("  正常率：%d/%d (%.0f%%)\n", normalCount, total, 100.0 * normalCount / total));
        sb.append(String.format("  偏高次数：%d 次\n\n", highCount));

        if (!fasting.isEmpty()) {
            DoubleSummaryStatistics fs = fasting.stream().mapToDouble(BloodSugarRecord::getBloodSugar).summaryStatistics();
            sb.append("── 空腹血糖（" + fasting.size() + " 次）──\n");
            sb.append(String.format("  平均：%.1f  最高：%.1f  最低：%.1f\n\n", fs.getAverage(), fs.getMax(), fs.getMin()));
        }
        if (!post1h.isEmpty()) {
            DoubleSummaryStatistics ps = post1h.stream().mapToDouble(BloodSugarRecord::getBloodSugar).summaryStatistics();
            sb.append("── 餐后1h（" + post1h.size() + " 次）──\n");
            sb.append(String.format("  平均：%.1f  最高：%.1f  最低：%.1f\n\n", ps.getAverage(), ps.getMax(), ps.getMin()));
        }
        if (!post2h.isEmpty()) {
            DoubleSummaryStatistics ps = post2h.stream().mapToDouble(BloodSugarRecord::getBloodSugar).summaryStatistics();
            sb.append("── 餐后2h（" + post2h.size() + " 次）──\n");
            sb.append(String.format("  平均：%.1f  最高：%.1f  最低：%.1f\n\n", ps.getAverage(), ps.getMax(), ps.getMin()));
        }
        if (!post3h.isEmpty()) {
            DoubleSummaryStatistics ps = post3h.stream().mapToDouble(BloodSugarRecord::getBloodSugar).summaryStatistics();
            sb.append("── 餐后3h（" + post3h.size() + " 次）──\n");
            sb.append(String.format("  平均：%.1f  最高：%.1f  最低：%.1f\n\n", ps.getAverage(), ps.getMax(), ps.getMin()));
        }

        sb.append("── 风险评估 ──\n");
        sb.append(diabetesRisk).append("\n");
        sb.append(insulinRisk).append("\n\n");
        sb.append("⚠ 以上为程序自动分析，仅供参考，不能替代医生诊断。如有疑虑请及时就医。");

        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.initOwner(owner);
        alert.setTitle("血糖总结报告");
        alert.setHeaderText("统计数据与风险评估");

        TextArea textArea = new TextArea(sb.toString());
        textArea.setEditable(false);
        textArea.setWrapText(true);
        textArea.setFont(Font.font("Microsoft YaHei", 13));
        textArea.setPrefSize(520, 440);
        textArea.setStyle("-fx-control-inner-background: #FDFEFB; -fx-background-radius: 14; "
                + "-fx-border-radius: 14; -fx-border-color: " + COLOR_BORDER + ";");
        alert.getDialogPane().setContent(textArea);
        styleDialogPane(alert.getDialogPane());
        alert.showAndWait();
    }

    private String assessDiabetesRisk(List<BloodSugarRecord> fasting, List<BloodSugarRecord> post2h,
            List<BloodSugarRecord> post1h, List<BloodSugarRecord> all) {
        StringBuilder risk = new StringBuilder();

        boolean fastingHigh = false, fastingImpaired = false;
        if (!fasting.isEmpty()) {
            double maxFasting = fasting.stream().mapToDouble(BloodSugarRecord::getBloodSugar).max().orElse(0);
            double avgFasting = fasting.stream().mapToDouble(BloodSugarRecord::getBloodSugar).average().orElse(0);
            if (maxFasting >= 7.0) {
                fastingHigh = true;
            } else if (maxFasting >= 6.1) {
                fastingImpaired = true;
            }
            risk.append(String.format("  空腹最高 %.1f，平均 %.1f\n", maxFasting, avgFasting));
        }

        boolean post2hHigh = false, post2hImpaired = false;
        if (!post2h.isEmpty()) {
            double maxPost2h = post2h.stream().mapToDouble(BloodSugarRecord::getBloodSugar).max().orElse(0);
            if (maxPost2h >= 11.1) {
                post2hHigh = true;
            } else if (maxPost2h >= 7.8) {
                post2hImpaired = true;
            }
            risk.append(String.format("  餐后2h最高 %.1f\n", maxPost2h));
        }

        if (fastingHigh || post2hHigh) {
            risk.append("  【高风险】空腹≥7.0 或 餐后2h≥11.1，符合糖尿病诊断标准，强烈建议尽快就医做OGTT和HbA1c检查。\n");
        } else if (fastingImpaired || post2hImpaired) {
            risk.append("  【中风险】空腹6.1~7.0 或 餐后2h 7.8~11.1，属于糖尿病前期（空腹血糖受损/糖耐量减低），建议控制饮食、增加运动，并就医评估。\n");
        } else {
            risk.append("  【低风险】当前数据未达糖尿病或糖尿病前期标准。继续保持健康生活方式。\n");
        }

        // 随机血糖检查
        boolean randomHigh = all.stream().anyMatch(r -> r.getBloodSugar() >= 11.1
                && !"空腹".equals(r.getMealPeriod()));
        if (randomHigh) {
            risk.append("  ⚠ 存在随机血糖≥11.1 的记录（非空腹），也需警惕。\n");
        }

        return "【糖尿病风险评估】\n" + risk;
    }

    private String assessInsulinResistanceRisk(List<BloodSugarRecord> fasting, List<BloodSugarRecord> post1h,
            List<BloodSugarRecord> post2h, List<BloodSugarRecord> all) {
        StringBuilder risk = new StringBuilder();

        if (fasting.isEmpty() && post1h.isEmpty() && post2h.isEmpty()) {
            return "【胰岛素抵抗评估】\n  数据不足，需要空腹+餐后血糖记录才能评估。";
        }

        // 特征1：空腹正常但餐后飙升（胰岛素抵抗典型表现）
        boolean fastingNormal = false, post1hHigh = false;
        if (!fasting.isEmpty()) {
            double avgFasting = fasting.stream().mapToDouble(BloodSugarRecord::getBloodSugar).average().orElse(0);
            fastingNormal = avgFasting < 6.1;
            risk.append(String.format("  空腹平均值：%.1f mmol/L %s\n", avgFasting, fastingNormal ? "（正常）" : "（偏高）"));
        }
        if (!post1h.isEmpty()) {
            double avgPost1h = post1h.stream().mapToDouble(BloodSugarRecord::getBloodSugar).average().orElse(0);
            post1hHigh = avgPost1h > 10.0;
            risk.append(String.format("  餐后1h平均值：%.1f mmol/L\n", avgPost1h));
        }

        // 特征2：血糖波动幅度大
        double maxSugar = all.stream().mapToDouble(BloodSugarRecord::getBloodSugar).max().orElse(0);
        double minSugar = all.stream().mapToDouble(BloodSugarRecord::getBloodSugar).min().orElse(0);
        double range = maxSugar - minSugar;
        boolean wideFluctuation = range > 5.0;
        risk.append(String.format("  血糖波动幅度：%.1f (%.1f ~ %.1f)%s\n", range, minSugar, maxSugar,
                wideFluctuation ? " ← 波动偏大" : ""));

        // 综合判断
        int score = 0;
        if (fastingNormal && post1hHigh) score += 2; // 空腹正常+餐后飙升
        if (wideFluctuation) score += 1;
        double avgBloodSugar = all.stream().mapToDouble(BloodSugarRecord::getBloodSugar).average().orElse(0);
        if (avgBloodSugar > 7.0) score += 1;

        if (score >= 2) {
            risk.append("  【高风险】空腹正常但餐后明显升高 + 波动大，符合胰岛素抵抗典型特征。建议就医查空腹胰岛素、HOMA-IR 指数。\n");
        } else if (score == 1) {
            risk.append("  【中风险】部分指标提示可能存在胰岛素抵抗倾向，建议关注并定期监测。\n");
        } else {
            risk.append("  【低风险】当前数据未显示明显的胰岛素抵抗特征。\n");
        }

        return "【胰岛素抵抗评估】\n" + risk;
    }

    // ==================== 刷新 ====================
    private void refreshAll() {
        try {
            refreshDates();
            refreshTableAndChart();
        } catch (Exception e) {
            statusLabel.setText("刷新失败：" + e.getMessage());
        }
    }

    private void refreshDates() {
        try {
            String current = dateFilterCombo.getValue();
            dateFilterCombo.getItems().clear();
            dateFilterCombo.getItems().add("全部");
            dateFilterCombo.getItems().addAll(service.getRecordDates());
            if (current != null) dateFilterCombo.setValue(current);
            else dateFilterCombo.setValue("全部");
        } catch (SQLException ignored) {
        }
    }

    private void refreshTableAndChart() {
        try {
            String selected = dateFilterCombo.getValue();
            if (selected == null || "全部".equals(selected)) {
                currentChartRecords = service.getAllRecords();
            } else {
                LocalDate date = LocalDate.parse(selected);
                // 按凌晨 4:00 的业务日边界筛选：[当天4:00, 次日4:00)
                currentChartRecords = service.getRecordsByDateRange(
                        PeriodClassifier.getBusinessDayStart(date),
                        PeriodClassifier.getBusinessDayEnd(date));
            }

            table.setItems(FXCollections.observableArrayList(currentChartRecords));
            // 清理旧记录的复选框状态
            Set<Integer> currentIds = currentChartRecords.stream()
                    .map(BloodSugarRecord::getId).collect(Collectors.toSet());
            rowSelected.keySet().retainAll(currentIds);
            // 图表按时间升序（从左到右递增）
            List<BloodSugarRecord> chartData = new ArrayList<>(currentChartRecords);
            chartData.sort(Comparator.comparing(BloodSugarRecord::getRecordTime));
            updateChart(chartData);
        } catch (Exception e) {
            statusLabel.setText("加载失败：" + e.getMessage());
        }
    }

    // ==================== 趋势曲线 ====================
    private void updateChart(List<BloodSugarRecord> records) {
        chart.getData().clear();
        chartLabelToId.clear();

        // 根据数据最大值动态调整 Y 轴上限
        double maxSugar = records.stream().mapToDouble(BloodSugarRecord::getBloodSugar).max().orElse(8);
        double upperBound = Math.max(10, Math.ceil(maxSugar * 1.3));
        double tickUnit = upperBound <= 15 ? 1 : upperBound <= 25 ? 2 : 5;
        yAxis.setUpperBound(upperBound);
        yAxis.setTickUnit(tickUnit);

        XYChart.Series<String, Number> normalSeries = new XYChart.Series<>();
        normalSeries.setName("正常");
        XYChart.Series<String, Number> highSeries = new XYChart.Series<>();
        highSeries.setName("偏高");
        XYChart.Series<String, Number> upperLine = new XYChart.Series<>();
        upperLine.setName("正常上限");
        XYChart.Series<String, Number> lowerLine = new XYChart.Series<>();
        lowerLine.setName("正常下限");

        for (BloodSugarRecord r : records) {
            String label = r.getRecordTime() != null
                    ? r.getRecordTime().format(CHART_FMT) : "";
            double sugar = r.getBloodSugar();
            chartLabelToId.put(label, r.getId());
            boolean normal = PeriodClassifier.isNormal(r.getMealPeriod(), sugar);
            double[] range = PeriodClassifier.getNormalRange(r.getMealPeriod());

            XYChart.Data<String, Number> dataPoint = new XYChart.Data<>(label, sugar);
            if (normal) {
                normalSeries.getData().add(dataPoint);
            } else {
                highSeries.getData().add(dataPoint);
            }
            upperLine.getData().add(new XYChart.Data<>(label, range[1]));
            lowerLine.getData().add(new XYChart.Data<>(label, range[0]));
        }

        chart.getData().addAll(upperLine, lowerLine, normalSeries, highSeries);

        Platform.runLater(() -> {
            colorSeries(normalSeries, COLOR_NORMAL);
            colorSeries(highSeries, COLOR_HIGH);
            colorSeries(upperLine, "#FFB74D");
            colorSeries(lowerLine, "#64B5F6");
        });
    }

    private void colorSeries(XYChart.Series<String, Number> series, String color) {
        for (XYChart.Data<String, Number> data : series.getData()) {
            if (data.getNode() != null) {
                data.getNode().setStyle("-fx-background-color: " + color + ", white; "
                        + "-fx-background-radius: 8; -fx-background-insets: 0, 3; "
                        + "-fx-padding: 6;  -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 4, 0, 1, 1);");
            }
        }
        if (series.getNode() != null) {
            series.getNode().lookup(".chart-series-line").setStyle("-fx-stroke: " + color + "; -fx-stroke-width: 2;");
        }
    }

    private void showAlert(String msg) {
        Alert alert = new Alert(Alert.AlertType.WARNING);
        alert.setTitle("提示");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        styleDialogPane(alert.getDialogPane());
        alert.showAndWait();
    }
}
