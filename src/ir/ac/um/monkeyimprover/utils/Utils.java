package ir.ac.um.monkeyimprover.utils;

public class Utils {
    public static String capitalize(String text) {
        if(text!= null && !text.isEmpty()) {
            text = "" + text.toUpperCase().charAt(0) + text.toLowerCase().substring(1);
        }
        return text;
    }
}
