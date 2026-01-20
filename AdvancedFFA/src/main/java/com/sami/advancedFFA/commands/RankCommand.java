package com.sami.advancedFFA.commands;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.Rank;
import com.sami.advancedFFA.managers.DataManager;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachment;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class RankCommand implements CommandExecutor {

    private final Main plugin;
    private final DataManager dataManager;

    public RankCommand(Main plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command cmd, @NotNull String s, @NotNull String[] args) {

        if (!sender.hasPermission("advancedranks.use")) {
            sender.sendMessage("§4§lYou don't have the permission to run this command.");
            return true;
        }

        if (args.length < 2) {
            sender.sendMessage("§4§lUsage: §r§7/rank <set | add | rem | reset | get> <player> [rank]");
            return true;
        }

        String action = args[0].toLowerCase();
        OfflinePlayer target = Bukkit.getOfflinePlayer(args[1]);
        String uuid = target.getUniqueId().toString();

        if (!dataManager.getConfig().contains("players." + uuid)) {
            sender.sendMessage("§c" + target.getName() + " has never joined the server.");
            return true;
        }

        List<String> rankList = dataManager.getConfig().getStringList("players." + uuid + ".ranks");

        switch (action) {
            case "get":
                List<String> displays = new ArrayList<>();
                for (String rName : rankList) {
                    try { displays.add(Rank.valueOf(rName).getDisplay()); } catch (Exception ignored) {}
                }
                sender.sendMessage("§l" + target.getName() + "§r§7's ranks: " + String.join("§r§7, ", displays));
                break;

            case "add":
                if (args.length < 3) { sender.sendMessage("§4§lUsage: §r§7/rank add <player> <rank>"); return true; }
                try {
                    Rank rank = Rank.valueOf(args[2].toUpperCase());
                    if (!rankList.contains(rank.name())) {
                        rankList.remove(Rank.MEMBER.name());
                        rankList.add(rank.name());
                        saveAndRefresh(target, rankList);
                        sender.sendMessage("§aAdded " + rank.getDisplay() + " §ato §l" + target.getName());
                    } else {
                        sender.sendMessage("§cPlayer already has this rank.");
                    }
                } catch (Exception e) { sender.sendMessage("§4That is not a valid rank."); }
                break;

            case "set":
                if (args.length < 3) { sender.sendMessage("§4§lUsage: §r§7/rank set <player> <rank>"); return true; }
                try {
                    Rank rank = Rank.valueOf(args[2].toUpperCase());
                    rankList.clear();
                    rankList.add(rank.name());
                    saveAndRefresh(target, rankList);
                    sender.sendMessage("§aSet §l" + target.getName() + "§r§a's rank to " + rank.getDisplay());
                } catch (Exception e) { sender.sendMessage("§4That is not a valid rank."); }
                break;

            case "rem":
                if (args.length < 3) { sender.sendMessage("§4§lUsage: §r§7/rank rem <player> <rank>"); return true; }
                if (args[2].toUpperCase().equals(Rank.MEMBER.name())) { sender.sendMessage("§4§lYou can't remove MEMBER rank."); return true; }
                if (rankList.remove(args[2].toUpperCase())) {
                    if (rankList.isEmpty()) rankList.add(Rank.MEMBER.name());
                    saveAndRefresh(target, rankList);
                    sender.sendMessage("§eRemoved §l" + args[2].toUpperCase() + " §r§efrom §l" + target.getName());
                } else {
                    sender.sendMessage("§cPlayer does not have that rank.");
                }
                break;

            case "reset":
                rankList.clear();
                rankList.add(Rank.MEMBER.name());
                saveAndRefresh(target, rankList);
                sender.sendMessage("§l" + target.getName() + "§r§e's rank reset.");
                break;

            default:
                sender.sendMessage("§l§4Invalid action! §r§4Use: set, add, rem, reset, get");
        }
        return true;
    }

    private void saveAndRefresh(OfflinePlayer target, List<String> ranks) {
        dataManager.getConfig().set("players." + target.getUniqueId() + ".ranks", ranks);
        dataManager.saveConfig();
        Player online = target.getPlayer();
        if (online != null) {
            Rank highest = getHighestRank(ranks);
            online.setPlayerListName(ChatColor.translateAlternateColorCodes('&', highest.getDisplay() + " " + online.getName()));

            // Re-apply permissions
            PermissionAttachment att = online.addAttachment(plugin);
            for (String r : ranks) {
                for (String p : plugin.getPermsManager().getConfig().getStringList(r)) att.setPermission(p, true);
            }
        }
    }

    private Rank getHighestRank(List<String> ranks) {
        Rank highest = Rank.MEMBER;
        for (String rName : ranks) {
            try {
                Rank r = Rank.valueOf(rName);
                if (r.ordinal() < highest.ordinal()) highest = r;
            } catch (Exception ignored) {}
        }
        return highest;
    }
}