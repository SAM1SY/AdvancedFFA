package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.Guild;
import org.bukkit.Material;

import java.io.File;
import java.sql.*;
import java.util.*;

public class DatabaseManager {

    private final Main plugin;
    private Connection connection;
    private final File dbFile;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        this.dbFile = new File(plugin.getDataFolder(), "database.db");

        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdirs();
        }

        initConnection();
        setupTables();
    }

    private void initConnection() {
        try {
            if (connection != null && !connection.isClosed()) return;

            Class.forName("org.sqlite.JDBC");
            this.connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (Exception e) {
            plugin.getLogger().severe("Could not connect to SQLite database!");
            e.printStackTrace();
        }
    }

    public Connection getConnection() {
        try {
            if (connection == null || connection.isClosed()) {
                initConnection();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return connection;
    }

    private void setupTables() {
        try (Statement s = getConnection().createStatement()) {
            // 1. Player Stats Table
            s.execute("CREATE TABLE IF NOT EXISTS player_stats (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "name TEXT, " +
                    "kills INTEGER DEFAULT 0, " +
                    "deaths INTEGER DEFAULT 0, " +
                    "current_streak INTEGER DEFAULT 0, " +
                    "best_streak INTEGER DEFAULT 0, " +
                    "level INTEGER DEFAULT 1, " +
                    "xp INTEGER DEFAULT 0, " +
                    "ranks TEXT)");

            // 2. Kit Layouts Table
            s.execute("CREATE TABLE IF NOT EXISTS kit_layouts (" +
                    "uuid TEXT, " +
                    "mode TEXT, " +
                    "layout TEXT, " +
                    "PRIMARY KEY (uuid, mode))");

            // 3. Guild Main Table
            s.execute("CREATE TABLE IF NOT EXISTS ffa_guilds (" +
                    "name TEXT PRIMARY KEY, " +
                    "tag TEXT, " +
                    "tag_color TEXT, " +
                    "leader TEXT, " +
                    "points INTEGER DEFAULT 0, " +
                    "coins REAL DEFAULT 0.0, " +
                    "owned_colors TEXT DEFAULT '')");

            // --- MIGRATION: Check if owned_colors column exists (for old databases) ---
            try {
                s.execute("ALTER TABLE ffa_guilds ADD COLUMN owned_colors TEXT DEFAULT ''");
                plugin.getLogger().info("Successfully migrated ffa_guilds table to include owned_colors.");
            } catch (SQLException ignored) {
                // Column already exists, we can ignore this error safely
            }

            // 4. Guild Members Table
            s.execute("CREATE TABLE IF NOT EXISTS ffa_guild_members (" +
                    "uuid TEXT PRIMARY KEY, " +
                    "guild_name TEXT, " +
                    "rank TEXT)");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // --- PLAYER STATS LOGIC ---

    public void savePlayer(UUID uuid, String name, int kills, int deaths, int curStreak, int bestStreak, int level, int xp, List<String> ranks) {
        String ranksStr = String.join(",", ranks);
        String sql = "INSERT INTO player_stats(uuid, name, kills, deaths, current_streak, best_streak, level, xp, ranks) " +
                "VALUES(?,?,?,?,?,?,?,?,?) ON CONFLICT(uuid) DO UPDATE SET " +
                "name=excluded.name, kills=excluded.kills, deaths=excluded.deaths, " +
                "current_streak=excluded.current_streak, best_streak=excluded.best_streak, " +
                "level=excluded.level, xp=excluded.xp, ranks=excluded.ranks";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, name);
            pstmt.setInt(3, kills);
            pstmt.setInt(4, deaths);
            pstmt.setInt(5, curStreak);
            pstmt.setInt(6, bestStreak);
            pstmt.setInt(7, level);
            pstmt.setInt(8, xp);
            pstmt.setString(9, ranksStr);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<String, Object> loadPlayer(UUID uuid) {
        Map<String, Object> data = new HashMap<>();
        String sql = "SELECT * FROM player_stats WHERE uuid = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();

            if (rs.next()) {
                data.put("kills", rs.getInt("kills"));
                data.put("deaths", rs.getInt("deaths"));
                data.put("current_streak", rs.getInt("current_streak"));
                data.put("best_streak", rs.getInt("best_streak"));
                data.put("level", rs.getInt("level"));
                data.put("xp", rs.getInt("xp"));

                String rawRanks = rs.getString("ranks");
                List<String> rankList = new ArrayList<>();
                if (rawRanks != null && !rawRanks.isEmpty()) {
                    rankList.addAll(Arrays.asList(rawRanks.split(",")));
                } else {
                    rankList.add("MEMBER");
                }
                data.put("ranks", rankList);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return data;
    }

    public Map<String, Integer> getTop10(String column) {
        Map<String, Integer> top = new LinkedHashMap<>();
        String sql = "SELECT name, " + column + " FROM player_stats ORDER BY " + column + " DESC LIMIT 10";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                top.put(rs.getString("name"), rs.getInt(column));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
    }

    // --- KIT LAYOUT LOGIC ---

    public void saveKitLayout(UUID uuid, String mode, Map<Material, Integer> layout) {
        if (layout.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        layout.forEach((mat, slot) -> sb.append(mat.name()).append(":").append(slot).append(","));

        String sql = "INSERT INTO kit_layouts(uuid, mode, layout) VALUES(?,?,?) " +
                "ON CONFLICT(uuid, mode) DO UPDATE SET layout=excluded.layout";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, mode);
            pstmt.setString(3, sb.toString());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public Map<Material, Integer> loadKitLayout(UUID uuid, String mode) {
        Map<Material, Integer> layout = new HashMap<>();
        String sql = "SELECT layout FROM kit_layouts WHERE uuid = ? AND mode = ?";

        try (PreparedStatement pstmt = getConnection().prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, mode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String raw = rs.getString("layout");
                if (raw != null && !raw.isEmpty()) {
                    for (String entry : raw.split(",")) {
                        if (entry.contains(":")) {
                            String[] parts = entry.split(":");
                            try {
                                layout.put(Material.valueOf(parts[0]), Integer.parseInt(parts[1]));
                            } catch (Exception ignored) {}
                        }
                    }
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return layout;
    }

    // --- GUILD LOGIC ---

    public void saveGuild(Guild g) {
        // Updated to use 7 parameters to match the 7 columns provided
        String sql = "REPLACE INTO ffa_guilds (name, tag, tag_color, leader, points, coins, owned_colors) VALUES (?, ?, ?, ?, ?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, g.getName());
            ps.setString(2, g.getTag());
            ps.setString(3, g.getTagColor());
            ps.setString(4, g.getLeader().toString());
            ps.setInt(5, g.getPoints());
            ps.setDouble(6, g.getCoins());
            ps.setString(7, g.getOwnedColorsString());
            ps.executeUpdate();

            // Save current members
            for (UUID uuid : g.getMembers()) {
                saveMember(uuid, g.getName(), g.getRole(uuid));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void saveMember(UUID uuid, String guildName, String rank) {
        String sql = "REPLACE INTO ffa_guild_members (uuid, guild_name, rank) VALUES (?, ?, ?)";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.setString(2, guildName);
            ps.setString(3, rank);
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void removeMember(UUID uuid) {
        String sql = "DELETE FROM ffa_guild_members WHERE uuid = ?";
        try (PreparedStatement ps = getConnection().prepareStatement(sql)) {
            ps.setString(1, uuid.toString());
            ps.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteGuild(String guildName) {
        try {
            try (PreparedStatement ps1 = getConnection().prepareStatement("DELETE FROM ffa_guilds WHERE name = ?")) {
                ps1.setString(1, guildName);
                ps1.executeUpdate();
            }

            try (PreparedStatement ps2 = getConnection().prepareStatement("DELETE FROM ffa_guild_members WHERE guild_name = ?")) {
                ps2.setString(1, guildName);
                ps2.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void close() {
        try {
            if (connection != null && !connection.isClosed()) {
                connection.close();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}