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

        int level = plugin.getStatsManager().getLevel(uuid);
        String lColor = plugin.getStatsManager().getLevelColor(uuid);
        Rank rank = plugin.getStatsManager().getHighestRank(uuid);
        int streak = plugin.getStatsManager().getStreak(uuid);
        Guild g = plugin.getGuildManager().getGuild(uuid);

        String rankPrefix = (rank == Rank.MEMBER) ? "" : rank.getPrefix();
        String prefix = lColor + "[" + level + "] " + rankPrefix;

        StringBuilder sb = new StringBuilder();
        if (streak > 0) sb.append(" §6").append(streak);

        if (g != null) {
            if (sb.length() > 0) sb.append(" ");
            String colorData = g.getTagColor();
            String tag = g.getTag();

            if (colorData != null && colorData.contains(":")) {
                String[] hex = colorData.split(":");
                sb.append(" §8§l[").append(ColorTag.getAnimatedTag(tag, hex[0], hex[1], plugin.getAnimationFrame())).append("§8§l]");
            } else {
                sb.append(" §8§l[").append(colorData != null ? colorData : "§b").append(tag).append("§8§l]");
            }
        }
        String suffix = sb.toString();

        Team oldTeam = scoreboard.getEntryTeam(player.getName());
        if (oldTeam != null) {
            oldTeam.removeEntry(player.getName());
        }

        String teamName = getSortWeight(rank) + player.getName();
        if (teamName.length() > 16) teamName = teamName.substring(0, 16);

        Team team = scoreboard.getTeam(teamName);
        if (team == null) team = scoreboard.registerNewTeam(teamName);

        team.setPrefix(prefix);
        team.setSuffix(suffix);
        team.addEntry(player.getName());

        player.setPlayerListName(prefix + player.getName() + suffix);
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