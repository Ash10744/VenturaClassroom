package net.bov.main.Integrations;

import net.milkbowl.vault.economy.Economy;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.plugin.RegisteredServiceProvider;

public class EconomyHook {

    private Economy economy;

    public boolean setup() {
        if (Bukkit.getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = Bukkit.getServicesManager().getRegistration(Economy.class);
        if (rsp == null) {
            return false;
        }
        this.economy = rsp.getProvider();
        return this.economy != null;
    }

    public boolean available() {
        return this.economy != null;
    }

    public void deposit(OfflinePlayer player, double amount) {
        if (this.economy != null) {
            this.economy.depositPlayer(player, amount);
        }
    }

    public String format(double amount) {
        return this.economy == null ? String.valueOf(amount) : this.economy.format(amount);
    }
}