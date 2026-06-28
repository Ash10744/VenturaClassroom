package net.bov.main.GUI;

import net.bov.main.Classes.Classroom;
import net.bov.main.Libs.Libs;
import net.bov.main.VenturaClassroom;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class DismissMenu implements InventoryHolder {

    private final Inventory inventory;
    private final String classId;
    private final UUID student;
    private final Map<Integer, String> gradeSlots = new HashMap<>();

    public DismissMenu(Classroom room, UUID student, String studentName) {
        this.classId = room.getId();
        this.student = student;

        List<String> grades = VenturaClassroom.getInstance().getClassManager().gradeKeys();
        int size = Math.min(54, Math.max(9, ((grades.size() + 8) / 9) * 9));
        this.inventory = Bukkit.createInventory(this, size, Libs.format("&6Grade: " + trim(studentName)));

        FileConfiguration cfg = VenturaClassroom.getInstance().getConfig();
        int slot = 0;
        for (String g : grades) {
            String base = "grades." + g;
            ItemStack item = new ItemStack(Material.PAPER);
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                meta.setDisplayName(Libs.format("&6Grade " + g));
                List<String> lore = new ArrayList<>();
                double money = cfg.getDouble(base + ".money", 0);
                int xp = cfg.getInt(base + ".xp", 0);
                int cmds = cfg.getStringList(base + ".commands").size();
                if (money > 0) {
                    lore.add(Libs.format("&7Money: &a" + money));
                }
                if (xp > 0) {
                    lore.add(Libs.format("&7XP: &a" + xp));
                }
                if (cmds > 0) {
                    lore.add(Libs.format("&7Runs &a" + cmds + " &7command(s)"));
                }
                lore.add(Libs.format("&8Click to give this grade & dismiss"));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            this.inventory.setItem(slot, item);
            this.gradeSlots.put(slot, g);
            slot++;
        }
    }

    private static String trim(String s) {
        return s.length() > 22 ? s.substring(0, 22) : s;
    }

    public String classId() {
        return this.classId;
    }

    public UUID student() {
        return this.student;
    }

    public String gradeAt(int slot) {
        return this.gradeSlots.get(slot);
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}