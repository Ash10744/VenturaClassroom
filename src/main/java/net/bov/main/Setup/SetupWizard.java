package net.bov.main.Setup;

import net.bov.main.Classes.ClassManager;
import net.bov.main.Classes.ClassTime;
import net.bov.main.Classes.Classroom;
import net.bov.main.Classes.TimeUtil;
import net.bov.main.Libs.Libs;
import net.bov.main.VenturaClassroom;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

public class SetupWizard {

    private final VenturaClassroom plugin;
    private final Map<UUID, SetupSession> sessions = new HashMap<>();

    public SetupWizard(VenturaClassroom plugin) {
        this.plugin = plugin;
    }

    private ClassManager mgr() {
        return this.plugin.getClassManager();
    }

    public boolean isAwaitingChat(Player player) {
        SetupSession s = this.sessions.get(player.getUniqueId());
        return s != null && s.awaitingChat != null;
    }

    public void handle(Player player, String[] args) {
        if (args.length < 2) {
            start(player);
            return;
        }
        SetupSession s = this.sessions.get(player.getUniqueId());
        String action = args[1].toLowerCase(Locale.ROOT);
        if (action.equals("cancel")) {
            this.sessions.remove(player.getUniqueId());
            send(player, Libs.format("&8[&6VClassroom&8] &7Setup cancelled."));
            return;
        }
        if (s == null) {
            start(player);
            return;
        }
        switch (action) {
            case "teacher": {
                if (args.length < 3) {
                    promptTeacher(player, s);
                    return;
                }
                Player t = Bukkit.getPlayerExact(args[2]);
                if (t == null) {
                    send(player, Libs.format("&8[&6VClassroom&8] &cThat player is not online."));
                    promptTeacher(player, s);
                    return;
                }
                s.teacher = t.getUniqueId();
                s.step = SetupSession.Step.WARP;
                promptWarp(player, s);
                return;
            }
            case "skipteacher":
                s.teacher = null;
                s.step = SetupSession.Step.WARP;
                promptWarp(player, s);
                return;
            case "warp":
                s.location = player.getLocation();
                s.step = SetupSession.Step.DAY;
                promptDay(player, s);
                return;
            case "skipwarp":
                s.location = null;
                s.step = SetupSession.Step.DAY;
                promptDay(player, s);
                return;
            case "day": {
                if (args.length < 3) {
                    promptDay(player, s);
                    return;
                }
                Set<DayOfWeek> days = TimeUtil.parseDays(args[2]);
                if (days.isEmpty()) {
                    send(player, Libs.format("&8[&6VClassroom&8] &cUnknown day."));
                    promptDay(player, s);
                    return;
                }
                s.pendingDays.clear();
                s.pendingDays.addAll(days);
                s.step = SetupSession.Step.TIME;
                promptTime(player, s);
                return;
            }
            case "customtime":
                s.awaitingChat = "time";
                send(player, Libs.format("&8[&6VClassroom&8] &7Type a time in chat (e.g. &e9am&7, &e2:30pm&7, &e14:00&7)."));
                return;
            case "time": {
                if (args.length < 3) {
                    promptTime(player, s);
                    return;
                }
                LocalTime time = TimeUtil.parseClock(args[2]);
                if (time == null) {
                    send(player, Libs.format("&8[&6VClassroom&8] &cInvalid time. Try 9am, 2:30pm or 14:00."));
                    promptTime(player, s);
                    return;
                }
                addTimes(s, time);
                promptTimeAdded(player, s);
                return;
            }
            case "moretimes":
                s.pendingDays.clear();
                s.step = SetupSession.Step.DAY;
                promptDay(player, s);
                return;
            case "donetimes":
            case "skiptimes":
                s.step = SetupSession.Step.DURATION;
                promptDuration(player, s);
                return;
            case "duration": {
                if (args.length < 3) {
                    promptDuration(player, s);
                    return;
                }
                if (args[2].equalsIgnoreCase("none")) {
                    s.durationSeconds = 0;
                } else {
                    int secs = TimeUtil.parseDuration(args[2]);
                    s.durationSeconds = Math.max(0, secs);
                }
                finish(player, s);
                return;
            }
            default:
                resume(player, s);
                return;
        }
    }

    public void handleChat(Player player, String message) {
        SetupSession s = this.sessions.get(player.getUniqueId());
        if (s == null || s.awaitingChat == null) {
            return;
        }
        if (s.awaitingChat.equals("name")) {
            String display = message.trim();
            if (display.isEmpty()) {
                send(player, Libs.format("&8[&6VClassroom&8] &cPlease type a name."));
                return;
            }
            s.name = display;
            s.awaitingChat = null;
            s.step = SetupSession.Step.TEACHER;
            send(player, Libs.format("&8[&6VClassroom&8] &aName set to &e" + display + "&7."));
            promptTeacher(player, s);
            return;
        }
        if (s.awaitingChat.equals("time")) {
            LocalTime time = TimeUtil.parseClock(message.trim());
            if (time == null) {
                send(player, Libs.format("&8[&6VClassroom&8] &cInvalid time. Try 9am, 2:30pm or 14:00."));
                return;
            }
            s.awaitingChat = null;
            addTimes(s, time);
            promptTimeAdded(player, s);
        }
    }

