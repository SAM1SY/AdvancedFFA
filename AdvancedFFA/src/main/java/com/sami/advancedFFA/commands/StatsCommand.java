package com.sami.advancedFFA.commands;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.UUID;

public class StatsCommand implements CommandExecutor {
    private final Main plugin;

    public StatsCommand(Main plugin) {
        this.plugin = plugin;
    }

    private final HashMap<UUID, Long> cooldowns = new HashMap<>();
    private final long COOLDOWN_TIME = 3000;

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player player = (Player) sender;

        UUID targetUUID;
        String targetName;

        if (cooldowns.containsKey(player.getUniqueId())) {
            long secondsLeft = (cooldowns.get(player.getUniqueId()) - System.currentTimeMillis()) / 1000;
            if (secondsLeft > 0) {
                player.sendMessage("§c§lERROR §8» §7Please wait " + secondsLeft + "s before using this again.");
                return true;
            }
        }

        // 1. Determine Target
        if (args.length == 0) {
            targetUUID = player.getUniqueId();
            targetName = player.getName();
        } else {
            OfflinePlayer target = Bukkit.getOfflinePlayer(args[0]);
            // Use hasPlayedBefore to avoid stats for fake/non-existent players
            if (!target.hasPlayedBefore() && !target.isOnline()) {
                player.sendMessage("§c§lERROR §8» §7Player has never joined the server.");
                return true;
            }
            targetUUID = target.getUniqueId();
            targetName = target.getName();
        }

        cooldowns.put(targetUUID, System.currentTimeMillis() + COOLDOWN_TIME);

        // 2. Fetch Data
        int kills = plugin.getStatsManager().getKills(targetUUID);
        int deaths = plugin.getStatsManager().getDeaths(targetUUID);
        int level = plugin.getStatsManager().getLevel(targetUUID);
        int xp = plugin.getStatsManager().getXP(targetUUID);
        int streak = plugin.getStatsManager().getStreak(targetUUID);
        int bestStreak = plugin.getStatsManager().getHighestStreak(targetUUID);

        double kdr = (deaths == 0) ? kills : (double) kills / deaths;
        String kdrFormatted = String.format("%.2f", kdr);

        player.sendMessage("§8§m----------------------------------");
        player.sendMessage("§b§lSTATS §8» §f" + targetName);
        player.sendMessage("");
        player.sendMessage(" §8• §7Level: " + plugin.getStatsManager().getLevelColor(targetUUID) + level + " §8(§f" + xp + " XP§8)");
        player.sendMessage(" §8• §7Kills: §b" + kills);
        player.sendMessage(" §8• §7Deaths: §b" + deaths);
        player.sendMessage(" §8• §7KDR: §3" + kdrFormatted);
        player.sendMessage("");
        player.sendMessage(" §8• §7Current Streak: §e" + streak + "🔥");
        player.sendMessage(" §8• §7Highest Streak: §6" + bestStreak + "✪");
        player.sendMessage("§8§m----------------------------------");

        return true;
    }
}