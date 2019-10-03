package com.vzhilin.dbview.ui;

import com.vzhilin.dbview.ui.autocomplete.AutoCompletion;
import com.vzhilin.dbview.ui.autocomplete.row.RowSuggestionProvider;
import com.vzhilin.dbview.ui.conf.ConnectionSettings;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.QueryContext;
import com.vzhilin.dbview.db.data.Row;
import com.vzhilin.dbview.db.schema.Table;
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
        Table kca = ctx.getSchema().getTable("OESO_KCA");
        QueryContext qc = new QueryContext(ctx, new ConnectionSettings());
        RowSuggestionProvider kcaProvider = new RowSuggestionProvider(new Row(qc, kca, 2190));
        new AutoCompletion(kcaProvider, autocompleteField);
    }
}