    private void addTimes(SetupSession s, LocalTime time) {
        for (DayOfWeek d : s.pendingDays) {
            ClassTime ct = new ClassTime(d, time);
            if (!s.times.contains(ct)) {
                s.times.add(ct);
            }
        }
    }

    public void start(Player player) {
        SetupSession s = new SetupSession();
        this.sessions.put(player.getUniqueId(), s);
        header(player, 1, "Name");
        send(player, Libs.format("&7Welcome to guided class setup. Type the class &fname &7in chat."));
        send(player, Libs.format("&7You can use spaces, e.g. &fMaths 101&7."));
        row(player, btn("&c[Cancel]", "&7Cancel setup", "/class setup cancel"));
    }

    private void resume(Player player, SetupSession s) {
        switch (s.step) {
            case TEACHER: promptTeacher(player, s); break;
            case WARP: promptWarp(player, s); break;
            case DAY: promptDay(player, s); break;
            case TIME: promptTime(player, s); break;
            case DURATION: promptDuration(player, s); break;
            default:
                send(player, Libs.format("&8[&6VClassroom&8] &7Type the class name in chat."));
                break;
        }
    }

    private void promptTeacher(Player player, SetupSession s) {
        header(player, 2, "Teacher");
        send(player, Libs.format("&7Click the player who will teach &e" + s.name + "&7:"));
        TextComponent line = new TextComponent("");
        int shown = 0;
        for (Player p : Bukkit.getOnlinePlayers()) {
            if (shown >= 12) {
                break;
            }
            line.addExtra(btn("&a[" + p.getName() + "]", "&7Set teacher to &e" + p.getName(),
                    "/class setup teacher " + p.getName()));
            shown++;
        }
        if (shown > 0) {
            player.spigot().sendMessage(line);
        }
        row(player, btn("&7[Skip]", "&7Assign a teacher later", "/class setup skipteacher"),
                btn("&c[Cancel]", "&7Cancel setup", "/class setup cancel"));
    }

    private void promptWarp(Player player, SetupSession s) {
        header(player, 3, "Warp");
        send(player, Libs.format("&7Stand where students should arrive, then click below."));
        row(player, btn("&a[Set Warp Here]", "&7Use your current location", "/class setup warp"),
                btn("&7[Skip]", "&7Set the warp later", "/class setup skipwarp"),
                btn("&c[Cancel]", "&7Cancel setup", "/class setup cancel"));
    }

    private void promptDay(Player player, SetupSession s) {
        header(player, 4, "Days");
        send(player, Libs.format("&7Pick the day(s) &e" + s.name + " &7runs on:"));
        TextComponent days = new TextComponent("");
        String[] dd = {"Mon:monday", "Tue:tuesday", "Wed:wednesday", "Thu:thursday", "Fri:friday", "Sat:saturday", "Sun:sunday"};
        for (String d : dd) {
            String[] parts = d.split(":");
            days.addExtra(btn("&e[" + parts[0] + "]", "&7Schedule on " + parts[1], "/class setup day " + parts[1]));
        }
        player.spigot().sendMessage(days);
        row(player, btn("&b[Daily]", "&7Every day", "/class setup day daily"),
                btn("&b[Weekdays]", "&7Mon-Fri", "/class setup day weekdays"),
                btn("&b[Weekend]", "&7Sat & Sun", "/class setup day weekend"));
        row(player, btn("&7[Skip times]", "&7No schedule for now", "/class setup skiptimes"),
                btn("&c[Cancel]", "&7Cancel setup", "/class setup cancel"));
    }

    private void promptTime(Player player, SetupSession s) {
        header(player, 4, "Time");
        send(player, Libs.format("&7Days: &e" + daysLabel(s.pendingDays) + "&7. Pick a time:"));
        TextComponent times = new TextComponent("");
        String[] tt = {"6am", "9am", "noon", "2pm", "5pm", "8pm"};
        for (String t : tt) {
            times.addExtra(btn("&e[" + t + "]", "&7Run at " + t, "/class setup time " + t));
        }
        player.spigot().sendMessage(times);
        row(player, btn("&b[Type a time]", "&7Enter a custom time in chat", "/class setup customtime"),
                btn("&c[Cancel]", "&7Cancel setup", "/class setup cancel"));
    }

