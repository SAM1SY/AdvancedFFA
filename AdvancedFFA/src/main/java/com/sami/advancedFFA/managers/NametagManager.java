package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
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

        int level = plugin.getStatsManager().getLevel(uuid);
        String color = plugin.getStatsManager().getLevelColor(uuid);
        int streak = plugin.getStatsManager().getStreak(uuid);
        Rank rank = plugin.getStatsManager().getHighestRank(uuid);

        String rankPrefix = "";
        String plainRankName = ChatColor.stripColor(rank.getPrefix()).trim();

        if (!plainRankName.equalsIgnoreCase("Member") && !plainRankName.equalsIgnoreCase("Default")) {
            rankPrefix = rank.getPrefix() + " ";
        }

        String prefix = color + "[" + level + "] " + rankPrefix;
        String suffix = (streak > 0) ? " §6§l" + streak + "🔥" : "";

        String weight = getSortWeight(rank);
        String teamName = weight + player.getName();

        for (Team oldTeam : scoreboard.getTeams()) {
            if (oldTeam.hasEntry(player.getName())) {
                oldTeam.removeEntry(player.getName());
            }
        }

        Team team = scoreboard.getTeam(teamName);
        if (team == null) {
            team = scoreboard.registerNewTeam(teamName);
        }

        team.setPrefix(prefix);
        team.setSuffix(suffix);
        team.addEntry(player.getName());

        player.setScoreboard(scoreboard);
        player.setPlayerListName(prefix + "§f" + player.getName() + suffix);
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
        for (Team team : scoreboard.getTeams()) {
            if (team.hasEntry(player.getName())) {
                team.removeEntry(player.getName());
                if (team.getEntries().isEmpty()) {
                    team.unregister();
                }
            }
        }
    }
}