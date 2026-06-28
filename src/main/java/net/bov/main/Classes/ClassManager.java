package net.bov.main.Classes;

import net.bov.main.Libs.Libs;
import net.bov.main.VenturaClassroom;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitTask;

import java.io.File;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClassManager {

    private static final long CHECK_INTERVAL = 100L;

    private final VenturaClassroom plugin;
    private final File file;
    private FileConfiguration config;

    private final Map<String, Classroom> classes = new LinkedHashMap<>();
    private final Map<String, String> lastTrigger = new HashMap<>();
    private final Map<String, BukkitTask> endTasks = new HashMap<>();

    private BukkitTask task;

    public ClassManager(VenturaClassroom plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "classes.yml");
    }

    public void load() {
        this.classes.clear();
        this.lastTrigger.clear();
        if (!this.plugin.getDataFolder().exists()) {
            this.plugin.getDataFolder().mkdirs();
        }
        this.config = YamlConfiguration.loadConfiguration(this.file);

        ConfigurationSection root = this.config.getConfigurationSection("classes");
        if (root != null) {
            for (String id : root.getKeys(false)) {
                String base = "classes." + id;
                Classroom room = new Classroom(id);
                room.setName(this.config.getString(base + ".name", id));
                room.setCapacity(this.config.getInt(base + ".capacity", 30));
                room.setDurationSeconds(this.config.getInt(base + ".duration", 0));

                String teacher = this.config.getString(base + ".teacher", null);
                if (teacher != null && !teacher.isEmpty()) {
                    try {
                        room.setTeacher(UUID.fromString(teacher));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                for (String sub : this.config.getStringList(base + ".subTeachers")) {
                    try {
                        room.getSubTeachers().add(UUID.fromString(sub));
                    } catch (IllegalArgumentException ignored) {
                    }
                }
                for (String raw : this.config.getStringList(base + ".times")) {
                    ClassTime ct = ClassTime.deserialize(raw);
                    if (ct != null) {
                        room.getTimes().add(ct);
                    }
                }
                room.setLocation(readLoc(base + ".location"));
                this.classes.put(id.toLowerCase(), room);
            }
        }
        this.plugin.getLogger().info("Loaded " + this.classes.size() + " classroom(s).");
    }

    public void save() {
        FileConfiguration out = new YamlConfiguration();
        for (Classroom room : this.classes.values()) {
            String base = "classes." + room.getId();
            out.set(base + ".name", room.getName());
            out.set(base + ".capacity", room.getCapacity());
            out.set(base + ".duration", room.getDurationSeconds());
            out.set(base + ".teacher", room.getTeacher() == null ? null : room.getTeacher().toString());
            List<String> subs = new ArrayList<>();
            for (UUID u : room.getSubTeachers()) {
                subs.add(u.toString());
            }
            out.set(base + ".subTeachers", subs);
            List<String> times = new ArrayList<>();
            for (ClassTime ct : room.getTimes()) {
                times.add(ct.serialize());
            }
            out.set(base + ".times", times);
            writeLoc(out, base + ".location", room.getLocation());
        }
        try {
            out.save(this.file);
        } catch (IOException ex) {
            this.plugin.getLogger().severe("Failed to save classes.yml: " + ex.getMessage());
        }
    }

    private void writeLoc(FileConfiguration c, String path, Location l) {
        if (l == null || l.getWorld() == null) {
            return;
        }
        c.set(path + ".world", l.getWorld().getName());
        c.set(path + ".x", l.getX());
        c.set(path + ".y", l.getY());
        c.set(path + ".z", l.getZ());
        c.set(path + ".yaw", (double) l.getYaw());
        c.set(path + ".pitch", (double) l.getPitch());
    }

    private Location readLoc(String path) {
        if (!this.config.contains(path + ".world")) {
            return null;
        }
        World world = Bukkit.getWorld(this.config.getString(path + ".world"));
        if (world == null) {
            return null;
        }
        return new Location(world,
                this.config.getDouble(path + ".x"),
                this.config.getDouble(path + ".y"),
                this.config.getDouble(path + ".z"),
                (float) this.config.getDouble(path + ".yaw"),
                (float) this.config.getDouble(path + ".pitch"));
    }

    public Classroom get(String id) {
        return id == null ? null : this.classes.get(id.toLowerCase());
    }

    public Classroom create(String id) {
        Classroom room = new Classroom(id);
        this.classes.put(id.toLowerCase(), room);
        save();
        return room;
    }

    public void delete(String id) {
        Classroom removed = this.classes.remove(id.toLowerCase());
        if (removed != null) {
            this.lastTrigger.remove(id.toLowerCase());
            save();
        }
    }

    public Collection<Classroom> all() {
        return this.classes.values();
    }

    public void start() {
        if (this.task != null) {
            this.task.cancel();
        }
        this.task = Bukkit.getScheduler().runTaskTimer(this.plugin, this::tick, CHECK_INTERVAL, CHECK_INTERVAL);
    }

    public void stop() {
        if (this.task != null) {
            this.task.cancel();
            this.task = null;
        }
    }

    private void tick() {
        LocalDateTime now = LocalDateTime.now();
        DayOfWeek today = now.getDayOfWeek();
        int hour = now.getHour();
        int minute = now.getMinute();

        for (Classroom room : this.classes.values()) {
            if (room.getLocation() == null || room.getLocation().getWorld() == null) {
                continue;
            }
            for (ClassTime ct : room.getTimes()) {
                if (ct.getDay() == today && ct.getTime().getHour() == hour && ct.getTime().getMinute() == minute) {
                    String key = ct.serialize();
                    if (key.equals(this.lastTrigger.get(room.getId().toLowerCase()))) {
                        break;
                    }
                    this.lastTrigger.put(room.getId().toLowerCase(), key);
                    if (!room.isInSession()) {
                        startClass(room);
                    }
                    break;
                }
            }
        }
    }

    public void startClass(Classroom room) {
        room.resetSession();
        room.setInSession(true);
        room.setJoinable(true);
        Libs.broadcastClassStart(room);
        scheduleAutoEnd(room);
    }

    public void beginClass(Classroom room) {
        room.setJoinable(false);
        if (room.getLocation() != null) {
            for (UUID id : room.getStudents()) {
                Player p = Bukkit.getPlayer(id);
                if (p != null && p.isOnline()) {
                    p.teleport(room.getLocation());
                }
            }
        }
    }

    private void scheduleAutoEnd(Classroom room) {
        BukkitTask existing = this.endTasks.remove(room.getId().toLowerCase());
        if (existing != null) {
            existing.cancel();
        }
        int dur = room.getDurationSeconds();
        if (dur > 0) {
            BukkitTask t = Bukkit.getScheduler().runTaskLater(this.plugin, () -> endClass(room), dur * 20L);
            this.endTasks.put(room.getId().toLowerCase(), t);
        }
    }

    public void endClass(Classroom room) {
        room.setInSession(false);
        room.setJoinable(false);
        BukkitTask t = this.endTasks.remove(room.getId().toLowerCase());
        if (t != null) {
            t.cancel();
        }
    }

    public String join(Player player, Classroom room) {
        if (!room.isInSession()) {
            return "no-session";
        }
        if (room.getStudents().contains(player.getUniqueId())) {
            return "already";
        }
        if (!room.isJoinable()) {
            return "locked";
        }
        if (room.isFull()) {
            return "full";
        }
        room.getStudents().add(player.getUniqueId());
        if (room.getLocation() != null && this.plugin.getConfig().getBoolean("teleport-on-join", false)) {
            player.teleport(room.getLocation());
        }
        return "ok";
    }

    public boolean leave(Player player, Classroom room) {
        return room.getStudents().remove(player.getUniqueId());
    }

    public Classroom getNextClass() {
        LocalDateTime now = LocalDateTime.now();
        Classroom best = null;
        Duration bestWait = null;
        for (Classroom room : this.classes.values()) {
            Duration wait = waitFrom(room, now);
            if (wait != null && (bestWait == null || wait.compareTo(bestWait) < 0)) {
                bestWait = wait;
                best = room;
            }
        }
        return best;
    }

    public Duration waitUntilNext(Classroom room) {
        return waitFrom(room, LocalDateTime.now());
    }

    private Duration waitFrom(Classroom room, LocalDateTime now) {
        Duration best = null;
        for (ClassTime ct : room.getTimes()) {
            int dayDiff = (ct.getDay().getValue() - now.getDayOfWeek().getValue() + 7) % 7;
            LocalDateTime candidate = now.toLocalDate().plusDays(dayDiff).atTime(ct.getTime());
            if (candidate.isBefore(now)) {
                candidate = candidate.plusDays(7);
            }
            Duration d = Duration.between(now, candidate);
            if (best == null || d.compareTo(best) < 0) {
                best = d;
            }
        }
        return best;
    }

    public int giveItemToStudents(Classroom room, ItemStack item) {
        int count = 0;
        for (UUID id : room.getStudents()) {
            Player p = Bukkit.getPlayer(id);
            if (p != null && p.isOnline()) {
                p.getInventory().addItem(item.clone());
                count++;
            }
        }
        return count;
    }

    public List<String> gradeKeys() {
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection("grades");
        if (sec == null) {
            return new ArrayList<>();
        }
        return new ArrayList<>(sec.getKeys(false));
    }

    public String resolveGrade(String input) {
        if (input == null) {
            return null;
        }
        ConfigurationSection sec = this.plugin.getConfig().getConfigurationSection("grades");
        if (sec == null) {
            return null;
        }
        for (String key : sec.getKeys(false)) {
            if (key.equalsIgnoreCase(input)) {
                return key;
            }
        }
        return null;
    }

    public boolean applyGrade(Player student, Classroom room, String grade) {
        FileConfiguration cfg = this.plugin.getConfig();
        String base = "grades." + grade;
        if (!cfg.contains(base)) {
            return false;
        }
        double money = cfg.getDouble(base + ".money", 0);
        int xp = cfg.getInt(base + ".xp", 0);
        String message = cfg.getString(base + ".message", "");
        List<String> commands = cfg.getStringList(base + ".commands");

        if (money > 0 && cfg.getBoolean("enable-economy", true)) {
            net.bov.main.Integrations.EconomyHook eco = this.plugin.getEconomyHook();
            if (eco != null && eco.available()) {
                eco.deposit(student, money);
            }
        }
        if (xp > 0) {
            student.giveExp(xp);
        }
        String rewardSummary = "";
        if (money > 0 && cfg.getBoolean("enable-economy", true)) {
            rewardSummary = "&a" + money + " coins";
        }
        if (xp > 0) {
            rewardSummary = rewardSummary.isEmpty() ? ("&a" + xp + " XP") : (rewardSummary + "&7, &a" + xp + " XP");
        }
        String suffix = rewardSummary.isEmpty() ? "" : (" &7[" + rewardSummary + "&7]");
        student.sendMessage(Libs.format("&8[&6VClassroom&8] &aYou were graded &6" + grade
                + " &ain &e" + room.getName() + suffix + "&a."));
        if (message != null && !message.isEmpty()) {
            student.sendMessage(Libs.format(fill(message, student, room, grade, money, xp)));
        }
        for (String cmd : commands) {
            if (cmd != null && !cmd.trim().isEmpty()) {
                Bukkit.dispatchCommand(Bukkit.getConsoleSender(), fill(cmd, student, room, grade, money, xp));
            }
        }
        return true;
    }

    private String fill(String s, Player student, Classroom room, String grade, double money, int xp) {
        return s.replace("%player%", student.getName())
                .replace("%class%", room.getName())
                .replace("%grade%", grade)
                .replace("%money%", String.valueOf(money))
                .replace("%xp%", String.valueOf(xp));
    }
}