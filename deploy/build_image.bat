set JAVA_HOME=D:\openjdk\jdk-14
set JAVAFX_JMODS=D:\opt\javafx\javafx-jmods-11.0.2

@REM %JAVA_HOME%\bin\jpackage --module-path %JAVAFX_JMODS% --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics,java.sql,java.management,java.security.jgss  --package-type app-image -d destdir -i shade -n name --main-class me.vzhilin.dbtree.MainWindowApp --main-jar db-tree-view-fx.jar

%JAVA_HOME%\bin\jpackage --module-path %JAVAFX_JMODS% --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics,java.sql,java.management,java.security.jgss --package-type exe ^
--icon ..\src\main\resources\icons\icon.ico ^
--win-dir-chooser ^
--win-per-user-install ^
--win-shortcut ^
--win-menu ^
-d ..\installer -i ..\shade -n DBTree --main-class me.vzhilin.dbtree.MainWindowApp --main-jar db-tree-view-fx.jar
