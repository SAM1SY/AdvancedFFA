package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.Guild;
import com.sami.advancedFFA.utils.ColorTag;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.*;

public class GuildManager {
    private final Main plugin;
    private final Map<String, Guild> guilds = new HashMap<>(); // Name (lowercase) -> Guild Object
    private final Map<UUID, String> playerToGuild = new HashMap<>(); // Player UUID -> Guild Name
    private final Map<UUID, String> invites = new HashMap<>(); // Invitee UUID -> Guild Name
    private final Set<UUID> guildChatToggled = new HashSet<>();

    public GuildManager(Main plugin) {
        this.plugin = plugin;
    }

    public void loadGuilds() {
        guilds.clear();
        playerToGuild.clear();

        try (Connection conn = plugin.getDatabaseManager().getConnection()) {
            String guildSql = "SELECT * FROM ffa_guilds";
            try (PreparedStatement ps = conn.prepareStatement(guildSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String name = rs.getString("name");
                    String tag = rs.getString("tag");
                    UUID leader = UUID.fromString(rs.getString("leader"));

                    Guild guild = new Guild(name, tag, leader);

                    String activeColor = rs.getString("tag_color");
                    guild.setTagColor(activeColor != null ? activeColor : "§b");

                    String ownedColorsRaw = rs.getString("owned_colors");
                    guild.setOwnedColorsFromString(ownedColorsRaw);

                    guild.setPoints(rs.getInt("points"));
                    guild.setCoins(rs.getDouble("coins"));

                    guilds.put(name.toLowerCase(), guild);
                }
            }

            // 2. Load all members and their ranks from ffa_guild_members
            String memberSql = "SELECT * FROM ffa_guild_members";
            try (PreparedStatement ps = conn.prepareStatement(memberSql); ResultSet rs = ps.executeQuery()) {
                while (rs.next()) {
                    String uuidStr = rs.getString("uuid");
                    String gName = rs.getString("guild_name");
                    String rank = rs.getString("rank");

                    if (uuidStr == null || gName == null) continue;

                    UUID uuid = UUID.fromString(uuidStr);
                    Guild g = guilds.get(gName.toLowerCase());

                    if (g != null) {
                        if (!g.getMembers().contains(uuid)) {
                            g.getMembers().add(uuid);
                        }

                        playerToGuild.put(uuid, gName.toLowerCase());

                        if (rank.equalsIgnoreCase("Co-Founder")) {
                            g.getCoFounders().add(uuid);
                        } else if (rank.equalsIgnoreCase("Officer")) {
                            g.getOfficers().add(uuid);
                        }
                    }
                }
            }
            plugin.getLogger().info("§a[Guilds] Successfully loaded " + guilds.size() + " guilds.");
        } catch (Exception e) {
            plugin.getLogger().severe("§c[Guilds] Error loading guilds from database!");
            e.printStackTrace();
        }
    }

    public Guild getGuild(UUID uuid) {
        String name = playerToGuild.get(uuid);
        return (name != null) ? guilds.get(name.toLowerCase()) : null;
    }

    public String getGuildTag(Player p) {
        Guild g = getGuild(p.getUniqueId());
        if (g == null || g.getTag() == null) return "";

        String colorData = g.getTagColor();
        String tag = g.getTag();

        if (colorData.contains(":")) {
            String[] hexes = colorData.split(":");
            return "§8[" + ColorTag.getGradientTag(tag, hexes[0], hexes[1]) + "§8] §r";
        }

        return "§8[" + colorData + tag + "§8] §r";
    }

    public boolean createGuild(Player p, String name, String tag) {
        if (getGuild(p.getUniqueId()) != null) return false;
        if (guilds.containsKey(name.toLowerCase())) return false;

        Guild guild = new Guild(name, tag, p.getUniqueId());
        guilds.put(name.toLowerCase(), guild);
        addPlayerToCache(p.getUniqueId(), name);
        return true;
    }

    public void disbandGuild(String name) {
        Guild guild = guilds.remove(name.toLowerCase());
        if (guild != null) {
            plugin.getDatabaseManager().deleteGuild(guild.getName());
            for (UUID uuid : new HashSet<>(guild.getMembers())) {
                removePlayerFromCache(uuid);
            }
        }
    }

    public void broadcast(Guild g, String message) {
        String formatted = "§b§lGUILD §8» §7" + message;
        for (UUID uuid : g.getMembers()) {
            Player p = Bukkit.getPlayer(uuid);
            if (p != null && p.isOnline()) {
                p.sendMessage(formatted);
            }
        }
    }

    // --- Chat & Cache Management ---

    public boolean toggleGuildChat(Player p) {
        if (guildChatToggled.contains(p.getUniqueId())) {
            guildChatToggled.remove(p.getUniqueId());
            return false; // Result: Global Chat
        } else {
            guildChatToggled.add(p.getUniqueId());
            return true; // Result: Guild Chat
        }
    }

    public boolean isInGuildChat(Player p) { return guildChatToggled.contains(p.getUniqueId()); }
    public void addPlayerToCache(UUID uuid, String guildName) { playerToGuild.put(uuid, guildName.toLowerCase()); }

    public void removePlayerFromCache(UUID uuid) {
        playerToGuild.remove(uuid);
        guildChatToggled.remove(uuid);
    }


    public Map<UUID, String> getInvites() { return invites; }
    public Map<String, Guild> getGuilds() { return guilds; }
}