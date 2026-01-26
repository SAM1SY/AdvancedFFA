package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.Guild;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildManager {
    private final Main plugin;
    private final Map<String, Guild> guilds = new HashMap<>(); // Name -> Guild
    private final Map<UUID, String> playerGuilds = new HashMap<>(); // Player -> Guild Name

    public GuildManager(Main plugin) {
        this.plugin = plugin;
    }

    public boolean createGuild(Player leader, String name, String tag) {
        if (playerGuilds.containsKey(leader.getUniqueId())) return false;
        if (guilds.containsKey(name.toLowerCase())) return false;

        Guild newGuild = new Guild(name, tag, leader.getUniqueId());
        guilds.put(name.toLowerCase(), newGuild);
        playerGuilds.put(leader.getUniqueId(), name.toLowerCase());

        return true;
    }

    public Guild getGuild(Player p) {
        String guildName = playerGuilds.get(p.getUniqueId());
        if (guildName == null) return null;
        return guilds.get(guildName);
    }

    public String getGuildTag(Player p) {
        Guild g = getGuild(p);
        return (g != null) ? "§8[§b" + g.getTag() + "§8] " : "";
    }
}