package net.bov.main.Classes;

import net.bov.main.Libs.Libs;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;

public class SubmissionListener implements Listener {

    @EventHandler
    public void onClick(InventoryClickEvent event) {
        if (!(event.getView().getTopInventory().getHolder() instanceof SubmissionsMenu)) {
            return;
        }
        event.setCancelled(true);

        if (event.getClickedInventory() == null
                || !(event.getClickedInventory().getHolder() instanceof SubmissionsMenu)) {
            return;
        }

        ItemStack clicked = event.getCurrentItem();
        if (clicked == null || clicked.getType() == org.bukkit.Material.AIR) {
            return;
        }
        if (event.getWhoClicked() instanceof Player) {
            Player p = (Player) event.getWhoClicked();
            p.getInventory().addItem(clicked.clone());
            p.sendMessage(Libs.format("&8[&6VClassroom&8] &aCopied that submission to your inventory."));
        }
    }
}