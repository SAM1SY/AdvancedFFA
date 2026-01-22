package com.sami.advancedFFA;

import com.sami.advancedFFA.commands.*;
import com.sami.advancedFFA.listeners.*;
import com.sami.advancedFFA.managers.*;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.plugin.java.JavaPlugin;

public final class Main extends JavaPlugin {

    private PlayerDataManager playerDataManager;
    private DataManager dataManager;
    private DataManager permsManager;
    private StatsManager statsManager;
    private NPCManager npcManager;
    private KitManager kitManager;

    private CombatListener combatListener;

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();

        if (!Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            getLogger().severe("Citizens 2.0 not found or disabled! NPCs will not work.");

        }

        loadRequiredWorlds();

        this.playerDataManager = new PlayerDataManager(this);
        this.dataManager = new DataManager(this, "ranks.yml");
        this.permsManager = new DataManager(this, "perms.yml");
        this.statsManager = new StatsManager(this);
        this.npcManager = new NPCManager(this);
        this.kitManager = new KitManager(this);

        this.combatListener = new CombatListener(this);

        registerCommands();
        registerListeners();

        LeaderboardUpdating();
        startNpcTasks();

        getLogger().info("========================================");
        getLogger().info("   AdvancedFFA Rank & Level System      ");
        getLogger().info("           Status: ENABLED              ");
        getLogger().info("========================================");
    }

    private void loadRequiredWorlds() {
        if (Bukkit.getWorld("arena") == null) {
            getLogger().info("Creating/Loading world: 'arena'...");
            Bukkit.createWorld(new WorldCreator("arena"));
        }

        if (Bukkit.getWorld("spawn") == null) {
            getLogger().info("Creating/Loading world: 'spawn'...");
            Bukkit.createWorld(new WorldCreator("spawn"));
        }
    }

    private void registerCommands() {
        if (getCommand("rank") != null) {
            getCommand("rank").setExecutor(new RankCommand(this, dataManager));
            getCommand("rank").setTabCompleter(new RankTabCompleter());
        }
        if (getCommand("kit") != null) {
            getCommand("kit").setExecutor(new KitCommand(this));
        }
        if (getCommand("spawn") != null) {
            getCommand("spawn").setExecutor(new SpawnCommand(this));
        }
    }

    private void registerListeners() {
        getServer().getPluginManager().registerEvents(new StatsListener(this), this);
        getServer().getPluginManager().registerEvents(new RankListener(this, dataManager), this);
        getServer().getPluginManager().registerEvents(new ChatListener(this, dataManager), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);

        getServer().getPluginManager().registerEvents(this.combatListener, this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this), this);

        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new LeaderboardListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(this), this);
    }

    private void startNpcTasks() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
                this.npcManager.createStandardArenaNPC();
            }
        }, 100L);
    }

    private void LeaderboardUpdating() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            this.statsManager.updateGlobalLeaderboards();
            getLogger().info("Global leaderboards have been refreshed.");
        }, 20L, 1200L);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);
        if (dataManager != null) dataManager.saveConfig();
        if (permsManager != null) permsManager.saveConfig();

        if (statsManager != null) {
            getServer().getOnlinePlayers().forEach(player -> {
                statsManager.saveAndUnloadPlayer(player);
            });
        }

        getLogger().info("AdvancedFFA: All data has been securely saved. Plugin disabled.");
    }

    // --- All Getters ---
    public PlayerDataManager getPlayerDataManager() { return playerDataManager; }
    public DataManager getDataManager() { return dataManager; } // Added this getter
    public DataManager getPermsManager() { return permsManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public NPCManager getNpcManager() { return npcManager; }
    public KitManager getKitManager() { return kitManager; }
    public CombatListener getCombatListener() { return combatListener; }
}