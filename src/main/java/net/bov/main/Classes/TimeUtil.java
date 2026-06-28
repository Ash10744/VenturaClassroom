package net.bov.main.Classes;

import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalTime;
import java.util.LinkedHashSet;
import java.util.Locale;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public final class TimeUtil {

    private static final Pattern AMPM = Pattern.compile("^(\\d{1,2})(?::(\\d{2}))?(am|pm)$");
    private static final Pattern HHMM = Pattern.compile("^(\\d{1,2}):(\\d{2})$");

    private TimeUtil() {
    }

    public static LocalTime parseClock(String input) {
        if (input == null) {
            return null;
        }
        String s = input.trim().toLowerCase(Locale.ROOT);
        if (s.equals("noon") || s.equals("midday")) {
            return LocalTime.of(12, 0);
        }
        if (s.equals("midnight")) {
            return LocalTime.of(0, 0);
        }
        Matcher m = AMPM.matcher(s);
        if (m.matches()) {
            int hour = Integer.parseInt(m.group(1));
            int min = m.group(2) == null ? 0 : Integer.parseInt(m.group(2));
            if (hour < 1 || hour > 12 || min > 59) {
                return null;
            }
            if (m.group(3).equals("am")) {
                if (hour == 12) {
                    hour = 0;
                }
            } else if (hour != 12) {
                hour += 12;
            }
            return LocalTime.of(hour, min);
        }
        m = HHMM.matcher(s);
        if (m.matches()) {
            int hour = Integer.parseInt(m.group(1));
            int min = Integer.parseInt(m.group(2));
            if (hour > 23 || min > 59) {
                return null;
            }
            return LocalTime.of(hour, min);
        }
        return null;
    }

    public static String formatClock(LocalTime time) {
        int h = time.getHour();
        int min = time.getMinute();
        String ap = h < 12 ? "am" : "pm";
        int h12 = h % 12;
        if (h12 == 0) {
            h12 = 12;
        }
        return String.format("%d:%02d%s", h12, min, ap);
    }

    public static DayOfWeek parseDay(String input) {
        if (input == null) {
            return null;
        }
        switch (input.trim().toUpperCase(Locale.ROOT)) {
            case "MON": case "MONDAY": return DayOfWeek.MONDAY;
            case "TUE": case "TUES": case "TUESDAY": return DayOfWeek.TUESDAY;
            case "WED": case "WEDS": case "WEDNESDAY": return DayOfWeek.WEDNESDAY;
            case "THU": case "THUR": case "THURS": case "THURSDAY": return DayOfWeek.THURSDAY;
            case "FRI": case "FRIDAY": return DayOfWeek.FRIDAY;
            case "SAT": case "SATURDAY": return DayOfWeek.SATURDAY;
            case "SUN": case "SUNDAY": return DayOfWeek.SUNDAY;
            default: return null;
        }
    }

    public static Set<DayOfWeek> parseDays(String input) {
        Set<DayOfWeek> out = new LinkedHashSet<>();
        if (input == null) {
            return out;
        }
        switch (input.trim().toLowerCase(Locale.ROOT)) {
            case "daily":
            case "everyday":
            case "all":
                for (DayOfWeek d : DayOfWeek.values()) {
                    out.add(d);
                }
                return out;
            case "weekdays":
            case "weekday":
                out.add(DayOfWeek.MONDAY);
                out.add(DayOfWeek.TUESDAY);
                out.add(DayOfWeek.WEDNESDAY);
                out.add(DayOfWeek.THURSDAY);
                out.add(DayOfWeek.FRIDAY);
                return out;
            case "weekend":
            case "weekends":
                out.add(DayOfWeek.SATURDAY);
                out.add(DayOfWeek.SUNDAY);
                return out;
            default:
                break;
        }
        DayOfWeek single = parseDay(input);
        if (single != null) {
            out.add(single);
        }
        return out;
    }

    public static String dayShort(DayOfWeek day) {
        switch (day) {
            case MONDAY: return "Mon";
            case TUESDAY: return "Tue";
            case WEDNESDAY: return "Wed";
            case THURSDAY: return "Thu";
            case FRIDAY: return "Fri";
            case SATURDAY: return "Sat";
            case SUNDAY: return "Sun";
            default: return day.name();
        }
    }

    public static String prettyUntil(Duration duration) {
        long total = duration.toMinutes();
        if (total <= 0) {
            return "now";
        }
        long days = total / 1440;
        long rem = total % 1440;
        long hours = rem / 60;
        long mins = rem % 60;
        StringBuilder sb = new StringBuilder();
        if (days > 0) {
            sb.append(days).append("d ");
        }
        if (hours > 0) {
            sb.append(hours).append("h ");
        }
        if (mins > 0 || sb.length() == 0) {
            sb.append(mins).append("m");
        }
        return sb.toString().trim();
    }
}