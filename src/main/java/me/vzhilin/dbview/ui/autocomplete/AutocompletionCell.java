package me.vzhilin.dbview.ui.autocomplete;

public final class AutocompletionCell {
    private final String column;
    private final boolean isPk;
    private final boolean isFk;
    private final Object value;

    public AutocompletionCell(String column, boolean isPk, boolean isFk, Object value) {
        this.column = column;
        this.isPk = isPk;
        this.isFk = isFk;
        this.value = value;
    }

    public String getColumn() {
        return column;
    }

    public boolean isPk() {
        return isPk;
    }

    public boolean isFk() {
        return isFk;
    }

    public Object getValue() {
        return value;
    }
}
