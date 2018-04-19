package com.vzhilin.dbview;

import com.google.common.collect.Lists;
import com.vzhilin.dbview.autocomplete.AutoCompletion;
import com.vzhilin.dbview.autocomplete.SuggestionProvider;
import com.vzhilin.dbview.db.DbContext;
import com.vzhilin.dbview.db.schema.Schema;
import com.vzhilin.dbview.db.schema.Table;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.TextField;

import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


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
        new AutoCompletion(new DbSuggestionProvider(ctx.getSchema())).bind(autocompleteField);
    }

    private static class DbSuggestionProvider implements SuggestionProvider {
        private final Schema schema;
        private final Table root;

        public DbSuggestionProvider(Schema schema) {
            this.schema = schema;
            this.root = schema.getTable("OESO_KCA");
        }

        @Override
        public List<String> suggestions(String text) {
            Table current = root;
            if (text.contains(".")) {
                String[] split = text.split("\\.");
                for (int i = 0; i < split.length; i++) {
                    Map<String, Table> rs = root.getRelations();
                    String name = split[i];
                    if (!rs.containsKey(name)) {
                        break;
                    }

                    current = rs.get(name);
                }

                text = text.substring(text.lastIndexOf('.') + 1);
            }


            final String finalText = text;
            return current.getColumns().stream().filter(t -> t.startsWith(finalText)).collect(Collectors.toList());
        }
    }
}
