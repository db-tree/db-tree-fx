#!/bin/bash
JAVA_HOME=~/opt/jdk-13.0.1.jdk/Contents/Home
JPACKAGE_HOME=~/opt/jdk/jdk-14.jdk/Contents/Home
JAVAFX_JMODS=~/opt/javafx-jmods-13.0.1

if [ ! -d "target/jre" ]
then
	$JAVA_HOME/bin/jlink \
	--module-path $JAVAFX_JMODS \
	--add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics,java.sql,java.management,java.security.jgss \
	--output target/jre

fi

$JPACKAGE_HOME/bin/jpackage \
	--copyright DBTree \
	--runtime-image target/jre \
	--icon src/main/resources/icons/icon.icns \
	--vendor "Vladimir Zhilin" \
	--app-version 0.0.2 \
	--package-type dmg \
	--mac-package-identifier me.vzhilin.dbtree \
	--mac-package-name DBTree \
	-d installer -i target/shade -n db-tree --main-class me.vzhilin.dbtree.MainWindowApp --main-jar db-tree-view-fx.jar
