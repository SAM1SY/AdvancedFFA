package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerCommandPreprocessEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;

public class SettingsListener implements Listener {

    private final Main plugin;

    public SettingsListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        if (plugin.getSettingsMenu().getSetting(p, "global_chat", 1) == 0) {
            p.sendMessage("§c§lERROR §8» §7You have global chat disabled!");
            e.setCancelled(true);
            return;
        }

        e.getRecipients().removeIf(recipient ->
                plugin.getSettingsMenu().getSetting(recipient, "global_chat", 1) == 0);

        String levelColor = plugin.getStatsManager().getLevelColor(p.getUniqueId());
        int level = plugin.getStatsManager().getLevel(p.getUniqueId());
        String rankPrefix = plugin.getStatsManager().getHighestRank(p.getUniqueId()).getPrefix();
        int streak = plugin.getStatsManager().getStreak(p.getUniqueId());
        String streakDisplay = (streak > 0) ? " §6" + streak : "";

        e.setFormat(levelColor + "[" + level + "] " + rankPrefix + "§f" + p.getName() + streakDisplay + "§8: §f%2$s");

        String message = e.getMessage().toLowerCase();
        for (Player online : e.getRecipients()) {
            if (message.contains(online.getName().toLowerCase()) && !online.equals(p)) {
                if (plugin.getSettingsMenu().getSetting(online, "mention_sound", 1) == 1) {
                    online.playSound(online.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1f);
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onPrivateMessage(PlayerCommandPreprocessEvent e) {
        String message = e.getMessage().toLowerCase();
        String[] args = message.split(" ");
        if (args.length < 1) return;

        Player sender = e.getPlayer();
        boolean isMsgCmd = args[0].equals("/msg") || args[0].equals("/tell") || args[0].equals("/w");
        boolean isReplyCmd = args[0].equals("/r") || args[0].equals("/reply");

        if (isMsgCmd || isReplyCmd) {
            if (plugin.getSettingsMenu().getSetting(sender, "pm_enabled", 1) == 0) {
                sender.sendMessage("§c§lERROR §8» §7You cannot send messages while your PMs are disabled!");
                e.setCancelled(true);
                return;
            }

            Player target = null;
            if (isMsgCmd) {
                if (args.length > 1) target = Bukkit.getPlayer(args[1]);
            } else {
                target = plugin.getLastMessaged(sender);
            }

            if (target != null && target.isOnline()) {
                if (plugin.getSettingsMenu().getSetting(target, "pm_enabled", 1) == 0) {
                    sender.sendMessage("§c§lERROR §8» §7" + target.getName() + " has private messages disabled!");
                    e.setCancelled(true);
                    return;
                }

                plugin.setLastMessaged(target, sender);
                if (plugin.getSettingsMenu().getSetting(target, "pm_sound", 1) == 1) {
                    Player finalTarget = target;
                    Bukkit.getScheduler().runTaskLater(plugin, () ->
                            finalTarget.playSound(finalTarget.getLocation(), Sound.BLOCK_NOTE_BLOCK_BELL, 1f, 1.2f), 2L);
                }
            }
        }
    }

    @EventHandler
    public void onMenuClick(InventoryClickEvent e) {
        if (!(e.getWhoClicked() instanceof Player)) return;
        Player p = (Player) e.getWhoClicked();
        String title = e.getView().getTitle();
        ItemStack clicked = e.getCurrentItem();

        if (clicked == null || !clicked.hasItemMeta()) return;

        if (title.equals("§8Settings")) {
            e.setCancelled(true);
            if (clicked.getType() == Material.PAPER) plugin.getSettingsMenu().openChatSettings(p);
            else if (clicked.getType() == Material.DIAMOND_SWORD) plugin.getSettingsMenu().openGameplaySettings(p);
        }

        else if (title.equals("§8Chat Settings")) {
            e.setCancelled(true);
            if (clicked.getType() == Material.ARROW) {
                plugin.getSettingsMenu().openMainMenu(p);
                return;
            }

            String key = null;
            if (clicked.getType() == Material.EXPERIENCE_BOTTLE) key = "xp_bar";
            else if (clicked.getType() == Material.BELL) {
                key = (clicked.getItemMeta().getDisplayName().contains("Mention")) ? "mention_sound" : "pm_sound";
            } else if (clicked.getType() == Material.OAK_SIGN) key = "global_chat";
            else if (clicked.getType() == Material.BIRCH_SIGN) key = "pm_enabled";

            if (key != null) {
                int val = plugin.getSettingsMenu().getSetting(p, key, 1);
                plugin.getSettingsMenu().setSetting(p, key, val == 1 ? 0 : 1);
                plugin.getSettingsMenu().openChatSettings(p);
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
            }
        }

        else if (title.equals("§8Gameplay Settings")) {
            e.setCancelled(true);
            if (clicked.getType() == Material.ARROW) {
                plugin.getSettingsMenu().openMainMenu(p);
                return;
            }

            if (clicked.getType() == Material.BOOK) {
                int mode = plugin.getSettingsMenu().getSetting(p, "kill_msg_mode", 0);
                plugin.getSettingsMenu().setSetting(p, "kill_msg_mode", (mode + 1) % 3);
                plugin.getSettingsMenu().openGameplaySettings(p);
                p.playSound(p.getLocation(), Sound.ENTITY_ITEM_FRAME_ROTATE_ITEM, 1f, 1f);
            }
            else if (clicked.getType() == Material.FEATHER) {
                if (p.hasPermission("group.elite") || p.hasPermission("group.mvp") || p.isOp()) {
                    boolean currentlyCanFly = p.getAllowFlight();
                    p.setAllowFlight(!currentlyCanFly);

                    if (!currentlyCanFly) {
                        p.setFlying(true);
                        p.sendMessage("§a§lSUCCESS §8» §7Flight §aENABLED§7!");
                    } else {
                        p.setFlying(false); // Make sure they drop
                        p.sendMessage("§c§lSUCCESS §8» §7Flight §cDISABLED§7!");
                    }

                    plugin.getSettingsMenu().openGameplaySettings(p);
                    p.playSound(p.getLocation(), Sound.ENTITY_BAT_LOOP, 1f, 1.5f);
                } else {
                    p.sendMessage("§c§lERROR §8» §7Only §bELITE §7and §aMVP §7can toggle flight!");
                }
            }
            else if (clicked.getType() == Material.CLOCK) {
                int mode = plugin.getSettingsMenu().getSetting(p, "time_mode", 0);
                int nextMode = (mode + 1) % 3;
                plugin.getSettingsMenu().setSetting(p, "time_mode", nextMode);
                if (nextMode == 0) p.resetPlayerTime();
                else if (nextMode == 1) p.setPlayerTime(12500, false);
                else p.setPlayerTime(18000, false);
                p.playSound(p.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1.2f);
                plugin.getSettingsMenu().openGameplaySettings(p);
            }
        }
    }

    @EventHandler
    public void onStarInteract(PlayerInteractEvent e) {
        ItemStack item = e.getItem();
        if (item == null || item.getType() != Material.NETHER_STAR) return;
        Player p = e.getPlayer();
        if (plugin.getCombatListener().isTagged(p)) {
            p.sendMessage("§c§lERROR §8» §7You cannot open settings while in combat!");
            return;
        }
        if (e.getAction().name().contains("RIGHT")) {
            e.setCancelled(true);
            plugin.getSettingsMenu().openMainMenu(p);
            p.playSound(p.getLocation(), Sound.BLOCK_NOTE_BLOCK_XYLOPHONE, 1f, 1f);
        }
    }
}