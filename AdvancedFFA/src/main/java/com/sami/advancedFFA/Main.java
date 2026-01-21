package com.sami.advancedFFA;

import com.sami.advancedFFA.commands.NPCCommand;
import com.sami.advancedFFA.commands.RankCommand;
import com.sami.advancedFFA.commands.RankTabCompleter;
import com.sami.advancedFFA.listeners.*;
import com.sami.advancedFFA.managers.DataManager;
import com.sami.advancedFFA.managers.DatabaseManager;
import com.sami.advancedFFA.managers.NPCManager;
import com.sami.advancedFFA.managers.StatsManager;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private DataManager dataManager;
    private DataManager permsManager;
    private DatabaseManager databaseManager;
    private StatsManager statsManager;
    private NPCManager npcManager;

    @Override
    public void onEnable() {
        // 1. Setup Folder and Config
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();

        loadRequiredWorlds();

        this.dataManager = new DataManager(this, "ranks.yml");
        this.permsManager = new DataManager(this, "perms.yml");
        this.databaseManager = new DatabaseManager(this);
        this.statsManager = new StatsManager(this);
        this.npcManager = new NPCManager(this);

        registerCommands();
        registerListeners();

        getLogger().info("========================================");
        getLogger().info("   AdvancedFFA Rank & Level System      ");
        getLogger().info("           Status: ENABLED              ");
        getLogger().info("========================================");
    }

    private void loadRequiredWorlds() {

        if (Bukkit.getWorld("arena") == null) {
            getLogger().info("Loading world: 'arena'...");
            Bukkit.createWorld(new WorldCreator("arena"));
        }

        if (Bukkit.getWorld("spawn") == null) {
            getLogger().info("Loading world: 'spawn'...");
            Bukkit.createWorld(new WorldCreator("spawn"));
        }
    }

    private void registerCommands() {
        if (getCommand("rank") != null) {
            getCommand("rank").setExecutor(new RankCommand(this, dataManager));
            getCommand("rank").setTabCompleter(new RankTabCompleter());
        }
        if (getCommand("spawnnpc") != null) {
            getCommand("spawnnpc").setExecutor(new NPCCommand(this));
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new RankListener(this, dataManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, dataManager), this);

        getServer().getPluginManager().registerEvents(new StatsListener(this), this);

        getServer().getPluginManager().registerEvents(new CombatListener(this), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this), this);

        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
    }

    @Override
    public void onDisable() {
        if (dataManager != null) dataManager.saveConfig();
        if (permsManager != null) permsManager.saveConfig();

        if (statsManager != null) {
            getServer().getOnlinePlayers().forEach(player -> {
                statsManager.saveAndUnloadPlayer(player);
            });
        }

        getLogger().info("AdvancedFFA data saved and plugin disabled.");
    }

    // Getters
    public DataManager getDataManager() { return dataManager; }
    public DataManager getPermsManager() { return permsManager; }
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public NPCManager getNpcManager() { return npcManager; }
}