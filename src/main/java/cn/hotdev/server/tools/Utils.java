package cn.hotdev.server.tools;

import java.io.File;
import java.net.URL;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

/**
 * Created by andy on 5/13/15.
 */
public class Utils {

    public static <T> String join(String delimit, Collection<T> list) {
        if (delimit == null || list == null || list.isEmpty()) {
            return "";
        }

        StringBuilder sb = new StringBuilder();

        boolean first = true;

        for (T t : list) {
            if (!first) sb.append(delimit);
            if (t == null)
                sb.append("");
            else
                sb.append(t.toString());
            first = false;
        }

        return sb.toString();
    }

    public static String replaceLast(String string, String find, String replacement) {
        int lastIndex = string.lastIndexOf(find);
        if (lastIndex < 0) return string;
        String tail = string.substring(lastIndex).replaceFirst(find, replacement);
        return string.substring(0, lastIndex) + tail;
    }

    public static <T> List<T> unique(Iterable<T> list) {
        List<T> result = new ArrayList<T>();
        if (list == null) {
            return result;
        }
        Map<T, Boolean> map = new HashMap<T, Boolean>();
        for (T t : list) {
            if (map.containsKey(t)) continue;
            map.put(t, true);
            result.add(t);
        }
        map.clear();
        return result;
    }

    public static <T> List<String> uniqueString(Iterable<T> list) {
        List<String> result = new ArrayList<String>();
        if (list == null) {
            return result;
        }
        Map<T, Boolean> map = new HashMap<T, Boolean>();
        for (T t : list) {
            if (map.containsKey(t)) continue;
            map.put(t, true);
            result.add(t.toString());
        }
        map.clear();
        return result;
    }

    public static String getProjectPath(Class cls) {
        URL uri = cls.getProtectionDomain().getCodeSource().getLocation();
        String path = uri.getPath();
        if (path.endsWith(".jar")) {
            path = path.substring(0, path.lastIndexOf("/") + 1);
        }
        File file = new File(path);
        return file.getAbsolutePath();
    }

    public static String formatTime(long ts, String format) {
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(ts);
    }

    public static String formatNumber(double num, String format) {
        DecimalFormat df = new DecimalFormat(format);
        return df.format(num);
    }

    public static Date getZeroTime(long timestamp) {
        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");

        Calendar cal = Calendar.getInstance();
        if (timestamp > 0)
            cal.setTime(new Date(timestamp));
        int year = cal.get(Calendar.YEAR);
        int month = cal.get(Calendar.MONTH) + 1;
        int day = cal.get(Calendar.DAY_OF_MONTH);

        try {
            return sdf.parse(year + "-" + month + "-" + day);
        } catch (ParseException e) {
            return new Date();
        }
    }

    public static long getYesterdayTime() {
        return getYesterdayTime(0);
    }

    public static long getTodayTime() {
        return getTodayTime(0);
    }

    public static long getTomorrowTime() {
        return getTomorrowTime(0);
    }

    public static long getAfterTomorrowTime() {
        return getAfterTomorrowTime(0);
    }

    public static long getYesterdayTime(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getZeroTime(timestamp));
        cal.add(Calendar.DAY_OF_YEAR, -1);
        return cal.getTimeInMillis();
    }

    public static long getTodayTime(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getZeroTime(timestamp));
        return cal.getTimeInMillis();
    }

    public static long getTomorrowTime(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getZeroTime(timestamp));
        cal.add(Calendar.DAY_OF_YEAR, 1);
        return cal.getTimeInMillis();
    }

    public static long getAfterTomorrowTime(long timestamp) {
        Calendar cal = Calendar.getInstance();
        cal.setTime(getZeroTime(timestamp));
        cal.add(Calendar.DAY_OF_YEAR, 2);
        return cal.getTimeInMillis();
    }

    public static <T> String makeString(T[] list, int limit) {
        if (list == null || list.length <= 0)
            return "";

        int cnt = 0, total = list.length;

        limit = (limit <= 0 || total < limit) ? total : limit;

        StringBuilder sb = new StringBuilder();
        for (T t : list) {
            sb.append(t.toString());
            ++cnt;
            if (cnt >= limit)
                break;
        }
        return sb.toString();
    }
}
