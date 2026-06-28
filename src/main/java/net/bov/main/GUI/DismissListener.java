package net.bov.main.GUI;

import net.bov.main.Classes.ClassManager;
import net.bov.main.Classes.Classroom;
import net.bov.main.Libs.Libs;
import net.bov.main.VenturaClassroom;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.util.UUID;

public class DismissListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof DismissMenu)) {
            return;
        }
        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof DismissMenu)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player staff = (Player) event.getWhoClicked();
        DismissMenu menu = (DismissMenu) event.getView().getTopInventory().getHolder();
        String grade = menu.gradeAt(event.getRawSlot());
        if (grade == null) {
            return;
        }

        ClassManager mgr = VenturaClassroom.getInstance().getClassManager();
        Classroom room = mgr.get(menu.classId());
        staff.closeInventory();
        if (room == null) {
            return;
        }
        UUID studentId = menu.student();
        if (!room.getStudents().contains(studentId)) {
            staff.sendMessage(Libs.format("&8[&6VClasses&8] &cThat student is no longer in the class."));
            return;
        }
        Player student = Bukkit.getPlayer(studentId);
        if (student != null && student.isOnline()) {
            mgr.applyGrade(student, room, grade);
            student.sendMessage(Libs.format("&8[&6VClasses&8] &eYou were dismissed from &6" + room.getName()
                    + " &ewith grade &6" + grade + "&e."));
        }
        room.getStudents().remove(studentId);
        room.getGrades().remove(studentId);
        staff.sendMessage(Libs.format("&8[&6VClasses&8] &aDismissed &e"
                + (student != null ? student.getName() : "student") + " &awith grade &6" + grade + "&a."));
    }
}