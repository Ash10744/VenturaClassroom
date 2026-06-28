package net.bov.main.GUI;

import net.bov.main.Classes.ClassTime;
import net.bov.main.Classes.Classroom;
import net.bov.main.Classes.TimeUtil;
import net.bov.main.Libs.Libs;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class DayClassesMenu implements InventoryHolder {

    private final Inventory inventory;
    private final Map<Integer, String> slots = new HashMap<>();

    public DayClassesMenu(LocalDate date, Collection<Classroom> rooms) {
        DayOfWeek day = date.getDayOfWeek();
        String title = "&6" + TimeUtil.dayShort(day) + " " + date.getDayOfMonth();

        TreeMap<LocalTime, List<Classroom>> byTime = new TreeMap<>();
        for (Classroom room : rooms) {
            for (ClassTime ct : room.getTimes()) {
                if (ct.getDay() == day) {
                    byTime.computeIfAbsent(ct.getTime(), k -> new ArrayList<>()).add(room);
                }
            }
        }
        int count = 0;
        for (List<Classroom> v : byTime.values()) {
            count += v.size();
        }
        int size = Math.min(54, Math.max(9, ((count + 8) / 9) * 9));
        this.inventory = Bukkit.createInventory(this, size, Libs.format(title));

        int slot = 0;
        for (Map.Entry<LocalTime, List<Classroom>> e : byTime.entrySet()) {
            for (Classroom room : e.getValue()) {
                if (slot >= size) {
                    break;
                }
                this.inventory.setItem(slot, entry(room, e.getKey()));
                this.slots.put(slot, room.getId());
                slot++;
            }
        }
    }

    private ItemStack entry(Classroom room, LocalTime time) {
        ItemStack item = new ItemStack(room.isInSession() ? Material.LIME_CONCRETE : Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Libs.format("&6" + room.getName() + " &7- &e" + TimeUtil.formatClock(time)));
            List<String> lore = new ArrayList<>();
            lore.add(Libs.format("&7Students: &e" + room.getStudents().size() + "&7/&e" + room.getCapacity()));
            String status = room.isInSession()
                    ? (room.isJoinable() ? "&aIn session (open)" : "&eIn session (locked)")
                    : "&7Not running";
            lore.add(Libs.format("&7Status: " + status));
            lore.add(Libs.format("&8Click to view class info"));
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    public boolean isEmpty() {
        return this.slots.isEmpty();
    }

    public String classAt(int slot) {
        return this.slots.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}