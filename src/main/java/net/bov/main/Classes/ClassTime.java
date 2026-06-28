package net.bov.main.Classes;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.Locale;

public class ClassTime implements Comparable<ClassTime> {

    private final DayOfWeek day;
    private final LocalTime time;

    public ClassTime(DayOfWeek day, LocalTime time) {
        this.day = day;
        this.time = time;
    }

    public DayOfWeek getDay() {
        return this.day;
    }

    public LocalTime getTime() {
        return this.time;
    }

    public String serialize() {
        return this.day.name() + " " + String.format("%02d:%02d", this.time.getHour(), this.time.getMinute());
    }

    public static ClassTime deserialize(String raw) {
        if (raw == null) {
            return null;
        }
        try {
            String[] parts = raw.trim().split("\\s+");
            if (parts.length < 2) {
                return null;
            }
            DayOfWeek d = DayOfWeek.valueOf(parts[0].toUpperCase(Locale.ROOT));
            String[] hm = parts[1].split(":");
            LocalTime t = LocalTime.of(Integer.parseInt(hm[0]), Integer.parseInt(hm[1]));
            return new ClassTime(d, t);
        } catch (Exception ex) {
            return null;
        }
    }

    @Override
    public int compareTo(ClassTime o) {
        int c = Integer.compare(this.day.getValue(), o.day.getValue());
        return c != 0 ? c : this.time.compareTo(o.time);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ClassTime)) {
            return false;
        }
        ClassTime ct = (ClassTime) o;
        return this.day == ct.day && this.time.equals(ct.time);
    }

    @Override
    public int hashCode() {
        return this.day.hashCode() * 31 + this.time.hashCode();
    }
}