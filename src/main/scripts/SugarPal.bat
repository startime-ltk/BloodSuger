@echo off
set DIR=%~dp0

rem 收集 javafx 目录下所有 jar 作为 module-path
set JFX=
for %%f in ("%DIR%javafx\*.jar") do (
    if "!JFX!"=="" (set JFX=%%f) else (set JFX=!JFX!;%%f)
)
setlocal enabledelayedexpansion
set JFX_LIST=
for %%f in ("%DIR%javafx\*.jar") do (
    if "!JFX_LIST!"=="" (set JFX_LIST=%%f) else (set JFX_LIST=!JFX_LIST!;%%f)
)

java --module-path "!JFX_LIST!" --add-modules javafx.controls,javafx.fxml -jar "%DIR%SugarPal.jar"
pause
