package me.vzhilin.dbtree.ui.util;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public final class ToStringConverter {
    private final DateFormat dateFormat;
    private final DateFormat dateTimeFormat;

    public ToStringConverter() {
        dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        dateFormat = new SimpleDateFormat("yyyy-MM-dd");
    }

    public String toString(Object value) {
        if (value instanceof Date) {
            Date date = (Date) value;
            Calendar c = Calendar.getInstance();
            c.setTime(date);

            if (c.get(Calendar.SECOND) == 0 &&
                c.get(Calendar.MINUTE) == 0 &&
                c.get(Calendar.HOUR_OF_DAY) == 0) {

                synchronized (this) {
                    return dateFormat.format(date);
                }
            } else {
                synchronized (this) {
                    return dateTimeFormat.format(date);
                }
            }
        } else {
            return String.valueOf(value);
        }
    }
}
