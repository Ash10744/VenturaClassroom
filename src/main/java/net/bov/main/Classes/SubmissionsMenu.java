package net.bov.main.Classes;

import net.bov.main.Libs.Libs;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryHolder;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * A read-through inventory of everything students have submitted to a class.
 * Implements InventoryHolder so the click listener can recognise this menu.
 * Each item is a copy of the student's submission with their name added as lore.
 */
public class SubmissionsMenu implements InventoryHolder {

    private final Inventory inventory;

    public SubmissionsMenu(Classroom room) {
        int count = room.getSubmissions().size();
        int size = Math.min(54, Math.max(9, ((count + 8) / 9) * 9));
        this.inventory = Bukkit.createInventory(this, size, Libs.format("&6" + trim(room.getName()) + " Submissions"));

        for (Map.Entry<UUID, ItemStack> entry : room.getSubmissions().entrySet()) {
            ItemStack item = entry.getValue().clone();
            ItemMeta meta = item.getItemMeta();
            if (meta != null) {
                List<String> lore = meta.hasLore() ? meta.getLore() : new ArrayList<>();
                lore.add(Libs.format("&7Submitted by &e" + nameOf(entry.getKey())));
                meta.setLore(lore);
                item.setItemMeta(meta);
            }
            this.inventory.addItem(item);
        }
    }

    private static String nameOf(UUID uuid) {
        OfflinePlayer p = Bukkit.getOfflinePlayer(uuid);
        return p.getName() == null ? "Unknown" : p.getName();
    }

    private static String trim(String s) {
        return s.length() > 20 ? s.substring(0, 20) : s;
    }

    @Override
    public Inventory getInventory() {
        return this.inventory;
    }
}