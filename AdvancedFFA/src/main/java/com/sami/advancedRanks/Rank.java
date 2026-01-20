package com.sami.advancedRanks;

public enum Rank {
    OWNER("§4§lOWNER§r"),
    DEV("§5§lDEV§r"),
    MANAGER("§c§lMANAGER§r"),
    MOD("§9§lMOD§r"),
    HELPER("§a§lHELPER§r"),
    MVP("§6§lMVP§r"),
    ELITE("§d§lELITE§r"),
    VIP("§e§lVIP§r"),
    MEMBER("§7§lMEMBER§r");

    private final String display;
    Rank(String display) { this.display = display; }
    public String getDisplay() { return display; }
}