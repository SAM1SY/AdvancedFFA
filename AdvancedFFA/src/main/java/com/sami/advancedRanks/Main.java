package com.sami.advancedRanks;

import com.sami.advancedRanks.commands.RankCommand;
import com.sami.advancedRanks.commands.RankTabCompleter;
import com.sami.advancedRanks.listeners.ChatListener;
import com.sami.advancedRanks.listeners.onJoin;
import com.sami.advancedRanks.managers.DataManager;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private DataManager dataManager;
    private DataManager permsManager;

    @Override
    public void onEnable() {
        saveDefaultConfig(); // config.yml
        this.dataManager = new DataManager(this, "ranks.yml");
        this.permsManager = new DataManager(this, "perms.yml");

        if (getCommand("rank") != null) {
            getCommand("rank").setExecutor(new RankCommand(this, dataManager));
            getCommand("rank").setTabCompleter(new RankTabCompleter());
        }

        getServer().getPluginManager().registerEvents(new onJoin(this, dataManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, dataManager), this);

        getLogger().info("AdvancedRanks System Loaded Successfully!");
    }

    @Override
    public void onDisable() {
        if (dataManager != null) dataManager.saveConfig();
        if (permsManager != null) permsManager.saveConfig();
    }

    public DataManager getPermsManager() { return permsManager; }
    public DataManager getDataManager() { return dataManager; }
}