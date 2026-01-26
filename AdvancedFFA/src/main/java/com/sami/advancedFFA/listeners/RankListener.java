package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RankListener implements Listener {

    private final Main plugin;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public RankListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!p.isOnline()) return;

            plugin.getNametagManager().updateNametag(p);

            PermissionAttachment att = p.addAttachment(plugin);
            attachments.put(uuid, att);

            List<String> ranks = plugin.getStatsManager().getRanks(uuid);
            for (String rName : ranks) {
                List<String> perms = plugin.getPermsManager().getConfig().getStringList(rName.toUpperCase());
                if (perms != null) {
                    for (String perm : perms) {
                        att.setPermission(perm, true);
                    }
                }
            }
        }, 1L);
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        UUID uuid = e.getPlayer().getUniqueId();

        PermissionAttachment att = attachments.remove(uuid);
        if (att != null) {
            e.getPlayer().removeAttachment(att);
        }

        plugin.getNametagManager().removeNametag(e.getPlayer());
    }
}