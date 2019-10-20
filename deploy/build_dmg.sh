#!/bin/bash
JAVA_HOME=~/opt/jdk/jdk-14.jdk/Contents/Home
JAVAFX_JMODS=~/opt/openjfx/javafx-jmods-11.0.2

$JAVA_HOME/bin/jpackage --module-path $JAVAFX_JMODS --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics,java.sql,java.management,java.security.jgss \
--package-type dmg \
--icon src/main/resources/icons/icon.icns \
--mac-package-identifier me.vzhilin.dbtree \
--mac-package-name DBTree \
-d installer -i shade -n DBTree --main-class me.vzhilin.dbtree.MainWindowApp --main-jar db-tree-view-fx.jar
