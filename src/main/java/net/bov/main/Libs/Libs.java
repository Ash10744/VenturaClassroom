package net.bov.main.Libs;

import net.bov.main.VenturaClassroom;
import net.bov.main.Classes.Classroom;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import net.md_5.bungee.api.chat.hover.content.Content;
import net.md_5.bungee.api.chat.hover.content.Text;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Libs {
    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String NewLine = "\n";
    public String Prefix = "&8[&6VClassrooms&8] ";
    public String cmdstarter = format("◊ ");
    public String spacer = format(" &8- ");
    public String CommandDivider = format("&8&m---------------------------------------------|>");
    public String NoPermission = format("&8[] &cYou do not have permission to use this command");
    public String console = format("&cYou must be online to run this command");
    public String Version() {return VenturaClassroom.getInstance().getDescription().getVersion();}
    public void send(CommandSender sender, String msg) {
        sender.sendMessage(String.format(msg));
    }
    public static void debugMsg(String msg) {
        Bukkit.getServer().getConsoleSender().sendMessage(msg);
    }

    public static class ChatComponent {
        TextComponent component;

        public String format(String input) {return input.length() == 0 ? "Empty" : ChatColor.translateAlternateColorCodes('&', input);}
        public ChatComponent(String message) {
            this.component = new TextComponent(this.format(message));
        }
        public TextComponent getComponent() {
            return this.component;
        }
        public void addHoverMessage(String message) {this.component.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, (new ComponentBuilder(this.format(message))).create()));}
        public void addClickEvent(String command, ClickEvent.Action action) {this.component.setClickEvent(new ClickEvent(action, command));}
    }

    public void FormatHelp(CommandSender sender) {
        ChatComponent header = new ChatComponent("&f&nAvailable Commands:&r &7[] = Required, &7<> = Optional\n\n&7&oHover / Click for Additional Information\n");
        sender.spigot().sendMessage(header.getComponent());
    }

    public int maxPage(CommandSender sender) {
        if (sender.hasPermission("venturaclasses.admin")) {
            return 3;
        }
        if (sender.hasPermission("venturaclasses.teacher")) {
            return 2;
        }
        return 1;
    }

    public void MHelp(CommandSender sender, int page) {
        int max = maxPage(sender);
        if (page < 1) {
            page = 1;
        }
        if (page > max) {
            page = max;
        }

        send(sender, CommandDivider);
        MCTitle(sender);
        send(sender, NewLine);
        FormatHelp(sender);

        switch (page) {
            case 2:
                helpTeacher(sender);
                break;
            case 3:
                helpAdmin(sender);
                break;
            default:
                helpStudent(sender);
                break;
        }

        send(sender, NewLine);
        Pager(sender, page, max);
        send(sender, CommandDivider);
        PluginInformation(sender);
        send(sender, CommandDivider);
    }

    private void helpStudent(CommandSender sender) {
        helpLine(sender, "/class &flist", "See every classroom and click one to view it.", "/class list");
        helpLine(sender, "/class &fnext", "See which class is starting next and when.", "/class next");
        helpLine(sender, "/class &finfo <name>", "View a class: teacher, times, and how full it is.", "/class info ");
        helpLine(sender, "/class &fcalendar", "Open a menu of all classes and their times.", "/class calendar");
        helpLine(sender, "/class &fjoin <name>", "Join a class that is in session.", "/class join ");
        helpLine(sender, "/class &fleave", "Leave the class you are in.", "/class leave");
        helpLine(sender, "/class &fsubmit", "Submit the item in your hand (e.g. a book) to your class.", "/class submit");
    }

    private void helpTeacher(CommandSender sender) {
        helpLine(sender, "/class &fstart <name>", "Start your class now and open it for joining.", "/class start ");
        helpLine(sender, "/class &fend <name>", "End your class and dismiss the students.", "/class end ");
        helpLine(sender, "/class &fgrade <name> <player> <grade>", "Give a student a grade, applied when you dismiss.", "/class grade ");
        helpLine(sender, "/class &fdismiss <name> [grade]", "End the class and reward students by their grade.", "/class dismiss ");
        helpLine(sender, "/class &flock <name>", "Stop new students joining (those inside stay).", "/class lock ");
        helpLine(sender, "/class &funlock <name>", "Allow students to join again.", "/class unlock ");
        helpLine(sender, "/class &fcapacity <name> <n>", "Change how many students can join.", "/class capacity ");
        helpLine(sender, "/class &fgiveitem <name>", "Give the item in your hand to every student.", "/class giveitem ");
        helpLine(sender, "/class &fsubmissions <name>", "Open a menu of everything students submitted.", "/class submissions ");
    }

    private void helpAdmin(CommandSender sender) {
        helpLine(sender, "/class &fcreate <name>", "Create a new classroom.", "/class create ");
        helpLine(sender, "/class &fdelete <name>", "Delete a classroom.", "/class delete ");
        helpLine(sender, "/class &fsetname <name> <display>", "Set the display name of a class.", "/class setname ");
        helpLine(sender, "/class &fsetteacher <name> <player>", "Assign the teacher.", "/class setteacher ");
        helpLine(sender, "/class &faddsub <name> <player>", "Add a sub-teacher (assistant).", "/class addsub ");
        helpLine(sender, "/class &fremovesub <name> <player>", "Remove a sub-teacher.", "/class removesub ");
        helpLine(sender, "/class &fsetlocation <name>", "Set the class location to where you stand.", "/class setlocation ");
        helpLine(sender, "/class &fsettime <name> <time>", "Add a time the class runs (e.g. 9am).", "/class settime ");
        helpLine(sender, "/class &fdeltime <name> <time>", "Remove a class time.", "/class deltime ");
        helpLine(sender, "/class &freload", "Reload config.yml (settings and grades).", "/class reload");
    }

    private void Pager(CommandSender sender, int page, int max) {
        TextComponent line = new TextComponent(format("&8&m-----&r  "));
        if (page > 1) {
            line.addExtra(button("&6[&f◀&6]", "&7Previous page", "/class help " + (page - 1), ClickEvent.Action.RUN_COMMAND));
        } else {
            line.addExtra(new TextComponent(format("&8[◀]")));
        }
        line.addExtra(new TextComponent(format("  &7Page &e" + page + "&7/&e" + max + "  ")));
        if (page < max) {
            line.addExtra(button("&6[&f▶&6]", "&7Next page", "/class help " + (page + 1), ClickEvent.Action.RUN_COMMAND));
        } else {
            line.addExtra(new TextComponent(format("&8[▶]")));
        }
        line.addExtra(new TextComponent(format("  &8&m-----")));
        sender.spigot().sendMessage(line);
    }

    private void helpLine(CommandSender sender, String command, String description, String suggest) {
        ChatComponent msg = new ChatComponent("&6" + this.cmdstarter + "&7" + command);
        msg.addHoverMessage("&6" + command + "\n\n&7" + description);
        msg.addClickEvent(suggest, ClickEvent.Action.SUGGEST_COMMAND);
        sender.spigot().sendMessage(msg.getComponent());
    }

    private static TextComponent button(String label, String hover, String command, ClickEvent.Action action) {
        TextComponent b = new TextComponent(format(label));
        b.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(format(hover)).create()));
        b.setClickEvent(new ClickEvent(action, command));
        return b;
    }

    public void ClassList(CommandSender sender, Collection<Classroom> rooms) {
        if (rooms.isEmpty()) {
            sender.sendMessage(format(Prefix + "&7No classes have been created yet."));
            return;
        }
        sender.sendMessage(format("&6Classes &7(" + rooms.size() + "): &oClick a class to view it"));
        for (Classroom room : rooms) {
            String status = room.isInSession()
                    ? (room.isJoinable() ? "&aIn session" : "&eLocked")
                    : "&7Not running";
            TextComponent line = new TextComponent(format("&6" + this.cmdstarter + "&e" + room.getName()
                    + " &8(" + room.getStudents().size() + "/" + room.getCapacity() + ") " + status + "  "));
            line.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                    new ComponentBuilder(format("&7Click to view &e" + room.getName())).create()));
            line.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/class info " + room.getId()));
            if (room.isInSession() && room.isJoinable()) {
                line.addExtra(button("&8[&aJoin&8]", "&7Join this class now.",
                        "/class join " + room.getId(), ClickEvent.Action.RUN_COMMAND));
            }
            sender.spigot().sendMessage(line);
        }
    }

    public static void broadcastClassStart(Classroom room) {
        org.bukkit.configuration.file.FileConfiguration cfg = VenturaClassroom.getInstance().getConfig();
        boolean announce = cfg.getBoolean("announce-start", true);
        boolean title = cfg.getBoolean("title-on-start", true);
        if (!announce && !title) {
            return;
        }
        String name = room.getName();
        TextComponent line = new TextComponent(format("&8[&6VClasses&8] &e" + name + " &ais starting now! "));
        TextComponent join = new TextComponent(format("&8[&aClick to Join&8]"));
        join.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(format("&7Teleport in and join &e" + name)).create()));
        join.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/class join " + room.getId()));
        line.addExtra(join);

        for (Player p : Bukkit.getOnlinePlayers()) {
            if (announce) {
                p.spigot().sendMessage(line);
            }
            if (title) {
                p.sendTitle(format("&6" + name), format("&eClass is starting - click to join!"), 10, 70, 20);
            }
        }
    }

    public void PluginInformation(CommandSender sender) {
        TextComponent msg = new TextComponent(format("&a" + this.cmdstarter + "&b[Wiki] "));
        msg.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text(format("&6VenturaClasses Wiki Page \n\n&7Website URL: \n&6" + this.cmdstarter + "&e&nhttps://bookofventura.net/wiki"))}));
        msg.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://bookofventura.net/wiki"));
        TextComponent msg2 = new TextComponent(format("&6[VenturaClasses] "));
        msg2.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text(format("&6VenturaClasses Help Page \n\n&7Click Command: \n&6" + this.cmdstarter + "&e&n/class"))}));
        msg2.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/class"));
        TextComponent msg3 = new TextComponent(format("&f[GitHub] "));
        msg3.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT, new Content[]{new Text(format("&6VenturaClasses GitHub Page \n\n&7Website URL: \n&6" + this.cmdstarter + "&e&nhttps://github.com/Ash10744/VenturaClasses"))}));
        msg3.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Ash10744/VenturaClasses"));
        msg.addExtra(msg2);
        msg2.addExtra(msg3);
        sender.spigot().sendMessage(msg);
    }

    public void MCTitle(CommandSender sender) {
        String Version = this.Version();
        Libs.ChatComponent msg = new Libs.ChatComponent("&6VenturaClasses-" + Version + this.spacer + "&eHelp Menu" + this.spacer + "&6Author: &eAsh10744 ");
        msg.addHoverMessage("&7A classroom system for your server");
        sender.spigot().sendMessage(msg.getComponent());
    }
}