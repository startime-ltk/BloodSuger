#!/bin/bash
DIR="$(cd "$(dirname "$0")" && pwd)"
JFX=$(echo "$DIR"/javafx/*.jar | tr ' ' ':')
java --module-path "$JFX" --add-modules javafx.controls,javafx.fxml -jar "$DIR/BloodSugar.jar"
read -p "按回车键退出..."
