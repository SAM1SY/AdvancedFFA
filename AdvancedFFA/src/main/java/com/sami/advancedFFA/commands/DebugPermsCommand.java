package com.sami.advancedFFA.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.permissions.PermissionAttachmentInfo;

public class DebugPermsCommand implements CommandExecutor {
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        p.sendMessage("§b§l--- Permission Debug ---");
        p.sendMessage("§7Checking: §f" + p.getName());

        // Loop through every permission the player currently holds
        for (PermissionAttachmentInfo pai : p.getEffectivePermissions()) {
            if (pai.getPermission().contains("ffa") || pai.getPermission().equals("*")) {
                p.sendMessage("§a✔ " + pai.getPermission() + " §7(Value: " + pai.getValue() + ")");
            }
        }

        p.sendMessage("§eChecking specific nodes:");
        p.sendMessage("§7ffa.guild.use: " + (p.hasPermission("ffa.guild.use") ? "§aTRUE" : "§cFALSE"));
        p.sendMessage("§7ffa.kit.use: " + (p.hasPermission("ffa.kit.use") ? "§aTRUE" : "§cFALSE"));
        p.sendMessage("§b§l-----------------------");
        return true;
    }
}