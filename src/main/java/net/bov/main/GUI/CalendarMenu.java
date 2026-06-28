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
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CalendarMenu implements InventoryHolder {

    private static final DayOfWeek[] WEEK = {
            DayOfWeek.MONDAY, DayOfWeek.TUESDAY, DayOfWeek.WEDNESDAY, DayOfWeek.THURSDAY,
            DayOfWeek.FRIDAY, DayOfWeek.SATURDAY, DayOfWeek.SUNDAY
    };

    private final Inventory inventory;
    private final Map<Integer, String> slots = new HashMap<>();

    public CalendarMenu(Collection<Classroom> rooms) {
        this.inventory = Bukkit.createInventory(this, 54, Libs.format("&6Class Calendar &7(Mon - Sun)"));
        DayOfWeek today = LocalDate.now().getDayOfWeek();

        for (int col = 0; col < WEEK.length; col++) {
            DayOfWeek day = WEEK[col];
            boolean isToday = day == today;
            this.inventory.setItem(col, header(day, isToday));

            TreeMap<LocalTime, List<Classroom>> byTime = new TreeMap<>();
            for (Classroom room : rooms) {
                for (ClassTime ct : room.getTimes()) {
                    if (ct.getDay() == day) {
                        byTime.computeIfAbsent(ct.getTime(), k -> new ArrayList<>()).add(room);
                    }
                }
            }

            int row = 1;
            outer:
            for (Map.Entry<LocalTime, List<Classroom>> e : byTime.entrySet()) {
                for (Classroom room : e.getValue()) {
                    if (row > 5) {
                        break outer;
                    }
                    int slot = row * 9 + col;
                    this.inventory.setItem(slot, entry(room, e.getKey()));
                    this.slots.put(slot, room.getId());
                    row++;
                }
            }
        }
    }

    private ItemStack header(DayOfWeek day, boolean today) {
        ItemStack item = new ItemStack(today ? Material.LIME_STAINED_GLASS_PANE : Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Libs.format((today ? "&a&l" : "&7&l") + fullDay(day) + (today ? " &r&7(today)" : "")));
            meta.setLore(Collections.singletonList(Libs.format("&8Classes for this day below")));
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack entry(Classroom room, LocalTime time) {
        ItemStack item = new ItemStack(room.isInSession() ? Material.LIME_CONCRETE : Material.BOOK);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Libs.format("&6" + room.getName() + " &7- &e" + TimeUtil.formatClock(time)));
            List<String> lore = new ArrayList<>();
            lore.add(Libs.format("&7Students: &e" + room.getStudents().size() + "&7/&e" + room.getCapacity()));
            if (room.isInSession()) {
                lore.add(Libs.format(room.isJoinable() ? "&aIn session - click to join" : "&eIn session (locked)"));
            } else {
                lore.add(Libs.format("&8Click to view"));
            }
            meta.setLore(lore);
            item.setItemMeta(meta);
        }
        return item;
    }

    private String fullDay(DayOfWeek day) {
        String s = day.name();
        return s.charAt(0) + s.substring(1).toLowerCase();
    }

    public String classAt(int slot) {
        return this.slots.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}