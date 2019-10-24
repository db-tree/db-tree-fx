package me.vzhilin.dbtree.ui;

import javafx.fxml.FXML;
import javafx.scene.control.TextArea;

import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

public final class AboutController {
    @FXML
    private TextArea aboutText = new TextArea();

    @FXML
    private void initialize() throws IOException {
        Properties props = new Properties();
        props.load(new InputStreamReader(getClass().getResourceAsStream("/version/version.txt")));

        String version = props.getProperty("version");
        String buildDate = props.getProperty("build.date");


        String text = "author: Vladimir Zhilin\n" +
                "version: $version\n" +
                "build date: $date\n" +
                "github: https://github.com/vzhn/db-tree-fx";

        text = text.replace("$date", buildDate).replace("$version", version);
        aboutText.setText(text);
    }
}
