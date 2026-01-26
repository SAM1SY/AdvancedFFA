package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
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
        this.dbFile = new File(plugin.getDataFolder(), "data.db");
        connect();
        setupTables();
    }

    private void connect() {
        try {
            Class.forName("org.sqlite.JDBC");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dbFile.getAbsolutePath());
        } catch (Exception e) {
            plugin.getLogger().severe("Could not connect to SQLite database!");
            e.printStackTrace();
        }
    }

    private void setupTables() {
        try (Statement s = connection.createStatement()) {
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

            s.execute("CREATE TABLE IF NOT EXISTS kit_layouts (" +
                    "uuid TEXT, " +
                    "mode TEXT, " +
                    "layout TEXT, " +
                    "PRIMARY KEY (uuid, mode))");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void savePlayer(UUID uuid, String name, int kills, int deaths, int curStreak, int bestStreak, int level, int xp, List<String> ranks) {
        String ranksStr = String.join(",", ranks);
        String sql = "INSERT INTO player_stats(uuid, name, kills, deaths, current_streak, best_streak, level, xp, ranks) " +
                "VALUES(?,?,?,?,?,?,?,?,?) ON CONFLICT(uuid) DO UPDATE SET " +
                "name=excluded.name, kills=excluded.kills, deaths=excluded.deaths, " +
                "current_streak=excluded.current_streak, best_streak=excluded.best_streak, " +
                "level=excluded.level, xp=excluded.xp, ranks=excluded.ranks";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

    public void saveKitLayout(UUID uuid, String mode, Map<Material, Integer> layout) {
        if (layout.isEmpty()) return;

        StringBuilder sb = new StringBuilder();
        layout.forEach((mat, slot) -> sb.append(mat.name()).append(":").append(slot).append(","));

        String sql = "INSERT INTO kit_layouts(uuid, mode, layout) VALUES(?,?,?) " +
                "ON CONFLICT(uuid, mode) DO UPDATE SET layout=excluded.layout";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
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

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            pstmt.setString(2, mode);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                String raw = rs.getString("layout");
                if (raw != null && !raw.isEmpty()) {
                    for (String entry : raw.split(",")) {
                        if (entry.contains(":")) {
                            String[] parts = entry.split(":");
                            layout.put(Material.valueOf(parts[0]), Integer.parseInt(parts[1]));
                        }
                    }
                }
            }
        } catch (Exception e) {
            // Log error or handle empty
        }
        return layout;
    }

    public Map<String, Integer> getTop10(String column) {
        Map<String, Integer> top = new LinkedHashMap<>();
        String sql = "SELECT name, " + column + " FROM player_stats ORDER BY " + column + " DESC LIMIT 10";

        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            ResultSet rs = pstmt.executeQuery();
            while (rs.next()) {
                top.put(rs.getString("name"), rs.getInt(column));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return top;
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