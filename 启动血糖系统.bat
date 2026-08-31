@echo off
cd /d "F:\Java\work\bloodsugar"
start "" javaw --module-path target\javafx --add-modules javafx.controls,javafx.fxml -cp target\SugarPal.jar com.bloodsugar.BloodSugarApp
