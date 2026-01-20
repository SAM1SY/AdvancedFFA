package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.Rank;
import com.sami.advancedFFA.managers.DataManager;
import org.bukkit.ChatColor;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.List;

public class ChatListener implements Listener {

    private final Main plugin;
    private final DataManager dataManager;

    public ChatListener(Main plugin, DataManager dataManager) {
        this.plugin = plugin;
        this.dataManager = dataManager;
    }

    @EventHandler
    public void onChat(AsyncPlayerChatEvent e) {
        if (!plugin.getConfig().getBoolean("display-prefixes-chat", true)) return;

        List<String> ranks = dataManager.getConfig().getStringList("players." + e.getPlayer().getUniqueId() + ".ranks");

        Rank highest = Rank.MEMBER;
        for (String rName : ranks) {
            try {
                Rank r = Rank.valueOf(rName);
                if (r.ordinal() < highest.ordinal()) highest = r;
            } catch (Exception ignored) {}
        }

        String rawFormat = plugin.getConfig().getString("chat-format", "%prefixes% %player%: %message%");

        String formatted = rawFormat
                .replace("%prefixes%", highest.getDisplay())
                .replace("%player%", "%1$s")
                .replace("%message%", "%2$s");

        e.setFormat(ChatColor.translateAlternateColorCodes('&', formatted));
    }
}