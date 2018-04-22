package com.vzhilin.dbview;

import com.vzhilin.dbview.autocomplete.AutoCompletion;
import com.vzhilin.dbview.autocomplete.row.RowSuggestionProvider;
import com.vzhilin.dbview.db.DbContext;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;


public class AutocompleteController {
    private static final double TITLE_HEIGHT = 28;
    @FXML
    private TextField autocompleteField;

    private Scene scene;
    private DbContext ctx;

    @FXML
    private void initialize() {

    }


    public void setScene(Scene scene) {
        this.scene = scene;
    }


    public void setContext(DbContext ctx) {
        this.ctx = ctx;
        RowSuggestionProvider kcaProvider = new RowSuggestionProvider(/* */null);
        new AutoCompletion(kcaProvider).bind(autocompleteField);
    }
}
