package me.vzhilin.dbtree.ui.autocomplete;

import java.util.List;

public interface SuggestionProvider<T> {
    List<T> suggestions(String text);
}