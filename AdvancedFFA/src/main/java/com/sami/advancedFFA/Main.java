package com.sami.advancedFFA;

import com.sami.advancedFFA.commands.*;
import com.sami.advancedFFA.listeners.*;
import com.sami.advancedFFA.managers.*;
import com.sami.advancedFFA.menus.SettingsMenu;
import com.sami.advancedFFA.traits.GuardTrait;
import net.citizensnpcs.api.CitizensAPI;
import net.citizensnpcs.api.trait.TraitInfo;
import org.bukkit.Bukkit;
import org.bukkit.WorldCreator;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.UUID;

public final class Main extends JavaPlugin {

    private DatabaseManager databaseManager;
    private DataManager permsManager;

    private StatsManager statsManager;
    private NPCManager npcManager;
    private KitManager kitManager;
    private CombatListener combatListener;
    private LeaderboardManager leaderboardManager;
    private NametagManager nametagManager;
    private SettingsMenu settingsMenu;
    private KitCommand kitCommand;

    private final HashMap<UUID, UUID> lastMessaged = new HashMap<>();

    @Override
    public void onEnable() {
        if (!getDataFolder().exists()) {
            getDataFolder().mkdirs();
        }
        saveDefaultConfig();

        this.databaseManager = new DatabaseManager(this);

        if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
            CitizensAPI.getTraitFactory().registerTrait(
                    TraitInfo.create(GuardTrait.class)
            );
            getLogger().info("GuardTrait registered successfully!");
        } else {
            getLogger().severe("Citizens 2.0 not found! NPCs will not work correctly.");
        }

        if (Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) {
            this.leaderboardManager = new LeaderboardManager(this);
            getLogger().info("DecentHolograms found! Leaderboards initialized.");
        } else {
            getLogger().severe("DecentHolograms not found! Disabling leaderboards.");
        }

        if (Bukkit.getPluginManager().getPlugin("PlaceholderAPI") != null) {
            new com.sami.advancedFFA.models.FFAPlaceholders(this).register();
            getLogger().info("PlaceholderAPI expansion registered!");
        }

        loadRequiredWorlds();

        this.permsManager = new DataManager(this, "perms.yml");

        this.statsManager = new StatsManager(this);
        this.npcManager = new NPCManager(this);
        this.kitManager = new KitManager(this);
        this.combatListener = new CombatListener(this);
        this.nametagManager = new NametagManager(this);
        this.settingsMenu = new SettingsMenu(this);
        this.kitCommand = new KitCommand(this);

        registerCommands();
        registerListeners();


        startAutoSaveTask();
        startLeaderboardTask();
        startNpcTasks();

        getLogger().info("========================================");
        getLogger().info("   AdvancedFFA SQLite System            ");
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

    private void startAutoSaveTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (statsManager == null) return;

            int count = 0;
            for (Player player : Bukkit.getOnlinePlayers()) {
                statsManager.syncRanksToDatabase(player.getUniqueId(), player.getName());
                count++;
            }

            if (count > 0) {
                getLogger().info("[Auto-Save] Synchronized stats for " + count + " players.");
            }
        }, 6000L, 6000L);
    }

    public Player getLastMessaged(Player recipient) {
        UUID senderUUID = lastMessaged.get(recipient.getUniqueId());
        if (senderUUID == null) return null;
        return Bukkit.getPlayer(senderUUID);
    }

    public void setLastMessaged(Player recipient, Player sender) {
        lastMessaged.put(recipient.getUniqueId(), sender.getUniqueId());
    }

    private void registerCommands() {
        if (getCommand("rank") != null) {
            getCommand("rank").setExecutor(new RankCommand(this));
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
        getServer().getPluginManager().registerEvents(new RankListener(this), this);
        getServer().getPluginManager().registerEvents(new JoinListener(this), this);
        getServer().getPluginManager().registerEvents(this.combatListener, this);
        getServer().getPluginManager().registerEvents(new DeathListener(this), this);
        getServer().getPluginManager().registerEvents(new NPCListener(this), this);
        getServer().getPluginManager().registerEvents(new InventoryListener(this), this);
        getServer().getPluginManager().registerEvents(new LeaderboardListener(this), this);
        getServer().getPluginManager().registerEvents(new SpawnProtectionListener(this), this);
        getServer().getPluginManager().registerEvents(new SettingsListener(this), this);
    }

    private void startNpcTasks() {
        Bukkit.getScheduler().runTaskLater(this, () -> {
            if (Bukkit.getPluginManager().isPluginEnabled("Citizens")) {
                this.npcManager.createStandardArenaNPC();
                this.npcManager.createSpeedArenaNPC();
                this.npcManager.createBeastArenaNPC();
                this.npcManager.createKitEditorNPC();
            }
        }, 100L);
    }

    private void startLeaderboardTask() {
        Bukkit.getScheduler().runTaskTimerAsynchronously(this, () -> {
            if (this.statsManager != null) {
                this.statsManager.updateGlobalLeaderboards();
            } else {
                getLogger().warning("Failed to sync global leaderboards  with Database.");
            }
        }, 20L, 1200L);
    }

    @Override
    public void onDisable() {
        Bukkit.getScheduler().cancelTasks(this);

        if (statsManager != null) {
            getLogger().info("Saving stats for " + Bukkit.getOnlinePlayers().size() + " players...");
            for (Player player : Bukkit.getOnlinePlayers()) {
                try {
                    statsManager.saveAndUnloadPlayer(player);
                } catch (Exception e) {
                    getLogger().severe("Failed to save data for " + player.getName() + ": " + e.getMessage());
                }
            }
        }

        if (permsManager != null) {
            permsManager.saveConfig();
            getLogger().info("Permissions data saved.");
        }
        if (databaseManager != null) {
            databaseManager.close();
            getLogger().info("Database connection closed.");
        }

        getLogger().info("========================================");
        getLogger().info("   AdvancedFFA: Plugin Disabled Safe    ");
        getLogger().info("========================================");
    }

    // --- Getters ---
    public DatabaseManager getDatabaseManager() { return databaseManager; }
    public DataManager getPermsManager() { return permsManager; }
    public StatsManager getStatsManager() { return statsManager; }
    public NPCManager getNpcManager() { return npcManager; }
    public KitManager getKitManager() { return kitManager; }
    public CombatListener getCombatListener() { return combatListener; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public NametagManager getNametagManager() { return nametagManager; }
    public SettingsMenu getSettingsMenu() { return settingsMenu; }
    public KitCommand getKitCommand() { return kitCommand; }
}