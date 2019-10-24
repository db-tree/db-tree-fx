package me.vzhilin.dbtree.ui.autocomplete;

public final class SuggestionHelper {
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

    public Suggestion word() {
        if (isWord()) {
            String word = text.substring(beginClause(), endClause());
            return new Suggestion(true, word);
        } else {
            return new Suggestion(false, "");
        }
    }

    private boolean isWord() {
        int k = 0;
        for (int i = 0; i < beginClause(); i++) {
            if (text.charAt(i) == '\'') {
                ++k;
            }
        }
        return k % 2 == 0;
    }

    public Select select(String replacement) {
        String result = text.substring(0, beginWord()) + replacement + text.substring(endWord());
        int cursorPosition = beginWord() + replacement.length();
        return new Select(result, cursorPosition);
    }

    public final static class Select {
        private final String result;
        private final int cursorPosition;

        public Select(String result, int cursorPosition) {
            this.result = result;
            this.cursorPosition = cursorPosition;
        }

        public String getResult() {
            return result;
        }

        public int getCursorPosition() {
            return cursorPosition;
        }
    }

    public final static class Suggestion {
        private final boolean isWord;
        private final String word;

        public Suggestion(boolean isWord, String word) {
            this.isWord = isWord;
            this.word = word;
        }

        public boolean isWord() {
            return isWord;
        }

        public String getWord() {
            return word;
        }
    }
}
