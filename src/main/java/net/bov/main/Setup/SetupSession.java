package net.bov.main.Setup;

import net.bov.main.Classes.ClassTime;
import org.bukkit.Location;

import java.time.DayOfWeek;
import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

public class SetupSession {

    public enum Step { NAME, TEACHER, WARP, DAY, TIME, DURATION }

    public Step step = Step.NAME;
    public String awaitingChat = "name";
    public String name;
    public String id;
    public UUID teacher;
    public Location location;
    public final List<ClassTime> times = new ArrayList<>();
    public final Set<DayOfWeek> pendingDays = new LinkedHashSet<>();
    public int durationSeconds = 0;
}