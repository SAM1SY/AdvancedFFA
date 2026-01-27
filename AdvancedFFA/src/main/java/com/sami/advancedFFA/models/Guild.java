package com.sami.advancedFFA.models;

import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import java.util.*;

public class Guild {
    private String name;
    private String tag;
    private String tagColor = "§b";
    private UUID leader;
    private final Set<UUID> coFounders = new HashSet<>();
    private final Set<UUID> officers = new HashSet<>();
    private final Set<UUID> members = new HashSet<>();

    private int points = 0;
    private double coins = 0.0;
    private List<String> ownedColors = new ArrayList<>();

    public Guild(String name, String tag, UUID leader) {
        this.name = name;
        this.tag = tag;
        this.leader = leader;
        this.members.add(leader);
    }

    public double calculateMultiplier() {
        double multiplier = 1.0;
        boolean hasVip = false, hasElite = false, hasMvp = false;

        for (UUID uuid : members) {
            Player p = Bukkit.getPlayer(uuid);
            if (p == null) continue;
            if (p.hasPermission("group.mvp")) hasMvp = true;
            else if (p.hasPermission("group.elite")) hasElite = true;
            else if (p.hasPermission("group.vip")) hasVip = true;
        }

        if (hasVip) multiplier += 0.1;
        if (hasElite) multiplier += 0.2;
        if (hasMvp) multiplier += 0.25;
        return multiplier;
    }

    public void addPoints(int amount) {
        int oldPoints = this.points;
        this.points += amount;

        if (this.points >= 300) {
            int oldMilestone = Math.max(2, (oldPoints) / 100);
            int newMilestone = (this.points) / 100;
            if (newMilestone > oldMilestone) {
                this.coins += (newMilestone - oldMilestone);
            }
        }
    }

    public void setPoints(int points) { this.points = points; }
    public void setCoins(double coins) { this.coins = coins; }
    public void setTagColor(String tagColor) { this.tagColor = tagColor; }
    public void addOwnedColor(String hexPair) {
        if (!ownedColors.contains(hexPair)) { ownedColors.add(hexPair); } }
    public boolean ownsColor(String hexPair) { return ownedColors.contains(hexPair); }
    public String getOwnedColorsString() { return String.join(",", ownedColors); }
    public void setOwnedColorsFromString(String data) {
        if (data == null || data.isEmpty()) return;
        this.ownedColors = new ArrayList<>(Arrays.asList(data.split(","))); }

    public String getRole(UUID uuid) {
        if (uuid.equals(leader)) return "Leader";
        if (coFounders.contains(uuid)) return "Co-Founder";
        if (officers.contains(uuid)) return "Officer";
        return "Member";
    }

    public int getRoleWeight(UUID uuid) {
        if (uuid.equals(leader)) return 3;
        if (coFounders.contains(uuid)) return 2;
        if (officers.contains(uuid)) return 1;
        return 0;
    }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getTag() { return tag; }
    public void setTag(String tag) { this.tag = tag; }
    public String getTagColor() { return tagColor; }
    public UUID getLeader() { return leader; }
    public void setLeader(UUID leader) { this.leader = leader; }
    public Set<UUID> getMembers() { return members; }
    public Set<UUID> getCoFounders() { return coFounders; }
    public Set<UUID> getOfficers() { return officers; }
    public int getPoints() { return points; }
    public double getCoins() { return coins; }
}