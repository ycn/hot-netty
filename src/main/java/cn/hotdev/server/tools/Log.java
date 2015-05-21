package cn.hotdev.server.tools;


import org.slf4j.Logger;

/**
 * Created by andy on 5/19/15.
 */
public class Log {
    public static void debug(Logger log, String format, Object... arguments) {
    }

    public static void info(Logger log, String format, Object... arguments) {
    }

    public static void err(Logger log, String format, Object... arguments) {
    }


    private static String createLog(String mark, long startTime, Object... logs) {
        long currentTime = System.currentTimeMillis();
        StringBuilder sb = new StringBuilder();
        sb.append("\t");
        sb.append(Utils.formatTime(currentTime, "yyyy-MM-dd HH:mm:ss")).append("\t");
        sb.append(mark).append("\t");
        sb.append(String.valueOf(currentTime - startTime)).append("\t");
        if (logs != null && logs.length > 0) {
            for (Object log : logs) {
                sb.append(log != null ? String.valueOf(log) : "").append("\t");
            }
        }
        return sb.toString();
    }
}
