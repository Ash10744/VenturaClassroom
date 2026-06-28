package net.bov.main.Integrations;

import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import net.bov.main.VenturaClassroom;
import net.bov.main.Classes.ClassManager;
import net.bov.main.Classes.Classroom;
import net.bov.main.Classes.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;

public class VClassExpansion extends PlaceholderExpansion {

    private final VenturaClassroom plugin;

    public VClassExpansion(VenturaClassroom plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean persist() {
        return true;
    }

    @Override
    public boolean canRegister() {
        return true;
    }

    @Override
    public String getIdentifier() {
        return "venturaclasses";
    }

    @Override
    public String getAuthor() {
        return "Ash10744";
    }

    @Override
    public String getVersion() {
        return this.plugin.getDescription().getVersion();
    }

    @Override
    public String onRequest(OfflinePlayer player, String params) {
        ClassManager mgr = this.plugin.getClassManager();
        if (mgr == null) {
            return "";
        }
        String p = params.toLowerCase();

        if (p.equals("count")) {
            return String.valueOf(mgr.all().size());
        }
        if (p.equals("next")) {
            Classroom room = mgr.getNextClass();
            return room == null ? "None" : room.getName();
        }
        if (p.equals("next_time")) {
            Classroom room = mgr.getNextClass();
            if (room == null) {
                return "None";
            }
            int wait = mgr.waitUntilNext(room);
            return wait < 0 ? "None" : TimeUtil.prettyDuration(wait);
        }
        if (p.startsWith("status_")) {
            Classroom room = mgr.get(p.substring(7));
            if (room == null) {
                return "";
            }
            return room.isInSession() ? (room.isJoinable() ? "Open" : "Locked") : "Not running";
        }
        if (p.startsWith("students_")) {
            Classroom room = mgr.get(p.substring(9));
            return room == null ? "" : (room.getStudents().size() + "/" + room.getCapacity());
        }
        if (p.startsWith("teacher_")) {
            Classroom room = mgr.get(p.substring(8));
            if (room == null || room.getTeacher() == null) {
                return "";
            }
            OfflinePlayer t = Bukkit.getOfflinePlayer(room.getTeacher());
            return t.getName() == null ? "" : t.getName();
        }
        return null;
    }
}