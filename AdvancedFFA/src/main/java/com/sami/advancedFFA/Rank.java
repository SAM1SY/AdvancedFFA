    package com.sami.advancedFFA;

    public enum Rank {
        OWNER("§4§lOWNER"),
        DEV("§5§lDEV"),
        MANAGER("§c§lMANAGER"),
        MOD("§9§lMOD"),
        HELPER("§a§lHELPER"),
        MVP("§6§lMVP"),
        ELITE("§d§lELITE"),
        VIP("§e§lVIP"),
        MEMBER("§7MEMBER");

        private final String display;

        Rank(String display) {
            this.display = display;
        }
        public String getDisplay() {
            return display;
        }
        public String getPrefix() {
            return display + " §r";
        }
    }