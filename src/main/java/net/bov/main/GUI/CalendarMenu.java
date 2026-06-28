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
import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class CalendarMenu implements InventoryHolder {

    private static final String[] HEADERS = {"Mon", "Tue", "Wed", "Thu", "Fri", "Sat", "Sun"};
    private static final String[] MONTHS = {"January", "February", "March", "April", "May", "June",
            "July", "August", "September", "October", "November", "December"};

    private final Inventory inventory;
    private final YearMonth month;
    private final Map<Integer, LocalDate> daySlots = new HashMap<>();
    private final int prevSlot = 7;
    private final int nextSlot = 8;

    public CalendarMenu(YearMonth month, Collection<Classroom> rooms) {
        this.month = month;
        String title = "&6" + MONTHS[month.getMonthValue() - 1] + " " + month.getYear();
        this.inventory = Bukkit.createInventory(this, 54, Libs.format(title));

        for (int i = 0; i < 7; i++) {
            ItemStack head = pane(Material.GRAY_STAINED_GLASS_PANE, "&7&l" + HEADERS[i], null);
            this.inventory.setItem(i, head);
        }
        this.inventory.setItem(this.prevSlot, nav(Material.ARROW, "&e\u00ab " + MONTHS[month.minusMonths(1).getMonthValue() - 1],
                "&7Previous month"));
        this.inventory.setItem(this.nextSlot, nav(Material.ARROW, "&e" + MONTHS[month.plusMonths(1).getMonthValue() - 1] + " \u00bb",
                "&7Next month"));

        LocalDate first = month.atDay(1);
        int lead = first.getDayOfWeek().getValue() - 1;
        int days = month.lengthOfMonth();
        LocalDate today = LocalDate.now();

        for (int d = 1; d <= days; d++) {
            LocalDate date = month.atDay(d);
            int slot = 9 + lead + (d - 1);
            if (slot >= 54) {
                break;
            }
            List<String> classes = classesOn(date.getDayOfWeek(), rooms);
            boolean isToday = date.equals(today);
            Material mat = isToday ? Material.LIME_STAINED_GLASS_PANE
                    : (classes.isEmpty() ? Material.WHITE_STAINED_GLASS_PANE : Material.YELLOW_STAINED_GLASS_PANE);
            String name = (isToday ? "&a&l" : (classes.isEmpty() ? "&7" : "&e&l"))
                    + TimeUtil.dayShort(date.getDayOfWeek()) + " " + d + (isToday ? " &r&7(today)" : "");
            List<String> lore = new ArrayList<>();
            if (classes.isEmpty()) {
                lore.add(Libs.format("&8No classes this day"));
            } else {
                lore.addAll(classes);
                lore.add(Libs.format("&8Click to open this day"));
            }
            this.inventory.setItem(slot, pane(mat, name, lore));
            this.daySlots.put(slot, date);
        }
    }

    private List<String> classesOn(DayOfWeek day, Collection<Classroom> rooms) {
        TreeMap<LocalTime, List<String>> byTime = new TreeMap<>();
        for (Classroom room : rooms) {
            for (ClassTime ct : room.getTimes()) {
                if (ct.getDay() == day) {
                    byTime.computeIfAbsent(ct.getTime(), k -> new ArrayList<>())
                            .add(Libs.format("&e" + room.getName() + " &7- &f" + TimeUtil.formatClock(ct.getTime())));
                }
            }
        }
        List<String> out = new ArrayList<>();
        for (List<String> v : byTime.values()) {
            out.addAll(v);
        }
        return out;
    }

    private ItemStack pane(Material mat, String name, List<String> lore) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName(Libs.format(name));
            if (lore != null) {
                meta.setLore(lore);
            }
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack nav(Material mat, String name, String hover) {
        List<String> lore = new ArrayList<>();
        lore.add(Libs.format(hover));
        return pane(mat, name, lore);
    }

    public YearMonth getMonth() {
        return this.month;
    }

    public boolean isPrev(int slot) {
        return slot == this.prevSlot;
    }

    public boolean isNext(int slot) {
        return slot == this.nextSlot;
    }

    public LocalDate dateAt(int slot) {
        return this.daySlots.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}