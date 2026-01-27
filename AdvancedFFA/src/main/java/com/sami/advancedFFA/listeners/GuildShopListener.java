package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.Guild;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class GuildShopListener implements Listener {
    private final Main plugin;
    private final Map<UUID, Long> cooldown = new HashMap<>();

    public GuildShopListener(Main plugin) { this.plugin = plugin; }

    @EventHandler
    public void onGuildShopClick(InventoryClickEvent e) {
        if (!e.getView().getTitle().equals("§8Guild Tag Color Shop")) return;
        e.setCancelled(true);
        Player p = (Player) e.getWhoClicked();
        ItemStack item = e.getCurrentItem();
        if (item == null || item.getType() == Material.GRAY_STAINED_GLASS_PANE) return;

        Guild g = plugin.getGuildManager().getGuild(p.getUniqueId());
        if (g == null || !g.getLeader().equals(p.getUniqueId())) return;

        String name = item.getItemMeta().getDisplayName();
        String hexPair; int cost;

        switch (name) {
            case "§6§lThe Supreme": hexPair = "#B8860B:#FFFACD"; cost = 150; break;
            case "§4§lDragon": hexPair = "#800000:#FF8000"; cost = 30; break;
            case "§f§lSnow White": hexPair = "#FFFFFF:#B0E0E6"; cost = 20; break;
            case "§c§lPure Red": hexPair = "#FF0000:#990000"; cost = 20; break;
            case "§b§lKitsune Blue": hexPair = "#00B4DB:#0083B0"; cost = 25; break;
            case "§5§lCelestial": hexPair = "#C33764:#1D2671"; cost = 40; break;
            case "§d§lLight Pink": hexPair = "#FFB6C1:#FF69B4"; cost = 30; break;
            default: return;
        }

        if (g.ownsColor(hexPair)) {
            g.setTagColor(hexPair);
            p.sendMessage("§a§lSUCCESS §8» §7Applied " + name);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_CHIME, 1f, 1.5f);
        } else if (g.getCoins() >= cost) {
            g.setCoins(g.getCoins() - cost);
            g.addOwnedColor(hexPair);
            g.setTagColor(hexPair);
            p.sendMessage("§a§lPURCHASED §8» §7Unlocked " + name);
            p.playSound(p.getLocation(), Sound.ENTITY_PLAYER_LEVELUP, 1f, 2f);
        } else {
            p.sendMessage("§c§lERROR §8» §7Insufficient coins!");
            return;
        }

        plugin.getDatabaseManager().saveGuild(g);
        plugin.getGuildShop().open(p);
    }
}