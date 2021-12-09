package dev.gump;

public class WormUtils {
    public static String getLastDot(String text){
        String[] textArr = text.split("\\.");
        return textArr[textArr.length - 1];
    }

    public static String escapeToSql(String value) {
        return value.replace("'", "''");
    }
}
