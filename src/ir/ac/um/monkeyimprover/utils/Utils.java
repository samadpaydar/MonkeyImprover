package ir.ac.um.monkeyimprover.utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Samad Paydar
 */
public class Utils {
    public static String capitalize(String text) {
        if (text != null && !text.isEmpty()) {
            text = "" + text.toUpperCase().charAt(0) + text.toLowerCase().substring(1);
        }
        return text;
    }

    public static String getTimestamp() {
        String pattern = "yyyy_MM_dd_HH_mm_ss";
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat(pattern);
        return simpleDateFormat.format(new Date());
    }
}
