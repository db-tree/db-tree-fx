package com.vzhilin.dbview.autocomplete;

import java.util.List;

public interface SuggestionProvider {
    List<String> suggestions(String text);
}