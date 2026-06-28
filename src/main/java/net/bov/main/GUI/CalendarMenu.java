package net.bov.main.GUI;

import net.bov.main.Libs.Libs;
import net.bov.main.Classes.Classroom;
import net.bov.main.Classes.TimeUtil;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class CalendarMenu implements InventoryHolder {

    private final Inventory inventory;
    private final List<String> slots = new ArrayList<>();

    public CalendarMenu(Collection<Classroom> rooms) {
        int count = rooms.size();
        int size = Math.min(54, Math.max(9, ((count + 8) / 9) * 9));
        this.inventory = Bukkit.createInventory(this, size, Libs.format("&6Class Calendar"));

        int slot = 0;
        for (Classroom room : rooms) {
            if (slot >= size) {
                break;
            }
            this.inventory.setItem(slot, icon(room));
            this.slots.add(room.getId());
            slot++;
        }
    }

    private ItemStack icon(Classroom room) {
        ItemStack item = new ItemStack(room.isInSession() ? Material.CLOCK : Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Libs.format("&6" + room.getName()));
            List<String> lore = new ArrayList<>();
            lore.add(Libs.format("&7Students: &e" + room.getStudents().size() + "&7/&e" + room.getCapacity()));
            if (room.getTimes().isEmpty()) {
                lore.add(Libs.format("&7Times: &cnone set"));
            } else {
                StringBuilder sb = new StringBuilder();
                for (int t : room.getTimes()) {
                    sb.append(sb.length() > 0 ? "&7, &e" : "&e").append(TimeUtil.ticksToClock(t));
                }
                lore.add(Libs.format("&7Times: " + sb));
            }
            if (room.isInSession()) {
                lore.add(Libs.format(room.isJoinable() ? "&aIn session - click to join" : "&eIn session (locked)"));
            } else {
                lore.add(Libs.format("&7Not running - click to view"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public String classAt(int slot) {
        return slot >= 0 && slot < this.slots.size() ? this.slots.get(slot) : null;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}