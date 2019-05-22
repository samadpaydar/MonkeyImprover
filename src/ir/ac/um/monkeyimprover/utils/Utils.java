package ir.ac.um.monkeyimprover.utils;

import com.intellij.execution.ui.ConsoleView;
import com.intellij.execution.ui.ConsoleViewContentType;
import ir.ac.um.monkeyimprover.analysis.utils.AnalysisUtils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;

/**
 * @author Samad Paydar
 */
public class Utils {
    private static ConsoleView consoleView;

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

    public static ConsoleView getConsoleView() {
        return Utils.consoleView;
    }

    public static void setConsoleView(ConsoleView consoleView) {
        Utils.consoleView = consoleView;
    }

    public static void showMessage(String message) {
        if (consoleView != null) {
            consoleView.print(String.format("%s%n", message),
                    ConsoleViewContentType.NORMAL_OUTPUT);
        }
    }

}
