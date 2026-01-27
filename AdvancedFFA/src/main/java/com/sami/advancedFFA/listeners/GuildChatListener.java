package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.Guild;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.UUID;

public class GuildChatListener implements Listener {

    private final Main plugin;

    public GuildChatListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onGuildChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        Guild g = plugin.getGuildManager().getGuild(p.getUniqueId());
        if (g == null) return;

        if (plugin.getGuildManager().isInGuildChat(p)) {
            e.setCancelled(true);

            String format = "§b§lGUILD §8| §f" + p.getName() + "§7: " + e.getMessage();

            for (UUID memberUUID : g.getMembers()) {
                Player member = Bukkit.getPlayer(memberUUID);
                if (member != null && member.isOnline()) {
                    member.sendMessage(format);
                }
            }

            Bukkit.getLogger().info("[Guild Chat - " + g.getName() + "] " + p.getName() + ": " + e.getMessage());
        }
    }
}