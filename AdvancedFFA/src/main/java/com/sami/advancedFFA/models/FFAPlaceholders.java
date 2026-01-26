package com.sami.advancedFFA.models;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.Rank;
import me.clip.placeholderapi.expansion.PlaceholderExpansion;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class FFAPlaceholders extends PlaceholderExpansion {

    private final Main plugin;

    public FFAPlaceholders(Main plugin) {
        this.plugin = plugin;
    }

    @Override public @NotNull String getIdentifier() { return "advancedffa"; }
    @Override public @NotNull String getAuthor() { return "Sami"; }
    @Override public @NotNull String getVersion() { return "1.0"; }
    @Override public boolean persist() { return true; }

    @Override
    public String onPlaceholderRequest(Player player, @NotNull String params) {
        if (player == null) return "";

        if (params.equalsIgnoreCase("level")) {
            return String.valueOf(plugin.getStatsManager().getLevel(player.getUniqueId()));
        }

        if (params.equalsIgnoreCase("level_color")) {
            return plugin.getStatsManager().getLevelColor(player.getUniqueId());
        }

        if (params.equalsIgnoreCase("streak")) {
            int streak = plugin.getStatsManager().getStreak(player.getUniqueId());
            return (streak > 0) ? "§6" + streak : "";
        }

        if (params.equalsIgnoreCase("rank")) {
            Rank highest = plugin.getStatsManager().getHighestRank(player.getUniqueId());
            if (highest == Rank.MEMBER) return "";
            return highest.getDisplay();
        }

        return null;
    }
}