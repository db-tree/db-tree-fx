#!/bin/bash
JAVA_HOME=~/opt/jdk-13.0.1/
JPACKAGE_HOME=~/opt/jdk-14
JAVAFX_JMODS=~/opt/javafx-jmods-13.0.1

if [ ! -d "target/jre" ]
then
	$JAVA_HOME/bin/jlink \
	--module-path $JAVAFX_JMODS \
	--add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics,java.sql,java.management,java.security.jgss,jdk.security.auth \
	--output target/jre
fi

$JPACKAGE_HOME/bin/jpackage \
	--copyright DBTree \
	--runtime-image target/jre \
	--icon src/main/resources/icons/icon.png \
	--vendor "Vladimir Zhilin" \
	--app-version 0.0.1a \
	--type rpm \
	--linux-package-name DBTree \
	--linux-app-category Database \
	--linux-shortcut \
	-d installer -i target/shade -n DBTree --main-class me.vzhilin.dbtree.MainWindowApp --main-jar db-tree-view-fx.jar
