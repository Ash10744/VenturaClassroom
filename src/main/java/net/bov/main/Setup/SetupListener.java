package net.bov.main.Setup;

import net.bov.main.VenturaClassroom;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

public class SetupListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST)
    public void onChat(AsyncPlayerChatEvent event) {
        final Player player = event.getPlayer();
        SetupWizard wizard = VenturaClassroom.getInstance().getSetupWizard();
        if (!wizard.isAwaitingChat(player)) {
            return;
        }
        event.setCancelled(true);
        final String message = event.getMessage();
        Bukkit.getScheduler().runTask(VenturaClassroom.getInstance(), new Runnable() {
            @Override
            public void run() {
                wizard.handleChat(player, message);
            }
        });
    }
}