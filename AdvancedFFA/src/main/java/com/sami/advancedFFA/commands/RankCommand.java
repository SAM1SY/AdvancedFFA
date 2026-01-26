package com.sami.advancedFFA.commands;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.Rank;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.*;

public class RankCommand implements CommandExecutor {
    private final Main plugin;

    public RankCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("advancedranks.use")) {
            sender.sendMessage(ChatColor.RED + "You don't have permission to use this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage(ChatColor.RED + "Usage: /rank <set|add|rem|reset|get> <player> [rank]");
            return true;
        }

        String action = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        UUID uuid = target.getUniqueId();

        List<String> ranks = new ArrayList<>(plugin.getStatsManager().getRanks(uuid));

        switch (action) {
            case "get":
                sender.sendMessage(ChatColor.GRAY + "--- " + ChatColor.YELLOW + target.getName() + "'s Ranks" + ChatColor.GRAY + " ---");
                if (ranks.isEmpty()) {
                    sender.sendMessage(ChatColor.RED + " No ranks found (Defaulting to MEMBER).");
                } else {
                    for (String rName : ranks) {
                        sender.sendMessage(ChatColor.GRAY + " » " + ChatColor.WHITE + rName);
                    }
                }
                return true;

            case "set":
                if (args.length < 3) return false;
                try {
                    Rank r = Rank.valueOf(args[2].toUpperCase());
                    ranks.clear();
                    ranks.add(Rank.MEMBER.name());
                    if (r != Rank.MEMBER) ranks.add(r.name());
                    plugin.getStatsManager().setRanks(uuid, ranks);
                    sender.sendMessage(ChatColor.GREEN + "Rank for " + target.getName() + " set to " + r.name());
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid Rank! Use: OWNER, DEV, MANAGER, MOD, HELPER, MVP, ELITE, VIP, MEMBER");
                    return true;
                }
                break;

            case "add":
                if (args.length < 3) return false;
                try {
                    String newRank = args[2].toUpperCase();
                    Rank.valueOf(newRank); // Validate if rank exists
                    if (!ranks.contains(newRank)) {
                        ranks.add(newRank);
                        plugin.getStatsManager().setRanks(uuid, ranks);
                        sender.sendMessage(ChatColor.GREEN + "Added " + newRank + " to " + target.getName());
                    } else {
                        sender.sendMessage(ChatColor.RED + target.getName() + " already has that rank.");
                        return true;
                    }
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(ChatColor.RED + "Invalid Rank!");
                    return true;
                }
                break;

            case "rem":
                if (args.length < 3) return false;
                String toRem = args[2].toUpperCase();
                if (toRem.equals("MEMBER")) {
                    sender.sendMessage(ChatColor.RED + "You cannot remove the MEMBER rank.");
                    return true;
                }
                if (ranks.remove(toRem)) {
                    plugin.getStatsManager().setRanks(uuid, ranks);
                    sender.sendMessage(ChatColor.YELLOW + "Removed " + toRem + " from " + target.getName());
                } else {
                    sender.sendMessage(ChatColor.RED + "This player does not have that rank.");
                    return true;
                }
                break;

            case "reset":
                ranks.clear();
                ranks.add(Rank.MEMBER.name());
                plugin.getStatsManager().setRanks(uuid, ranks);
                sender.sendMessage(ChatColor.YELLOW + "Ranks reset for " + target.getName());
                break;

            default:
                sender.sendMessage(ChatColor.RED + "Unknown action. Use: set, add, rem, reset, get");
                return true;
        }

        plugin.getStatsManager().syncRanksToDatabase(uuid, target.getName());

        if (target.isOnline()) {
            Player online = target.getPlayer();
            plugin.getNametagManager().updateNametag(online);
            sender.sendMessage(ChatColor.GRAY + "(Live update applied to Tab/Nametag for " + online.getName() + ")");
        }
        return true;
    }
}