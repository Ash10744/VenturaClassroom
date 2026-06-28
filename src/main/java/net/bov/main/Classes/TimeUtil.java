package net.bov.main.Classes;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtil {

    private static final Pattern AMPM = Pattern.compile("^(\\d{1,2})(?::(\\d{2}))?(am|pm)$");
    private static final Pattern HHMM = Pattern.compile("^(\\d{1,2}):(\\d{2})$");

    private TimeUtil() {
    }

    public static int normalize(int ticks) {
        int t = ticks % 24000;
        if (t < 0) {
            t += 24000;
        }
        return t;
    }

    public static int parseTime(String input) {
        if (input == null) {
            return -1;
        }
        String s = input.trim().toLowerCase(Locale.ROOT);
        switch (s) {
            case "dawn":
            case "sunrise":
            case "morning":
                return 0;
            case "noon":
            case "midday":
                return 6000;
            case "dusk":
            case "sunset":
            case "evening":
                return 12000;
            case "night":
                return 13000;
            case "midnight":
                return 18000;
            default:
                break;
        }

        Matcher m = AMPM.matcher(s);
        if (m.matches()) {
            int hour = Integer.parseInt(m.group(1));
            int min = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
            if (hour < 1 || hour > 12 || min > 59) {
                return -1;
            }
            if (m.group(3).equals("am")) {
                if (hour == 12) {
                    hour = 0;
                }
            } else if (hour != 12) {
                hour += 12;
            }
            return clockToTicks(hour, min);
        }

        m = HHMM.matcher(s);
        if (m.matches()) {
            int hour = Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            if (hour > 23 || min > 59) {
                return -1;
            }
            return clockToTicks(hour, min);
        }

        try {
            int t = Integer.parseInt(s);
            if (t >= 0 && t < 24000) {
                return t;
            }
        } catch (NumberFormatException ignored) {
        }
        return -1;
    }

    private static int clockToTicks(int hour24, int minute) {
        return normalize(((hour24 - 6) * 1000) + (minute * 1000 / 60));
    }

    public static String ticksToClock(int ticks) {
        int t = normalize(ticks);
        int clockMin = (int) Math.round(360 + t * 0.06) % 1440;
        int hour24 = clockMin / 60;
        int min = clockMin % 60;
        String ap = hour24 < 12 ? "am" : "pm";
        int h12 = hour24 % 12;
        if (h12 == 0) {
            h12 = 12;
        }
        return String.format("%d:%02d%s", h12, min, ap);
    }

    /** Ticks from `now` until the next occurrence of `target` in a 24000-tick day. */
    public static int ticksUntil(int now, int target) {
        int diff = normalize(target) - normalize(now);
        if (diff < 0) {
            diff += 24000;
        }
        return diff;
    }

    /** Rough real-seconds for a tick span (20 in-game ticks per real second). */
    public static String prettyDuration(int ticks) {
        int totalSeconds = ticks / 20;
        int minutes = totalSeconds / 60;
        int seconds = totalSeconds % 60;
        if (minutes <= 0) {
            return seconds + "s";
        }
        return minutes + "m " + seconds + "s";
    }
}