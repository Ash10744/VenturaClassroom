package net.bov.main.Libs;

import net.bov.main.VenturaClassroom;
import net.bov.main.Classes.Classroom;
import net.md_5.bungee.api.chat.ClickEvent;
import net.md_5.bungee.api.chat.ComponentBuilder;
import net.md_5.bungee.api.chat.HoverEvent;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Collection;

public class Libs {
    public static String format(String message) {
        return ChatColor.translateAlternateColorCodes('&', message);
    }

    public String Prefix = "&8[&6VClassroom&8] ";
    public String cmdstarter = format("◊ ");
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

    private void blank(CommandSender sender) {
        sender.sendMessage("");
    }

    private void line(CommandSender sender, String text) {
        sender.sendMessage(format(text));
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
        ChatComponent header = new ChatComponent("&f&nAvailable Commands:&r &7[] = Required, &7<> = Optional\n\n&7&oHover / Click for Additional Information");
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

        line(sender, CommandDivider);
        MCTitle(sender);
        blank(sender);
        FormatHelp(sender);
        blank(sender);

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

        blank(sender);
        line(sender, CommandDivider);
        Pager(sender, page, max);
        line(sender, CommandDivider);
        PluginInformation(sender);
        line(sender, CommandDivider);
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
        helpLine(sender, "/class &fstart <name>", "Announce the class and open it for joining.", "/class start ");
        helpLine(sender, "/class &fbegin <name>", "Teleport everyone who joined in and lock the class.", "/class begin ");
        helpLine(sender, "/class &fend <name>", "End your class and dismiss the students.", "/class end ");
        helpLine(sender, "/class &fwarp <name>", "Teleport yourself to the class location.", "/class warp ");
        helpLine(sender, "/class &fgrade <name> <player> <grade>", "Pre-set a student's grade, applied when you dismiss the class.", "/class grade ");
        helpLine(sender, "/class &fdismiss <player>", "Open a grade menu to grade & dismiss one student.", "/class dismiss ");
        helpLine(sender, "/class &fdismiss all <name>", "Dismiss everyone left, applying any grades you set.", "/class dismiss all ");
        helpLine(sender, "/class &flock <name>", "Stop new students joining (those inside stay).", "/class lock ");
        helpLine(sender, "/class &funlock <name>", "Allow students to join again.", "/class unlock ");
        helpLine(sender, "/class &fcapacity <name> <n>", "Change how many students can join.", "/class capacity ");
        helpLine(sender, "/class &fgiveitem <name>", "Give the item in your hand to every student.", "/class giveitem ");
        helpLine(sender, "/class &fsubmissions <name>", "Open a menu of everything students submitted.", "/class submissions ");
    }

    private void helpAdmin(CommandSender sender) {
        helpLine(sender, "/class &fsetup", "Guided step-by-step setup for a new class.", "/class setup");
        helpLine(sender, "/class &fcreate <name...>", "Create a new classroom (name can have spaces).", "/class create ");
        helpLine(sender, "/class &fdelete <name>", "Delete a classroom.", "/class delete ");
        helpLine(sender, "/class &fsetname <name> <display...>", "Set the display name of a class.", "/class setname ");
        helpLine(sender, "/class &fsetteacher <name> <player>", "Assign the teacher.", "/class setteacher ");
        helpLine(sender, "/class &faddsub <name> <player>", "Add a sub-teacher (assistant).", "/class addsub ");
        helpLine(sender, "/class &fremovesub <name> <player>", "Remove a sub-teacher.", "/class removesub ");
        helpLine(sender, "/class &fsetwarp <name>", "Set the class warp to where you stand.", "/class setwarp ");
        helpLine(sender, "/class &fsettime <name> <day> <time>", "Add a day & time, or 'every <interval>' to repeat (e.g. every 1h).", "/class settime ");
        helpLine(sender, "/class &fdeltime <name> <day> <time>", "Remove a day & time, or 'every' to stop repeating.", "/class deltime ");
        helpLine(sender, "/class &fsetduration <name> <length>", "Set how long a class runs (e.g. 1h, 90m, 45s).", "/class setduration ");
        helpLine(sender, "/class &freload", "Reload config.yml (settings and grades).", "/class reload");
    }

    private void Pager(CommandSender sender, int page, int max) {
        TextComponent row = new TextComponent("");

        TextComponent prev;
        if (page > 1) {
            prev = new TextComponent(format("&6" + this.cmdstarter + "\u00ab&e Prev"));
            prev.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/class help " + (page - 1)));
        } else {
            prev = new TextComponent(format("&8" + this.cmdstarter + "\u00ab Prev"));
        }

        TextComponent sep = new TextComponent(format("&8  |  "));

        TextComponent next;
        if (page < max) {
            next = new TextComponent(format("&eNext &6\u00bb"));
            next.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/class help " + (page + 1)));
        } else {
            next = new TextComponent(format("&8Next \u00bb"));
        }

        row.addExtra(prev);
        row.addExtra(sep);
        row.addExtra(next);
        sender.spigot().sendMessage(row);
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
        TextComponent line = new TextComponent(format("&8[&6VClassroom&8] &e" + name + " &ais starting now! "));
        TextComponent join = new TextComponent(format("&8[&aClick to Join&8]"));
        join.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(format("&7Join &e" + name + " &7- you'll be brought in when it begins")).create()));
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
        TextComponent wiki = new TextComponent(format("&a" + this.cmdstarter + "&b[Spigot] "));
        wiki.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(format("&6VenturaClassroom Wiki \n\n&7Website URL: \n&6" + this.cmdstarter + "&e&nhttps://www.spigotmc.org/resources/authors/ash10744.213894/")).create()));
        wiki.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://www.spigotmc.org/resources/authors/ash10744.213894/"));

        TextComponent main = new TextComponent(format("&6[VenturaClassroom] "));
        main.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(format("&6Open the Help Menu \n\n&7Click Command: \n&6" + this.cmdstarter + "&e&n/class help")).create()));
        main.setClickEvent(new ClickEvent(ClickEvent.Action.RUN_COMMAND, "/class help"));

        TextComponent discord = new TextComponent(format("&e[Discord] "));
        discord.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(format("&6Join the Discord \n\n&7Website URL: \n&6" + this.cmdstarter + "&e&nhttps://discord.gg/n3RuqxNZxg")).create()));
        discord.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://discord.gg/n3RuqxNZxg"));

        TextComponent github = new TextComponent(format("&f[GitHub] "));
        github.setHoverEvent(new HoverEvent(net.md_5.bungee.api.chat.HoverEvent.Action.SHOW_TEXT,
                new ComponentBuilder(format("&6VenturaClassroom GitHub \n\n&7Website URL: \n&6" + this.cmdstarter + "&e&nhttps://github.com/Ash10744/VenturaClassroom")).create()));
        github.setClickEvent(new ClickEvent(ClickEvent.Action.OPEN_URL, "https://github.com/Ash10744/VenturaClassroom"));

        wiki.addExtra(main);
        main.addExtra(discord);
        discord.addExtra(github);
        sender.spigot().sendMessage(wiki);
    }

    public void MCTitle(CommandSender sender) {
        Libs.ChatComponent msg = new Libs.ChatComponent("&6VenturaClassroom-" + this.Version() + " &8- &eHelp Menu &8- &6Author: &eAsh10744");
        msg.addHoverMessage("&7A classroom system for your server");
        sender.spigot().sendMessage(msg.getComponent());
    }
}