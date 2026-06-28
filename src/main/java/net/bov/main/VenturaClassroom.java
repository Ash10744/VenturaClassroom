package net.bov.main;

import net.bov.main.Commands.MainCommand;
import net.bov.main.Classes.ClassManager;
import net.bov.main.Integrations.EconomyHook;
import net.bov.main.Integrations.VClassExpansion;
import org.bukkit.command.PluginCommand;
import org.bukkit.plugin.java.JavaPlugin;

public final class VenturaClassroom extends JavaPlugin {

    private static VenturaClassroom instance;
    private final MainCommand mainCommand = new MainCommand();
    private ClassManager classManager;
    private EconomyHook economyHook;
    private net.bov.main.Setup.SetupWizard setupWizard;

    public static VenturaClassroom getInstance() {
        return instance;
    }

    public ClassManager getClassManager() {
        return this.classManager;
    }

    public EconomyHook getEconomyHook() {
        return this.economyHook;
    }

    public net.bov.main.Setup.SetupWizard getSetupWizard() {
        return this.setupWizard;
    }

    @Override
    public void onEnable() {
        instance = this;

        saveDefaultConfig();

        this.classManager = new ClassManager(this);
        this.classManager.load();
        this.classManager.start();

        this.setupWizard = new net.bov.main.Setup.SetupWizard(this);

        getServer().getPluginManager().registerEvents(new net.bov.main.Classes.SubmissionListener(), this);
        getServer().getPluginManager().registerEvents(new net.bov.main.GUI.CalendarListener(), this);
        getServer().getPluginManager().registerEvents(new net.bov.main.GUI.DismissListener(), this);
        getServer().getPluginManager().registerEvents(new net.bov.main.Setup.SetupListener(), this);

        if (getServer().getPluginManager().getPlugin("Vault") != null) {
            EconomyHook hook = new EconomyHook();
            if (hook.setup()) {
                this.economyHook = hook;
                getLogger().info("Hooked into Vault economy.");
            }
        }

        if (getServer().getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new VClassExpansion(this).register();
            getLogger().info("Hooked into PlaceholderAPI.");
        }

        PluginCommand cmd = getCommand("class");
        if (cmd != null) {
            cmd.setExecutor(this.mainCommand);
            cmd.setTabCompleter(this.mainCommand);
        } else {
            getLogger().severe("Command 'class' is not defined in plugin.yml!");
        }

        getLogger().info("VenturaClassroom enabled.");
    }

    @Override
    public void onDisable() {
        if (this.classManager != null) {
            this.classManager.stop();
            this.classManager.save();
        }
    }
}