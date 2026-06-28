package net.bov.main.Commands;

import net.bov.main.VenturaClassroom;
import net.bov.main.Libs.Libs;
import net.bov.main.Classes.ClassManager;
import net.bov.main.Classes.Classroom;
import net.bov.main.GUI.CalendarMenu;
import net.bov.main.Classes.SubmissionsMenu;
import net.bov.main.Classes.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class MainCommand implements CommandExecutor, TabCompleter {
    private static final Libs Libs = new Libs();

    private ClassManager mgr() {
        return VenturaClassroom.getInstance().getClassManager();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0 || args[0].equalsIgnoreCase("help")) {
            int page = 1;
            if (args.length >= 2) {
                try {
                    page = Integer.parseInt(args[1]);
                } catch (NumberFormatException ignored) {
                }
            }
            Libs.MHelp(sender, page);
            return true;
        }

        String sub = args[0].toLowerCase(Locale.ROOT);
        switch (sub) {
            case "list":
                Libs.ClassList(sender, mgr().all());
                return true;

            case "next": {
                Classroom room = mgr().getNextClass();
                if (room == null) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&7There are no scheduled classes right now."));
                    return true;
                }
                int wait = mgr().waitUntilNext(room);
                sender.sendMessage(Libs.format(Libs.Prefix + "&6Next class: &e" + room.getName()
                        + " &7in &a" + TimeUtil.prettyDuration(wait)
                        + (room.isInSession() ? " &7(&ain session now&7)" : "")));
                return true;
            }

            case "info": {
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                info(sender, room);
                return true;
            }

            case "join": {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                String result = mgr().join(player, room);
                switch (result) {
                    case "ok":
                        player.sendMessage(Libs.format(Libs.Prefix + "&aYou joined &e" + room.getName() + "&a!"));
                        notifyStaff(room, "&e" + player.getName() + " &7joined the class.");
                        break;
                    case "no-session":
                        player.sendMessage(Libs.format(Libs.Prefix + "&c" + room.getName() + " is not in session right now."));
                        break;
                    case "locked":
                        player.sendMessage(Libs.format(Libs.Prefix + "&cJoining is locked for this class."));
                        break;
                    case "full":
                        player.sendMessage(Libs.format(Libs.Prefix + "&cThis class is full."));
                        break;
                    case "already":
                        player.sendMessage(Libs.format(Libs.Prefix + "&7You are already in this class."));
                        break;
                    default:
                        break;
                }
                return true;
            }

            case "leave": {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                for (Classroom room : mgr().all()) {
                    if (mgr().leave(player, room)) {
                        player.sendMessage(Libs.format(Libs.Prefix + "&7You left &e" + room.getName() + "&7."));
                        return true;
                    }
                }
                player.sendMessage(Libs.format(Libs.Prefix + "&7You are not in a class."));
                return true;
            }

            case "start": {
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (!staff(sender, room)) return noStaff(sender);
                mgr().startClass(room);
                sender.sendMessage(Libs.format(Libs.Prefix + "&aStarted &e" + room.getName() + "&a and announced it."));
                return true;
            }

            case "end": {
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (!staff(sender, room)) return noStaff(sender);
                mgr().endClass(room);
                sender.sendMessage(Libs.format(Libs.Prefix + "&aEnded &e" + room.getName() + "&a."));
                return true;
            }

            case "lock": {
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (!staff(sender, room)) return noStaff(sender);
                room.setJoinable(false);
                sender.sendMessage(Libs.format(Libs.Prefix + "&eJoining is now locked for &6" + room.getName() + "&e."));
                return true;
            }

            case "unlock": {
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (!staff(sender, room)) return noStaff(sender);
                room.setJoinable(true);
                sender.sendMessage(Libs.format(Libs.Prefix + "&aJoining is now open for &6" + room.getName() + "&a."));
                return true;
            }

            case "capacity": {
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (!staff(sender, room)) return noStaff(sender);
                if (args.length < 3) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cUsage: /class capacity <name> <number>"));
                    return true;
                }
                try {
                    room.setCapacity(Integer.parseInt(args[2]));
                } catch (NumberFormatException ex) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cThat is not a number."));
                    return true;
                }
                mgr().save();
                sender.sendMessage(Libs.format(Libs.Prefix + "&aCapacity for &e" + room.getName() + " &ais now &e" + room.getCapacity() + "&a."));
                return true;
            }

            case "giveitem": {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (!staff(sender, room)) return noStaff(sender);
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cHold the item you want to give first."));
                    return true;
                }
                int n = mgr().giveItemToStudents(room, hand);
                sender.sendMessage(Libs.format(Libs.Prefix + "&aGave &e" + hand.getType().name().toLowerCase(Locale.ROOT)
                        + " &ato &e" + n + " &astudent(s)."));
                return true;
            }

            case "create": {
                if (!admin(sender)) return noPerm(sender);
                if (args.length < 2) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cUsage: /class create <name>"));
                    return true;
                }
                String id = args[1].toLowerCase(Locale.ROOT);
                if (mgr().get(id) != null) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cA class called &e" + id + " &calready exists."));
                    return true;
                }
                Classroom room = mgr().create(id);
                room.setCapacity(VenturaClassroom.getInstance().getConfig().getInt("default-capacity", 30));
                if (sender instanceof Player) {
                    room.setLocation(((Player) sender).getLocation());
                }
                mgr().save();
                sender.sendMessage(Libs.format(Libs.Prefix + "&aCreated class &e" + id
                        + "&a. Set a teacher with &e/class setteacher " + id + " <player>&a and times with &e/class settime " + id + " <time>&a."));
                return true;
            }

            case "delete": {
                if (!admin(sender)) return noPerm(sender);
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                mgr().delete(room.getId());
                sender.sendMessage(Libs.format(Libs.Prefix + "&aDeleted class &e" + room.getId() + "&a."));
                return true;
            }

            case "setname": {
                if (!admin(sender)) return noPerm(sender);
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (args.length < 3) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cUsage: /class setname <name> <display name>"));
                    return true;
                }
                StringBuilder sb = new StringBuilder();
                for (int i = 2; i < args.length; i++) {
                    sb.append(i > 2 ? " " : "").append(args[i]);
                }
                room.setName(sb.toString());
                mgr().save();
                sender.sendMessage(Libs.format(Libs.Prefix + "&aName set to &e" + room.getName() + "&a."));
                return true;
            }

            case "setteacher": {
                if (!admin(sender)) return noPerm(sender);
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                OfflinePlayer target = resolvePlayer(sender, args, 2);
                if (target == null) return true;
                room.setTeacher(target.getUniqueId());
                mgr().save();
                sender.sendMessage(Libs.format(Libs.Prefix + "&aTeacher of &e" + room.getName() + " &ais now &e" + target.getName() + "&a."));
                return true;
            }

            case "addsub": {
                if (!admin(sender)) return noPerm(sender);
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                OfflinePlayer target = resolvePlayer(sender, args, 2);
                if (target == null) return true;
                room.getSubTeachers().add(target.getUniqueId());
                mgr().save();
                sender.sendMessage(Libs.format(Libs.Prefix + "&aAdded &e" + target.getName() + " &aas a sub-teacher of &e" + room.getName() + "&a."));
                return true;
            }

            case "removesub": {
                if (!admin(sender)) return noPerm(sender);
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                OfflinePlayer target = resolvePlayer(sender, args, 2);
                if (target == null) return true;
                room.getSubTeachers().remove(target.getUniqueId());
                mgr().save();
                sender.sendMessage(Libs.format(Libs.Prefix + "&aRemoved &e" + target.getName() + " &afrom &e" + room.getName() + "&a."));
                return true;
            }

            case "setlocation": {
                if (!admin(sender)) return noPerm(sender);
                Player player = requirePlayer(sender);
                if (player == null) return true;
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                room.setLocation(player.getLocation());
                mgr().save();
                sender.sendMessage(Libs.format(Libs.Prefix + "&aLocation for &e" + room.getName() + " &aset to where you are standing."));
                return true;
            }

            case "settime": {
                if (!admin(sender)) return noPerm(sender);
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (args.length < 3) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cUsage: /class settime <name> <time>  (e.g. 9am)"));
                    return true;
                }
                int ticks = TimeUtil.parseTime(args[2]);
                if (ticks < 0) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cInvalid time. Try 9am, 14:30, noon or dusk."));
                    return true;
                }
                room.getTimes().add(ticks);
                mgr().save();
                sender.sendMessage(Libs.format(Libs.Prefix + "&e" + room.getName() + " &awill run at &e" + TimeUtil.ticksToClock(ticks) + "&a."));
                return true;
            }

            case "deltime": {
                if (!admin(sender)) return noPerm(sender);
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (args.length < 3) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cUsage: /class deltime <name> <time>"));
                    return true;
                }
                int ticks = TimeUtil.parseTime(args[2]);
                if (ticks >= 0 && room.getTimes().remove(ticks)) {
                    mgr().save();
                    sender.sendMessage(Libs.format(Libs.Prefix + "&aRemoved the &e" + TimeUtil.ticksToClock(ticks) + " &atime."));
                } else {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cThat class has no time set for then."));
                }
                return true;
            }

            case "calendar": {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                player.openInventory(new CalendarMenu(mgr().all()).getInventory());
                return true;
            }

            case "grade": {
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (!staff(sender, room)) return noStaff(sender);
                if (args.length < 4) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cUsage: /class grade <name> <player> <grade>"));
                    sender.sendMessage(Libs.format(Libs.Prefix + "&7Grades: &e" + String.join("&7, &e", mgr().gradeKeys())));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[2]);
                if (target == null) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cThat player must be online."));
                    return true;
                }
                if (!room.getStudents().contains(target.getUniqueId())) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&c" + target.getName() + " is not in that class."));
                    return true;
                }
                String grade = mgr().resolveGrade(args[3]);
                if (grade == null) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cUnknown grade. Grades: &e" + String.join("&7, &e", mgr().gradeKeys())));
                    return true;
                }
                room.getGrades().put(target.getUniqueId(), grade);
                sender.sendMessage(Libs.format(Libs.Prefix + "&aGraded &e" + target.getName() + " &7as &6" + grade
                        + "&7. They get the reward when you dismiss the class."));
                return true;
            }

            case "dismiss": {
                Classroom room = need(sender, args, 1);
                if (room == null) return true;
                if (!staff(sender, room)) return noStaff(sender);
                if (room.getStudents().isEmpty()) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&7There are no students to dismiss in &e" + room.getName() + "&7."));
                    return true;
                }

                if (args.length >= 4) {
                    Player target = Bukkit.getPlayerExact(args[2]);
                    if (target == null || !room.getStudents().contains(target.getUniqueId())) {
                        sender.sendMessage(Libs.format(Libs.Prefix + "&cThat player is not in the class."));
                        return true;
                    }
                    String grade = mgr().resolveGrade(args[3]);
                    if (grade == null) {
                        sender.sendMessage(Libs.format(Libs.Prefix + "&cUnknown grade. Grades: &e" + String.join("&7, &e", mgr().gradeKeys())));
                        return true;
                    }
                    mgr().applyGrade(target, room, grade);
                    target.sendMessage(Libs.format(Libs.Prefix + "&eYou were dismissed from &6" + room.getName() + " &ewith grade &6" + grade + "&e."));
                    room.getStudents().remove(target.getUniqueId());
                    room.getGrades().remove(target.getUniqueId());
                    sender.sendMessage(Libs.format(Libs.Prefix + "&aDismissed &e" + target.getName() + " &awith grade &6" + grade + "&a."));
                    return true;
                }

                String forced = args.length >= 3 ? mgr().resolveGrade(args[2]) : null;
                if (args.length >= 3 && forced == null) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&cUnknown grade. Grades: &e" + String.join("&7, &e", mgr().gradeKeys())));
                    return true;
                }

                int dismissed = 0;
                for (UUID id : new ArrayList<>(room.getStudents())) {
                    Player s = Bukkit.getPlayer(id);
                    if (s == null || !s.isOnline()) {
                        continue;
                    }
                    String grade = forced != null ? forced : room.getGrades().get(id);
                    if (grade != null) {
                        mgr().applyGrade(s, room, grade);
                        s.sendMessage(Libs.format(Libs.Prefix + "&eClass dismissed: &6" + room.getName() + " &7(grade &6" + grade + "&7)"));
                    } else {
                        s.sendMessage(Libs.format(Libs.Prefix + "&eClass dismissed: &6" + room.getName() + "&7."));
                    }
                    dismissed++;
                }

                room.getStudents().clear();
                room.getGrades().clear();
                mgr().endClass(room);
                sender.sendMessage(Libs.format(Libs.Prefix + "&aDismissed &e" + dismissed + " &astudent(s) from &e" + room.getName()
                        + (forced != null ? " &awith grade &6" + forced + "&a." : "&a.")));
                return true;
            }

            case "reload": {
                if (!admin(sender)) return noPerm(sender);
                VenturaClassroom.getInstance().reloadConfig();
                sender.sendMessage(Libs.format(Libs.Prefix + "&aReloaded config.yml (settings and grades)."));
                return true;
            }

            case "submit": {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                Classroom room = null;
                for (Classroom r : mgr().all()) {
                    if (r.getStudents().contains(player.getUniqueId())) {
                        room = r;
                        break;
                    }
                }
                if (room == null) {
                    player.sendMessage(Libs.format(Libs.Prefix + "&cYou need to be in a class to submit work."));
                    return true;
                }
                ItemStack hand = player.getInventory().getItemInMainHand();
                if (hand == null || hand.getType().isAir()) {
                    player.sendMessage(Libs.format(Libs.Prefix + "&cHold the work you want to submit (e.g. a written book)."));
                    return true;
                }
                room.getSubmissions().put(player.getUniqueId(), hand.clone());
                player.sendMessage(Libs.format(Libs.Prefix + "&aSubmitted your work to &e" + room.getName() + "&a."));
                notifyStaff(room, "&e" + player.getName() + " &7submitted work.");
                return true;
            }

            case "submissions": {
                Player player = requirePlayer(sender);
                if (player == null) return true;
                Classroom room;
                if (args.length >= 2) {
                    room = need(sender, args, 1);
                    if (room == null) return true;
                } else {
                    room = findStaffClass(sender);
                    if (room == null) {
                        sender.sendMessage(Libs.format(Libs.Prefix + "&cName a class: &e/class submissions <name>"));
                        return true;
                    }
                }
                if (!staff(sender, room)) return noStaff(sender);
                if (room.getSubmissions().isEmpty()) {
                    sender.sendMessage(Libs.format(Libs.Prefix + "&7No submissions yet for &e" + room.getName() + "&7."));
                    return true;
                }
                player.openInventory(new SubmissionsMenu(room).getInventory());
                return true;
            }

            default:
                sender.sendMessage(Libs.format(Libs.Prefix + "&cUnknown subcommand. Try &e/class help&c."));
                return true;
        }
    }

    private void info(CommandSender sender, Classroom room) {
        sender.sendMessage(Libs.format("&8&m------------------&r &6" + room.getName() + " &8&m------------------"));
        String teacher = room.getTeacher() == null ? "&cnone" : "&e" + nameOf(room.getTeacher());
        sender.sendMessage(Libs.format("&7Teacher: " + teacher));
        if (!room.getSubTeachers().isEmpty()) {
            StringBuilder sb = new StringBuilder();
            for (UUID u : room.getSubTeachers()) {
                sb.append(sb.length() > 0 ? "&7, &e" : "&e").append(nameOf(u));
            }
            sender.sendMessage(Libs.format("&7Assistants: " + sb));
        }
        sender.sendMessage(Libs.format("&7Students: &e" + room.getStudents().size() + "&7/&e" + room.getCapacity()));
        String status = room.isInSession()
                ? (room.isJoinable() ? "&aIn session (open)" : "&eIn session (locked)")
                : "&7Not running";
        sender.sendMessage(Libs.format("&7Status: " + status));
        if (room.getTimes().isEmpty()) {
            sender.sendMessage(Libs.format("&7Times: &cnone set"));
        } else {
            StringBuilder sb = new StringBuilder();
            for (int t : room.getTimes()) {
                sb.append(sb.length() > 0 ? "&7, &e" : "&e").append(TimeUtil.ticksToClock(t));
            }
            sender.sendMessage(Libs.format("&7Times: " + sb));
        }
    }

    private String nameOf(UUID uuid) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return p.getName() == null ? uuid.toString().substring(0, 8) : p.getName();
    }

    private Classroom need(CommandSender sender, String[] args, int index) {
        if (args.length <= index) {
            sender.sendMessage(Libs.format(Libs.Prefix + "&cYou need to name a class. See &e/class list&c."));
            return null;
        }
        Classroom room = mgr().get(args[index]);
        if (room == null) {
            sender.sendMessage(Libs.format(Libs.Prefix + "&cThere is no class called &e" + args[index] + "&c."));
        }
        return room;
    }

    private OfflinePlayer resolvePlayer(CommandSender sender, String[] args, int index) {
        if (args.length <= index) {
            sender.sendMessage(Libs.format(Libs.Prefix + "&cName a player."));
            return null;
        }
        Player online = Bukkit.getPlayerExact(args[index]);
        if (online != null) {
            return online;
        }
        sender.sendMessage(Libs.format(Libs.Prefix + "&cThat player must be online to assign them."));
        return null;
    }

    private Player requirePlayer(CommandSender sender) {
        if (sender instanceof Player) {
            return (Player) sender;
        }
        sender.sendMessage(Libs.console);
        return null;
    }

    private boolean admin(CommandSender sender) {
        return sender.hasPermission("venturaclasses.admin");
    }

    private boolean staff(CommandSender sender, Classroom room) {
        if (sender.hasPermission("venturaclasses.admin")) {
            return true;
        }
        return sender instanceof Player && room.isStaff(((Player) sender).getUniqueId());
    }

    private Classroom findStaffClass(CommandSender sender) {
        for (Classroom room : mgr().all()) {
            if (staff(sender, room)) {
                return room;
            }
        }
        return null;
    }

    private boolean noPerm(CommandSender sender) {
        sender.sendMessage(Libs.NoPermission);
        return true;
    }

    private boolean noStaff(CommandSender sender) {
        sender.sendMessage(Libs.format(Libs.Prefix + "&cOnly this class's teacher or an admin can do that."));
        return true;
    }

    private void notifyStaff(Classroom room, String message) {
        sendIf(room.getTeacher(), message);
        for (UUID u : room.getSubTeachers()) {
            sendIf(u, message);
        }
    }

    private void sendIf(UUID uuid, String message) {
        if (uuid == null) {
            return;
        }
        Player p = Bukkit.getPlayer(uuid);
        if (p != null && p.isOnline()) {
            p.sendMessage(Libs.format(Libs.Prefix + message));
        }
    }

    public static void HelpCommand(CommandSender sender) {
        Libs.MHelp(sender, 1);
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String label, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            List<String> subs = new ArrayList<>();
            subs.add("list");
            subs.add("next");
            subs.add("info");
            subs.add("calendar");
            subs.add("join");
            subs.add("leave");
            subs.add("submit");
            subs.add("help");
            if (sender.hasPermission("venturaclasses.teacher")) {
                subs.add("start");
                subs.add("end");
                subs.add("lock");
                subs.add("unlock");
                subs.add("capacity");
                subs.add("giveitem");
                subs.add("dismiss");
                subs.add("grade");
                subs.add("submissions");
            }
            if (sender.hasPermission("venturaclasses.admin")) {
                subs.add("create");
                subs.add("delete");
                subs.add("setname");
                subs.add("setteacher");
                subs.add("addsub");
                subs.add("removesub");
                subs.add("setlocation");
                subs.add("settime");
                subs.add("deltime");
                subs.add("reload");
            }
            return prefix(subs, args[0]);
        }
        if (args.length == 2 && !args[0].equalsIgnoreCase("create")) {
            for (Classroom room : mgr().all()) {
                out.add(room.getId());
            }
            return prefix(out, args[1]);
        }
        if (args.length == 3) {
            String s = args[0].toLowerCase(Locale.ROOT);
            if (s.equals("settime") || s.equals("deltime")) {
                return prefix(java.util.Arrays.asList("6am", "9am", "noon", "2pm", "5pm", "8pm"), args[2]);
            }
            if (s.equals("setteacher") || s.equals("addsub") || s.equals("removesub") || s.equals("grade")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    out.add(p.getName());
                }
                return prefix(out, args[2]);
            }
            if (s.equals("dismiss")) {
                for (Player p : Bukkit.getOnlinePlayers()) {
                    out.add(p.getName());
                }
                out.addAll(mgr().gradeKeys());
                return prefix(out, args[2]);
            }
        }
        if (args.length == 4) {
            String s = args[0].toLowerCase(Locale.ROOT);
            if (s.equals("grade") || s.equals("dismiss")) {
                return prefix(new ArrayList<>(mgr().gradeKeys()), args[3]);
            }
        }
        return out;
    }

    private List<String> prefix(List<String> options, String token) {
        String lower = token.toLowerCase(Locale.ROOT);
        List<String> out = new ArrayList<>();
        for (String o : options) {
            if (o.toLowerCase(Locale.ROOT).startsWith(lower)) {
                out.add(o);
            }
        }
        return out;
    }
}