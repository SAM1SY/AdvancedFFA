package com.sami.advancedRanks.listeners;

import com.sami.advancedRanks.Main;
import com.sami.advancedRanks.Rank;
import com.sami.advancedRanks.managers.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.permissions.PermissionAttachment;

import java.util.List;

public class onJoin implements Listener {

    private final Main plugin;
    private final DataManager dataManager;

    public onJoin(Main plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) {
        Player p = e.getPlayer();
        String uuid = p.getUniqueId().toString();
        dataManager.getConfig().set("players." + uuid + ".name", p.getName());

        List<String> ranks = dataManager.getConfig().getStringList("players." + uuid + ".ranks");
        if (ranks.isEmpty()) {
            ranks.add(Rank.MEMBER.name());
            dataManager.getConfig().set("players." + uuid + ".ranks", ranks);
            dataManager.saveConfig();
        }

        // Apply Highest Rank to Tab List
        Rank highest = Rank.MEMBER;
        for (String rName : ranks) {
            try {
                Rank r = Rank.valueOf(rName);
                if (r.ordinal() < highest.ordinal()) highest = r;
            } catch (Exception ignored) {}
        }
        p.setPlayerListName(ChatColor.translateAlternateColorCodes('&', highest.getDisplay() + " " + p.getName()));

        // Apply Perms
        PermissionAttachment att = p.addAttachment(plugin);
        for (String rName : ranks) {
            List<String> perms = plugin.getPermsManager().getConfig().getStringList(rName);
            for (String perm : perms) att.setPermission(perm, true);
        }
    }
}