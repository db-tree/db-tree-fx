set JPACKAGE_HOME=D:\openjdk\jdk-14
set JAVA_HOME=D:\opt\java\jdk-13.0.1
set JAVAFX_JMODS=D:\opt\javafx\javafx-jmods-13.0.1
set MVN=C:\opt\apache-maven-3.6.2
set PATH=%PATH%;D:\opt\wix

call %MVN%\bin\mvn.cmd clean package -Dmaven.test.skip=true

IF NOT EXIST target\jre (
    %JAVA_HOME%\bin\jlink.exe ^
        --module-path %JAVAFX_JMODS% ^
        --add-modules javafx.base,javafx.controls,javafx.fxml,javafx.graphics,java.sql,java.management,java.security.jgss ^
        --output target\jre
)

%JPACKAGE_HOME%\bin\jpackage ^
--copyright DBTree ^
--runtime-image target\jre --package-type msi ^
--icon src\main\resources\icons\icon.ico ^
--win-dir-chooser ^
--win-shortcut ^
--win-menu ^
--vendor "Vladimir Zhilin" ^
--app-version 0.0.1-SNAPSHOT ^
-i target\shade -n DBTree --main-class me.vzhilin.dbtree.MainWindowApp --main-jar db-tree-view-fx.jar
