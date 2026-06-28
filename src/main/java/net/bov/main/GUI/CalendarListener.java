package net.bov.main.GUI;

import net.bov.main.Classes.Classroom;
import net.bov.main.VenturaClassroom;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;

import java.time.LocalDate;
import java.util.Collection;

public class CalendarListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player)) {
            return;
        }
        Object holder = event.getView().getTopInventory().getHolder();
        Player player = (Player) event.getWhoClicked();
        Collection<Classroom> rooms = VenturaClassroom.getInstance().getClassManager().all();

        if (holder instanceof CalendarMenu) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null
                    || !(event.getClickedInventory().getHolder() instanceof CalendarMenu)) {
                return;
            }
            CalendarMenu menu = (CalendarMenu) holder;
            int slot = event.getRawSlot();
            if (menu.isPrev(slot)) {
                player.openInventory(new CalendarMenu(menu.getMonth().minusMonths(1), rooms).getInventory());
                return;
            }
            if (menu.isNext(slot)) {
                player.openInventory(new CalendarMenu(menu.getMonth().plusMonths(1), rooms).getInventory());
                return;
            }
            LocalDate date = menu.dateAt(slot);
            if (date != null) {
                DayClassesMenu day = new DayClassesMenu(date, rooms);
                if (day.isEmpty()) {
                    return;
                }
                player.openInventory(day.getInventory());
            }
            return;
        }

        if (holder instanceof DayClassesMenu) {
            event.setCancelled(true);
            if (event.getClickedInventory() == null
                    || !(event.getClickedInventory().getHolder() instanceof DayClassesMenu)) {
                return;
            }
            DayClassesMenu menu = (DayClassesMenu) holder;
            String id = menu.classAt(event.getRawSlot());
            if (id == null) {
                return;
            }
            player.closeInventory();
            player.performCommand("class info " + id);
        }
    }
}