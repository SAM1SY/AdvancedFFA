package com.sami.advancedFFA.listeners;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.Rank;
import com.sami.advancedFFA.models.Guild;
import com.sami.advancedFFA.utils.ColorTag;
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

import java.util.List;
import java.util.UUID;

public class SettingsListener implements Listener {

    private final Main plugin;

    public SettingsListener(Main plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onChat(AsyncPlayerChatEvent e) {
        Player p = e.getPlayer();

        // 1. Guild Chat Check
        if (plugin.getGuildManager().isInGuildChat(p)) {
            Guild g = plugin.getGuildManager().getGuild(p.getUniqueId());
            if (g != null) {
                e.setCancelled(true);
                String message = e.getMessage();
                String format = "§8§l[§b§lGuild Chat§8§l] §f" + p.getName() + "§8: §7" + message;

                for (UUID uuid : g.getMembers()) {
                    Player member = Bukkit.getPlayer(uuid);
                    if (member != null && member.isOnline()) {
                        member.sendMessage(format);
                    }
                }
                return;
            }
        }

        // 2. Global Chat Checks
        if (plugin.getSettingsMenu().getSetting(p, "global_chat", 1) == 0) {
            p.sendMessage("§c§lERROR §8» §7You have global chat disabled!");
            e.setCancelled(true);
            return;
        }

        e.getRecipients().removeIf(recipient ->
                plugin.getSettingsMenu().getSetting(recipient, "global_chat", 1) == 0);

        // 3. Format Global Chat Components
        String levelColor = plugin.getStatsManager().getLevelColor(p.getUniqueId());
        int level = plugin.getStatsManager().getLevel(p.getUniqueId());

        // --- Rank Logic: Hide if MEMBER ---
        Rank rank = plugin.getStatsManager().getHighestRank(p.getUniqueId());
        String rankPrefix = "";
        if (rank != Rank.MEMBER) {
            rankPrefix = rank.getPrefix() + " ";
        }

        int streak = plugin.getStatsManager().getStreak(p.getUniqueId());
        String streakDisplay = (streak > 0) ? " §6" + streak : "";

        // 4. Guild Tag Logic (Animated Snapshot)
        Guild g = plugin.getGuildManager().getGuild(p.getUniqueId());
        String guildDisplay = "";

        if (g != null) {
            String colorData = g.getTagColor();
            String rawTag = g.getTag();

            if (colorData.contains(":")) {
                // Split the hex codes and apply animation frame from Main
                String[] hex = colorData.split(":");
                guildDisplay = "§8§l[" + ColorTag.getAnimatedTag(rawTag, hex[0], hex[1], plugin.getAnimationFrame()) + "§8§l] ";
            } else {
                // Fallback for non-gradient tags
                guildDisplay = "§8§l[" + colorData + "§l" + rawTag + "§8§l] ";
            }
        }

        // 5. Final Format: [%tag%] [%level%] %rank% %player% %streak%: %msg%
        // Note: If rank is Member, rankPrefix is empty, avoiding double spaces.
        e.setFormat(guildDisplay + levelColor + "[" + level + "] §f" + rankPrefix + p.getName() + streakDisplay + "§8: §f%2$s");

        // 6. Mention Sounds
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

            if (clicked.getType() == Material.EXPERIENCE_BOTTLE) {
                if (plugin.getGuildManager().getGuild(p.getUniqueId()) == null) {
                    p.sendMessage("§c§lERROR §8» §7You must be in a guild to toggle guild chat!");
                    p.closeInventory();
                    return;
                }
                boolean nowInGuildChat = plugin.getGuildManager().toggleGuildChat(p);
                p.sendMessage("§b§lGUILD §8» §7Chat mode: " + (nowInGuildChat ? "§bGUILD" : "§aGLOBAL"));
                plugin.getSettingsMenu().openChatSettings(p);
                p.playSound(p.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
                return;
            }

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
                java.util.List<String> playerRanks = plugin.getStatsManager().getRanks(p.getUniqueId());
                if (playerRanks.contains("ELITE") || playerRanks.contains("MVP") || playerRanks.contains("OWNER") || p.isOp()) {
                    boolean willFly = !p.getAllowFlight();
                    p.setAllowFlight(willFly);
                    p.setFlying(willFly);

                    if (willFly) {
                        p.sendMessage("§a§lSUCCESS §8» §7Flight §aENABLED§7!");
                    } else {
                        p.sendMessage("§c§lSUCCESS §8» §7Flight §cDISABLED§7!");
                    }

                    plugin.getSettingsMenu().openGameplaySettings(p);
                    p.playSound(p.getLocation(), Sound.ENTITY_BAT_LOOP, 1f, 1.5f);

                } else {
                    p.sendMessage("§c§lERROR §8» §7Only §bELITE §7and §aMVP §7can toggle flight!");
                    p.closeInventory();
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