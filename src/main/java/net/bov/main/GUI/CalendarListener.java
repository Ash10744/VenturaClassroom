package net.bov.main.GUI;

import net.bov.main.Libs.Libs;
import net.bov.main.VenturaClassroom;
import net.bov.main.Classes.ClassManager;
import net.bov.main.Classes.Classroom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

public class CalendarListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof CalendarMenu)) {
            return;
        }
        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof CalendarMenu)) {
            return;
        }
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Player player = (Player) event.getWhoClicked();
        CalendarMenu menu = (CalendarMenu) event.getView().getTopInventory().getHolder();
        String id = menu.classAt(event.getRawSlot());
        if (id == null) {
            return;
        }
        ClassManager mgr = VenturaClassroom.getInstance().getClassManager();
        Classroom room = mgr.get(id);
        if (room == null) {
            return;
        }

        player.closeInventory();
        if (room.isInSession() && room.isJoinable()) {
            String result = mgr.join(player, room);
            if (result.equals("ok")) {
                player.sendMessage(Libs.format("&8[&6VClasses&8] &aYou joined &e" + room.getName() + "&a!"));
            } else if (result.equals("full")) {
                player.sendMessage(Libs.format("&8[&6VClasses&8] &cThat class is full."));
            } else if (result.equals("already")) {
                player.sendMessage(Libs.format("&8[&6VClasses&8] &7You are already in that class."));
            }
        } else {
            player.performCommand("class info " + room.getId());
        }
    }
}