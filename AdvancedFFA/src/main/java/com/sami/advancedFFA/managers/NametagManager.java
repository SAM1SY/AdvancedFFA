package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.Rank;
import com.sami.advancedFFA.models.Guild;
import com.sami.advancedFFA.utils.ColorTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.Scoreboard;
import org.bukkit.scoreboard.Team;

import java.util.UUID;

public class NametagManager {

    private final Main plugin;
    private final Scoreboard scoreboard;

    public NametagManager(Main plugin) {
        this.plugin = plugin;
        this.scoreboard = Bukkit.getScoreboardManager().getMainScoreboard();
    }

    public void updateNametag(Player player) {
        UUID uuid = player.getUniqueId();
        Guild g = plugin.getGuildManager().getGuild(uuid);

        // DEBUG 1: Check if the manager even sees the player in a guild
        if (g == null) {
            Bukkit.getLogger().info("[DEBUG] No guild found for " + player.getName() + " in cache.");
        } else {
            Bukkit.getLogger().info("[DEBUG] Found guild " + g.getName() + " for " + player.getName());
        }

        StringBuilder suffixBuilder = new StringBuilder();

        // Streak logic
        int streak = plugin.getStatsManager().getStreak(uuid);
        if (streak > 0) {
            suffixBuilder.append(" §6").append(streak);
        }

        // Guild Tag logic
        if (g != null) {
            String colorData = g.getTagColor();
            String tag = g.getTag();

            // DEBUG 2: Check color data
            Bukkit.getLogger().info("[DEBUG] ColorData: " + colorData + " | Tag: " + tag);

            if (colorData != null && colorData.contains(":")) {
                String[] hex = colorData.split(":");
                suffixBuilder.append(" §8§l[").append(ColorTag.getAnimatedTag(tag, hex[0], hex[1], plugin.getAnimationFrame())).append("§8§l]");
            } else {
                suffixBuilder.append(" §8§l[").append(colorData != null ? colorData : "§b").append(tag).append("§8§l]");
            }
        }

        String suffix = suffixBuilder.toString();
        Bukkit.getLogger().info("[DEBUG] Final Suffix for " + player.getName() + ": " + suffix);

        // Apply to scoreboard...
        Team team = scoreboard.getTeam(getSortWeight(plugin.getStatsManager().getHighestRank(uuid)) + player.getName());
        if (team == null) team = scoreboard.registerNewTeam(getSortWeight(plugin.getStatsManager().getHighestRank(uuid)) + player.getName());

        team.setPrefix(plugin.getStatsManager().getLevelColor(uuid) + "[" + plugin.getStatsManager().getLevel(uuid) + "] §f");
        team.setSuffix(suffix);
        team.addEntry(player.getName());
    }

    private String getSortWeight(Rank rank) {
        switch (rank) {
            case OWNER:   return "a_";
            case DEV:     return "b_";
            case MANAGER: return "c_";
            case MOD:     return "d_";
            case HELPER:  return "e_";
            case MVP:     return "f_";
            case ELITE:   return "g_";
            case VIP:     return "h_";
            case MEMBER:  return "i_";
            default:      return "z_";
        }
    }

    public void removeNametag(Player player) {
        Team team = scoreboard.getEntryTeam(player.getName());
        if (team != null) {
            team.removeEntry(player.getName());
        }
    }
}