package net.bov.main.Classes;

import org.bukkit.Location;
import org.bukkit.inventory.ItemStack;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.UUID;

public class Classroom {

    private final String id;
    private String name;
    private UUID teacher;
    private final Set<UUID> subTeachers = new LinkedHashSet<>();
    private Location location;
    private int capacity = 30;
    private int durationSeconds = 0;
    private int intervalSeconds = 0;
    private final TreeSet<ClassTime> times = new TreeSet<>();

    private boolean inSession = false;
    private boolean joinable = false;
    private final Set<UUID> students = new LinkedHashSet<>();
    private final Map<UUID, ItemStack> submissions = new LinkedHashMap<>();
    private final Map<UUID, String> grades = new LinkedHashMap<>();

    public Classroom(String id) {
        this.id = id;
        this.name = id;
    }

    public String getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public UUID getTeacher() {
        return this.teacher;
    }

    public void setTeacher(UUID teacher) {
        this.teacher = teacher;
    }

    public Set<UUID> getSubTeachers() {
        return this.subTeachers;
    }

    public boolean isStaff(UUID uuid) {
        return uuid != null && (uuid.equals(this.teacher) || this.subTeachers.contains(uuid));
    }

    public Location getLocation() {
        return this.location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public int getCapacity() {
        return this.capacity;
    }

    public void setCapacity(int capacity) {
        this.capacity = Math.max(1, capacity);
    }

    public int getDurationSeconds() {
        return this.durationSeconds;
    }

    public void setDurationSeconds(int durationSeconds) {
        this.durationSeconds = Math.max(0, durationSeconds);
    }

    public int getIntervalSeconds() {
        return this.intervalSeconds;
    }

    public void setIntervalSeconds(int intervalSeconds) {
        this.intervalSeconds = Math.max(0, intervalSeconds);
    }

    public TreeSet<ClassTime> getTimes() {
        return this.times;
    }

    public boolean isInSession() {
        return this.inSession;
    }

    public void setInSession(boolean inSession) {
        this.inSession = inSession;
    }

    public boolean isJoinable() {
        return this.joinable;
    }

    public void setJoinable(boolean joinable) {
        this.joinable = joinable;
    }

    public Set<UUID> getStudents() {
        return this.students;
    }

    public boolean isFull() {
        return this.students.size() >= this.capacity;
    }

    public Map<UUID, ItemStack> getSubmissions() {
        return this.submissions;
    }

    public Map<UUID, String> getGrades() {
        return this.grades;
    }

    public void resetSession() {
        this.students.clear();
        this.submissions.clear();
        this.grades.clear();
    }
}