    private void promptTimeAdded(Player player, SetupSession s) {
        send(player, Libs.format("&8[&6VClassroom&8] &aSchedule so far: &f" + scheduleLabel(s)));
        row(player, btn("&a[Add more]", "&7Add another day/time", "/class setup moretimes"),
                btn("&e[Done]", "&7Finish scheduling", "/class setup donetimes"),
                btn("&c[Cancel]", "&7Cancel setup", "/class setup cancel"));
    }

    private void promptDuration(Player player, SetupSession s) {
        header(player, 5, "Duration");
        send(player, Libs.format("&7How long should each class run once started?"));
        row(player, btn("&e[30m]", "&730 minutes", "/class setup duration 30m"),
                btn("&e[45m]", "&745 minutes", "/class setup duration 45m"),
                btn("&e[1h]", "&71 hour", "/class setup duration 1h"),
                btn("&e[90m]", "&790 minutes", "/class setup duration 90m"),
                btn("&e[2h]", "&72 hours", "/class setup duration 2h"));
        row(player, btn("&7[No limit]", "&7Runs until ended manually", "/class setup duration none"),
                btn("&c[Cancel]", "&7Cancel setup", "/class setup cancel"));
    }

    private void finish(Player player, SetupSession s) {
        this.sessions.remove(player.getUniqueId());
        if (s.name == null || s.name.trim().isEmpty()) {
            send(player, Libs.format("&8[&6VClassroom&8] &cCould not create the class (no name). Start again with &e/class setup&c."));
            return;
        }
        s.id = mgr().nextId();
        Classroom room = mgr().create(s.id);
        room.setName(s.name);
        room.setCapacity(this.plugin.getConfig().getInt("default-capacity", 30));
        if (s.teacher != null) {
            room.setTeacher(s.teacher);
        }
        if (s.location != null) {
            room.setLocation(s.location);
        }
        for (ClassTime ct : s.times) {
            room.getTimes().add(ct);
        }
        room.setDurationSeconds(s.durationSeconds);
        mgr().save();

        send(player, Libs.format("&8&m---------------------------------------------|>"));
        send(player, Libs.format("&aClass setup complete! &7(id: &e" + s.id + "&7)"));
        send(player, Libs.format("&7Name: &e" + s.name));
        send(player, Libs.format("&7Teacher: " + (s.teacher == null ? "&cnone" : "&e" + nameOf(s.teacher))));
        send(player, Libs.format("&7Warp: " + (s.location != null ? "&aset" : "&cnot set")));
        send(player, Libs.format("&7Schedule: &f" + scheduleLabel(s)));
        send(player, Libs.format("&7Duration: &e" + (s.durationSeconds > 0 ? TimeUtil.formatDuration(s.durationSeconds) : "until ended")));
        row(player, btn("&a[Start now]", "&7Announce and open the class", "/class start " + s.id),
                btn("&e[Info]", "&7View the class", "/class info " + s.id),
                btn("&b[Calendar]", "&7Open the calendar", "/class calendar"));
        send(player, Libs.format("&8&m---------------------------------------------|>"));
    }

    private String daysLabel(Set<DayOfWeek> days) {
        StringBuilder sb = new StringBuilder();
        for (DayOfWeek d : days) {
            sb.append(sb.length() > 0 ? ", " : "").append(TimeUtil.dayShort(d));
        }
        return sb.length() == 0 ? "none" : sb.toString();
    }

    private String scheduleLabel(SetupSession s) {
        if (s.times.isEmpty()) {
            return "none";
        }
        StringBuilder sb = new StringBuilder();
        for (ClassTime ct : s.times) {
            sb.append(sb.length() > 0 ? ", " : "").append(TimeUtil.dayShort(ct.getDay())).append(" ").append(TimeUtil.formatClock(ct.getTime()));
        }
        return sb.toString();
    }

    private String nameOf(UUID uuid) {
        org.bukkit.OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return p.getName() == null ? uuid.toString().substring(0, 8) : p.getName();
    }

    private void header(Player player, int step, String title) {
        send(player, Libs.format("&8&m---------------------------------------------|>"));
        send(player, Libs.format("&6Class Setup &8(&7step " + step + "/5&8) &8- &e" + title));
    }

    private void send(Player player, String message) {
        player.sendMessage(message);
    }

    private void row(Player player, TextComponent... parts) {
        TextComponent row = new TextComponent("");
        for (TextComponent t : parts) {
            row.addExtra(t);
        }
        player.spigot().sendMessage(row);
    }

    private TextComponent btn(String label, String hover, String command) {
        TextComponent t = new TextComponent(Libs.format(label + " "));
        t.setHoverEvent(new HoverEvent(HoverEvent.Action.SHOW_TEXT, new ComponentBuilder(Libs.format(hover)).create()));
        t.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, command));
        return t;
    }
}