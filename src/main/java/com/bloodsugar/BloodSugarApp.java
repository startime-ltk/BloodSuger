package com.bloodsugar;

import com.bloodsugar.ui.MainUI;
import javafx.application.Application;
import javafx.scene.Scene;
import javafx.scene.image.Image;
import javafx.stage.Stage;

/**
 * 血糖记录系统 应用入口
 */
public class BloodSugarApp extends Application {

    @Override
    public void start(Stage stage) {
        MainUI ui = new MainUI();
        Scene scene = ui.createScene(stage);
        stage.setTitle("血糖记录系统");
        stage.getIcons().add(new Image(getClass().getResourceAsStream("/icon.png")));
        stage.setScene(scene);
        stage.setMinWidth(900);
        stage.setMinHeight(550);
        stage.show();
    }

    public static void main(String[] args) {
        launch(args);
    }
}
