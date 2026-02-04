package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.Rank;
import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import java.util.*;

public class StatsManager {
    private final Main plugin;
    private final Map<UUID, PlayerStats> statsCache = new HashMap<>();
    private final Map<UUID, List<String>> rankCache = new HashMap<>();
    private final Map<String, Map<String, Integer>> leaderboardCache = new HashMap<>();

    private final Set<Integer> milestones = new HashSet<>(Arrays.asList(5, 10, 20, 50, 67, 75, 100, 150, 200, 500));

    public StatsManager(Main plugin) { this.plugin = plugin; }

    public static class PlayerStats {
        public int kills, deaths, level, currentStreak, bestStreak, xp;
        public PlayerStats(int k, int d, int l, int bs, int cs, int xp) {
            this.kills = k; this.deaths = d; this.level = l;
            this.bestStreak = bs; this.currentStreak = cs; this.xp = xp;
        }
    }

    public void loadPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        Map<String, Object> data = plugin.getDatabaseManager().loadPlayer(uuid);

        if (data.isEmpty()) {
            statsCache.put(uuid, new PlayerStats(0, 0, 1, 0, 0, 0));
            rankCache.put(uuid, new ArrayList<>(Collections.singletonList("MEMBER")));
        } else {
            statsCache.put(uuid, new PlayerStats(
                    (int)data.get("kills"), (int)data.get("deaths"), (int)data.get("level"),
                    (int)data.get("best_streak"), (int)data.get("current_streak"), (int)data.get("xp")
            ));
            rankCache.put(uuid, (List<String>) data.get("ranks"));
        }
        // Use the centralized NametagManager
        plugin.getNametagManager().updateNametag(player);
    }

    public void syncRanksToDatabase(UUID uuid, String name) {
        PlayerStats stats = statsCache.get(uuid);
        List<String> ranks = getRanks(uuid);

        if (stats != null) {
            plugin.getDatabaseManager().savePlayer(uuid, name, stats.kills, stats.deaths,
                    stats.currentStreak, stats.bestStreak, stats.level, stats.xp, ranks);
        } else {
            Map<String, Object> data = plugin.getDatabaseManager().loadPlayer(uuid);
            if (!data.isEmpty()) {
                plugin.getDatabaseManager().savePlayer(uuid, name,
                        (int)data.get("kills"), (int)data.get("deaths"),
                        (int)data.get("current_streak"), (int)data.get("best_streak"),
                        (int)data.get("level"), (int)data.get("xp"), ranks);
            }
        }
    }

    public void saveAndUnloadPlayer(Player p) {
        UUID uuid = p.getUniqueId();
        PlayerStats stats = statsCache.get(uuid);
        List<String> ranks = getRanks(uuid);

        if (stats != null) {
            plugin.getDatabaseManager().savePlayer(uuid, p.getName(), stats.kills, stats.deaths,
                    stats.currentStreak, stats.bestStreak, stats.level, stats.xp, ranks);

            String[] modes = {"Standard", "Speed", "Beast"};
            for (String mode : modes) {
                Map<Material, Integer> layout = plugin.getKitManager().getLayout(uuid, mode);
                if (layout != null && !layout.isEmpty()) {
                    plugin.getDatabaseManager().saveKitLayout(uuid, mode, layout);
                }
            }
        }
        statsCache.remove(uuid);
        rankCache.remove(uuid);
    }

    public void addKill(Player p) {
        PlayerStats s = statsCache.get(p.getUniqueId());
        if (s == null) return;

        s.kills++;
        s.currentStreak++;
        int xpGained = 15;
        s.xp += xpGained;

        // Sync with /settings: Send XP ActionBar if enabled
        if (plugin.getSettingsMenu().getSetting(p, "xp_bar", 1) == 1) {
            p.spigot().sendMessage(ChatMessageType.ACTION_BAR,
                    new TextComponent("§a+" + xpGained + " XP §8(§f" + s.xp + " Total§8)"));
        }

        if (s.currentStreak > s.bestStreak) s.bestStreak = s.currentStreak;

        checkStreakMilestone(p, s.currentStreak);

        // Level Logic (Level up every 50 kills)
        int nextLevel = (s.kills / 50) + 1;
        if (nextLevel > s.level) {
            s.level = nextLevel;
            p.sendMessage("§b§lLEVEL UP §8» §7Reached Level §f" + s.level);
            p.sendTitle("§b§lLEVEL UP", "§7Level §f" + s.level, 10, 40, 10);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 1f);
        }

        // Refresh Tab and Nametag (Updates level color and streak🔥)
        plugin.getNametagManager().updateNametag(p);
    }

    public void addDeath(Player p) {
        PlayerStats s = statsCache.get(p.getUniqueId());
        if (s != null) {
            s.deaths++;
            s.currentStreak = 0;
            // Instantly remove streak from Tab/Nametag
            plugin.getNametagManager().updateNametag(p);
        }
    }

    private void checkStreakMilestone(Player p, int streak) {
        if (milestones.contains(streak)) {
            String msg = "§6§lSTREAK §8» §f" + p.getName() + " §7is on a §d§l" + streak + " §7killstreak!";
            Bukkit.broadcastMessage(msg);

            Sound sound = (streak >= 50) ? Sound.ENTITY_WITHER_SPAWN : Sound.ENTITY_PLAYER_LEVELUP;
            float pitch = (streak >= 50) ? 0.8f : 1.2f;

            for (Player all : Bukkit.getOnlinePlayers()) {
                all.playSound(all.getLocation(), sound, 0.5f, pitch);
            }

            int bonus = streak * 2;
            statsCache.get(p.getUniqueId()).xp += bonus;
            p.sendMessage("§a+ " + bonus + " XP Milestone Bonus!");
        }
    }

    public Rank getHighestRank(UUID uuid) {
        List<String> ranks = getRanks(uuid);
        Rank highest = Rank.MEMBER;
        for (String rName : ranks) {
            try {
                Rank r = Rank.valueOf(rName.toUpperCase());
                if (r.ordinal() < highest.ordinal()) highest = r;
            } catch (Exception ignored) {}
        }
        return highest;
    }

    public int getLevel(UUID uuid) {
        if (statsCache.containsKey(uuid)) return statsCache.get(uuid).kills;

        Map<String, Object> data = plugin.getDatabaseManager().loadPlayer(uuid);
        return data.isEmpty() ? 0 : (int) data.get("level");
    }

    public int getStreak(UUID uuid) {
        if (statsCache.containsKey(uuid)) return statsCache.get(uuid).kills;
        Map<String, Object> data = plugin.getDatabaseManager().loadPlayer(uuid);
        return data.isEmpty() ? 0 : (int) data.get("deaths");
    }

    public int getKills(UUID uuid) {
        if (statsCache.containsKey(uuid)) return statsCache.get(uuid).kills;
        Map<String, Object> data = plugin.getDatabaseManager().loadPlayer(uuid);
        return data.isEmpty() ? 0 : (int) data.get("kills");
    }

    public int getDeaths(UUID uuid) {
        if (statsCache.containsKey(uuid)) return statsCache.get(uuid).deaths;
        Map<String, Object> data = plugin.getDatabaseManager().loadPlayer(uuid);
        return data.isEmpty() ? 0 : (int) data.get("deaths");
    }

    public int getHighestStreak(UUID uuid) {
        if (statsCache.containsKey(uuid)) return statsCache.get(uuid).bestStreak;
        Map<String, Object> data = plugin.getDatabaseManager().loadPlayer(uuid);
        return data.isEmpty() ? 0 : (int) data.get("best_streak");
    }

    public int getXP(UUID uuid) {
        if (statsCache.containsKey(uuid)) return statsCache.get(uuid).xp;
        Map<String, Object> data = plugin.getDatabaseManager().loadPlayer(uuid);
        return data.isEmpty() ? 0 : (int) data.get("xp");
    }

    public String getLevelColor(UUID uuid) {
        int lvl = getLevel(uuid);
        if (lvl >= 100) return "§4";
        if (lvl >= 50) return "§c";
        if (lvl >= 25) return "§e";
        return "§7";
    }

    public void updateGlobalLeaderboards() {
        if (plugin.getDatabaseManager() == null) return;

        String[] categories = {"kills", "level", "best_streak", "deaths"};

        for (String cat : categories) {
            Map<String, Integer> data = plugin.getDatabaseManager().getTop10(cat);
            leaderboardCache.put(cat, data);
        }
    }

    public Map<String, Integer> getCachedTop10(String key) {
        return leaderboardCache.getOrDefault(key, new HashMap<>());
    }

    public List<String> getRanks(UUID uuid) {
        return rankCache.getOrDefault(uuid, new ArrayList<>(Collections.singletonList("MEMBER")));
    }

    public void setRanks(UUID uuid, List<String> ranks) {
        rankCache.put(uuid, ranks);
    }
}