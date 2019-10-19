module hellofx {
    requires java.sql;
    requires javafx.controls;
    requires javafx.fxml;
    requires dbrow;
    requires commons.dbcp2;
    requires com.google.common;
    requires commons.dbutils;
    requires log4j;
    requires antlr4.runtime;
    requires com.google.gson;

    requires java.management;
    requires fx.gson;

    opens me.vzhilin.dbtree to javafx.fxml;
    opens me.vzhilin.dbtree.ui to javafx.fxml;
    opens me.vzhilin.dbtree.ui.settings to javafx.fxml;
    opens me.vzhilin.dbtree.ui.conf to javafx.base,com.google.gson;
    exports me.vzhilin.dbtree;
}
