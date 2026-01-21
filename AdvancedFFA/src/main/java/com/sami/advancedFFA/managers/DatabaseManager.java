package com.sami.advancedFFA.managers;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.UserStats;

import java.io.File;
import java.sql.*;
import java.util.UUID;

public class DatabaseManager {
    private Connection connection;
    private final Main plugin;

    public DatabaseManager(Main plugin) {
        this.plugin = plugin;
        connect();
        createTable();
    }

    private void connect() {
        try {
            File dataFile = new File(plugin.getDataFolder(), "stats.db");
            connection = DriverManager.getConnection("jdbc:sqlite:" + dataFile.getPath());
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void createTable() {
        String sql = "CREATE TABLE IF NOT EXISTS player_stats (" +
                "uuid TEXT PRIMARY KEY," +
                "kills INTEGER DEFAULT 0," +
                "deaths INTEGER DEFAULT 0," +
                "xp INTEGER DEFAULT 0," +
                "level INTEGER DEFAULT 0);";
        try (Statement stmt = connection.createStatement()) {
            stmt.execute(sql);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public UserStats loadPlayerStats(UUID uuid) {
        String sql = "SELECT * FROM player_stats WHERE uuid = ?";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, uuid.toString());
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return new UserStats(uuid, rs.getInt("kills"), rs.getInt("deaths"), rs.getInt("xp"), rs.getInt("level"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return new UserStats(uuid, 0, 0, 0, 0); // Default for new players
    }

    public void savePlayerStats(UserStats stats) {
        String sql = "REPLACE INTO player_stats(uuid, kills, deaths, xp, level) VALUES(?,?,?,?,?)";
        try (PreparedStatement pstmt = connection.prepareStatement(sql)) {
            pstmt.setString(1, stats.getUuid().toString());
            pstmt.setInt(2, stats.getKills());
            pstmt.setInt(3, stats.getDeaths());
            pstmt.setInt(4, stats.getXp());
            pstmt.setInt(5, stats.getLevel());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}