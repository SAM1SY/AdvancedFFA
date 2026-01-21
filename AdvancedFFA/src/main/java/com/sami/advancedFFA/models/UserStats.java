package com.sami.advancedFFA.models;

import java.util.UUID;

public class UserStats {
    private final UUID uuid;
    private int kills;
    private int deaths;
    private int xp;
    private int level;

    public UserStats(UUID uuid, int kills, int deaths, int xp, int level) {
        this.uuid = uuid;
        this.kills = kills;
        this.deaths = deaths;
        this.xp = xp;
        this.level = level;
    }

    public UUID getUuid() { return uuid; }
    public int getKills() { return kills; }
    public int getDeaths() { return deaths; }
    public int getXp() { return xp; }
    public int getLevel() { return level; }

    public void addKill() { this.kills++; }
    public void addDeath() { this.deaths++; }

    public boolean addXp(int amount) {
        this.xp += amount;
        int nextLevel = xp / 500; // Example: 500 XP per level
        if (nextLevel > this.level) {
            this.level = nextLevel;
            return true;
        }
        return false;
    }

    public void setLevel(int level) { this.level = level; }
}