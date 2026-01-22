package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.Rank;
import com.sami.advancedFFA.managers.DataManager;
import org.bukkit.ChatColor;
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
    private final DataManager dataManager;
    private final Map<UUID, PermissionAttachment> attachments = new HashMap<>();

    public RankListener(Main plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onJoinEvent(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        UUID uuid = p.getUniqueId();

        dataManager.getConfig().set("players." + uuid + ".name", p.getName());
        List<String> ranks = dataManager.getConfig().getStringList("players." + uuid + ".ranks");

        if (ranks.isEmpty()) {
            ranks.add(Rank.MEMBER.name());
            dataManager.getConfig().set("players." + uuid + ".ranks", ranks);
            dataManager.saveConfig();
        }

        Rank highest = Rank.MEMBER;
        for (String rName : ranks) {
            try {
                Rank r = Rank.valueOf(rName.toUpperCase());
                if (r.ordinal() < highest.ordinal()) highest = r;
            } catch (IllegalArgumentException ignored) {}
        }

        String tabFormat = highest.getDisplay() + " " + p.getName();
        p.setPlayerListName(ChatColor.translateAlternateColorCodes('&', tabFormat));

        PermissionAttachment att = p.addAttachment(plugin);
        attachments.put(uuid, att);

        for (String rName : ranks) {
            List<String> perms = plugin.getPermsManager().getConfig().getStringList(rName.toUpperCase());
            if (perms != null) {
                for (String perm : perms) {
                    att.setPermission(perm, true);
                }
            }
        }
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e) {
        PermissionAttachment att = attachments.remove(e.getPlayer().getUniqueId());
        if (att != null) {
            e.getPlayer().removeAttachment(att);
        }
    }
}