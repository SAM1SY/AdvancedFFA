package com.sami.advancedFFA.commands;

import com.sami.advancedFFA.Main;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class NPCCommand implements CommandExecutor {

    private final Main plugin;

    public NPCCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;

        if (!p.hasPermission("ffa.admin")) {
            p.sendMessage("§cNo permission.");
            return true;
        }

        // Call the manager to spawn it
        plugin.getNpcManager().createStandardNPC();
        p.sendMessage("§aNPC created at 48.5, -2.5, -1.5!");

        return true;
    }
}