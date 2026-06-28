package net.bov.main.Classes;

import net.bov.main.Integrations.EconomyHook;
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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ClassManager {

    private static final long CHECK_INTERVAL = 40L;

    private final VenturaClassroom plugin;
    private final File file;
    private FileConfiguration config;

    private final Map<String, Classroom> classes = new LinkedHashMap<>();
    private final Map<String, Integer> lastSlot = new HashMap<>();

    private BukkitTask task;

    public ClassManager(VenturaClassroom plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "classes.yml");
    }

    public void load() {
        this.classes.clear();
        this.lastSlot.clear();
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
                for (int t : this.config.getIntegerList(base + ".times")) {
                    room.getTimes().add(TimeUtil.normalize(t));
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
            out.set(base + ".teacher", room.getTeacher() == null ? null : room.getTeacher().toString());
            List<String> subs = new ArrayList<>();
            for (UUID u : room.getSubTeachers()) {
                subs.add(u.toString());
            }
            out.set(base + ".subTeachers", subs);
            out.set(base + ".times", new ArrayList<>(room.getTimes()));
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
            this.lastSlot.remove(id.toLowerCase());
            save();
        }
    }

    public java.util.Collection<Classroom> all() {
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
        for (Classroom room : this.classes.values()) {
            if (room.getTimes().isEmpty() || room.getLocation() == null || room.getLocation().getWorld() == null) {
                continue;
            }
            int now = (int) (room.getLocation().getWorld().getTime() % 24000L);
            Integer floor = room.getTimes().floor(now);
            if (floor == null) {
                floor = room.getTimes().last();
            }
            Integer prev = this.lastSlot.get(room.getId().toLowerCase());
            this.lastSlot.put(room.getId().toLowerCase(), floor);
            if (prev != null && !prev.equals(floor) && !room.isInSession()) {
                startClass(room);
            }
        }
    }

    public void startClass(Classroom room) {
        room.resetSession();
        room.setInSession(true);
        room.setJoinable(true);
        Libs.broadcastClassStart(room);
    }

    public void endClass(Classroom room) {
        room.setInSession(false);
        room.setJoinable(false);
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
        if (room.getLocation() != null && this.plugin.getConfig().getBoolean("teleport-on-join", true)) {
            player.teleport(room.getLocation());
        }
        return "ok";
    }

    public boolean leave(Player player, Classroom room) {
        return room.getStudents().remove(player.getUniqueId());
    }

    public Classroom getNextClass() {
        Classroom best = null;
        int bestWait = Integer.MAX_VALUE;
        for (Classroom room : this.classes.values()) {
            if (room.getTimes().isEmpty() || room.getLocation() == null || room.getLocation().getWorld() == null) {
                continue;
            }
            int now = (int) (room.getLocation().getWorld().getTime() % 24000L);
            for (int t : room.getTimes()) {
                int wait = TimeUtil.ticksUntil(now, t);
                if (wait < bestWait) {
                    bestWait = wait;
                    best = room;
                }
            }
        }
        return best;
    }

    public int waitUntilNext(Classroom room) {
        if (room.getLocation() == null || room.getLocation().getWorld() == null || room.getTimes().isEmpty()) {
            return -1;
        }
        int now = (int) (room.getLocation().getWorld().getTime() % 24000L);
        int best = Integer.MAX_VALUE;
        for (int t : room.getTimes()) {
            best = Math.min(best, TimeUtil.ticksUntil(now, t));
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
            EconomyHook eco = this.plugin.getEconomyHook();
            if (eco != null && eco.available()) {
                eco.deposit(student, money);
            }
        }
        if (xp > 0) {
            student.giveExp(xp);
        }
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