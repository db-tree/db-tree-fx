package com.vzhilin.dbview.autocomplete;

import java.util.List;

public interface SuggestionProvider<T> {
    List<T> suggestions(String text);
}