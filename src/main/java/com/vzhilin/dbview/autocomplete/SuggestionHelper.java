package com.vzhilin.dbview.autocomplete;

public class SuggestionHelper {
    private final String text;
    private final int caret;

    public SuggestionHelper(String text, int caret) {
        this.text = text;
        this.caret = caret;
    }

    public int beginClause() {
        String beginning = text.substring(0, caret);
        int sp = beginning.lastIndexOf(' ');
        if (sp > 0) {
            return sp + 1;
        } else {
            return 0;
        }
    }

    public int endClause() {
        return caret;
    }

    public int beginWord() {
        String beginning = text.substring(0, caret);
        int sp = Math.max(beginning.lastIndexOf(' '), beginning.lastIndexOf('.'));
        if (sp > 0) {
            return sp + 1;
        } else {
            return 0;
        }
    }

    public int endWord() {
        return caret;
    }

    public String word() {
        return text.substring(beginClause(), endClause());
    }

    public String select(String replacement) {
        return text.substring(0, beginWord()) + replacement + text.substring(endWord());
    }
}
