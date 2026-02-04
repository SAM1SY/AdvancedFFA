package com.sami.advancedFFA.commands;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.menus.GuildShop;
import com.sami.advancedFFA.models.Guild;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import java.util.UUID;

public class GuildCommand implements CommandExecutor {
    private final Main plugin;

    public GuildCommand(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player)) return true;
        Player p = (Player) sender;
        if (!sender.hasPermission("ffa.guild.use")) return true;
        if (args.length == 0) { sendHelp(p); return true; }

        String sub = args[0].toLowerCase();
        Guild myGuild = plugin.getGuildManager().getGuild(p.getUniqueId());

        switch (sub) {
            case "create":
                if (args.length < 3) { p.sendMessage("§cUsage: /g create <name> <tag>"); return true; }
                String tag = args[2];
                if (tag.length() > 3) { p.sendMessage("§c§lERROR §8» §7Guild tags cannot exceed 3 characters."); return true; }

                if (plugin.getGuildManager().createGuild(p, args[1], tag)) {
                    p.sendMessage("§a§lSUCCESS §8» §7Guild created with tag §b" + tag);
                } else {
                    p.sendMessage("§c§lERROR §8» §7Already in a guild or name taken.");
                }
                break;

            case "settag":
                if (myGuild == null || myGuild.getRoleWeight(p.getUniqueId()) < 3) {
                    p.sendMessage("§cOnly the Leader can change the tag.");
                    return true;
                }
                if (args.length < 2) { p.sendMessage("§cUsage: /g settag <3-char-tag>"); return true; }
                String newTag = args[1];
                if (newTag.length() > 3) {
                    p.sendMessage("§c§lERROR §8» §7Guild tags cannot exceed 3 characters.");
                    return true;
                }
                myGuild.setTag(newTag);
                p.sendMessage("§a§lSUCCESS §8» §7Tag changed to: " + myGuild.getTagColor() + newTag);
                break;

            case "color":
                if (myGuild == null || myGuild.getRoleWeight(p.getUniqueId()) < 2) { p.sendMessage("§cOnly Co-Founders+ can change color."); return true; }
                if (args.length < 2) { p.sendMessage("§bColors (5 Coins): §fred, green, gold, purple, white"); return true; }
                handleColorChange(p, myGuild, args[1]);
                break;

            case "invite":
                if (args.length < 2) return true;
                handleInvite(p, myGuild, args[1]);
                break;

            case "join":
                if (args.length < 2) return true;
                handleJoin(p, args[1]);
                break;

            case "info":
                handleInfo(p, myGuild, args);
                break;

            case "chat":
                boolean toggled = plugin.getGuildManager().toggleGuildChat(p);
                p.sendMessage("§b§lGUILD §8» §7Chat: " + (toggled ? "§aGUILD" : "§7GLOBAL"));
                break;

            case "disband":
                if (myGuild != null && myGuild.getLeader().equals(p.getUniqueId())) {
                    plugin.getGuildManager().disbandGuild(myGuild.getName());
                    p.sendMessage("§cGuild disbanded.");
                }
                break;

            case "leave":
                handleLeave(p, myGuild);
                break;

            case "kick":
                if (args.length < 2) { p.sendMessage("§cUsage: /g kick <player>"); return true; }
                handleKick(p, myGuild, args[1]);
                break;

            case "promote":
                if (args.length < 2) { p.sendMessage("§cUsage: /g promote <player>"); return true; }
                handlePromotion(p, myGuild, args[1], true);
                break;

            case "demote":
                if (args.length < 2) { p.sendMessage("§cUsage: /g demote <player>"); return true; }
                handlePromotion(p, myGuild, args[1], false);
                break;

            case "transfer":
                if (args.length < 2) { p.sendMessage("§cUsage: /g transfer <player>"); return true; }
                handleTransfer(p, myGuild, args[1]);
                break;

            case "shop":
                if (myGuild == null) {
                    p.sendMessage("§cYou must be in a guild to open the shop.");
                    return true;
                }
                if (!myGuild.getLeader().equals(p.getUniqueId())) {
                    p.sendMessage("§cOnly the Guild Leader can purchase tag colors.");
                    return true;
                }
                new GuildShop(plugin).open(p);
                break;

            default:
                sendHelp(p);
                break;
        }
        return true;
    }

    private void handleColorChange(Player p, Guild g, String colorName) {
        if (g.getCoins() < 5) { p.sendMessage("§cYou need 5 Coins!"); return; }
        String code;
        switch(colorName.toLowerCase()) {
            case "red": code = "§c"; break;
            case "green": code = "§a"; break;
            case "gold": code = "§6"; break;
            case "purple": code = "§d"; break;
            case "white": code = "§f"; break;
            default: p.sendMessage("§cUnknown color."); return;
        }
        g.setCoins(g.getCoins() - 5);
        g.setTagColor(code);
        p.sendMessage("§aTag color updated!");
    }

    private void handleInvite(Player p, Guild g, String target) {
        if (g == null || g.getRoleWeight(p.getUniqueId()) < 1) return;
        Player t = Bukkit.getPlayer(target);
        if (t != null) {
            plugin.getGuildManager().getInvites().put(t.getUniqueId(), g.getName());
            p.sendMessage("§aInvited " + t.getName());
        }
    }

    private void handleJoin(Player p, String name) {
        String invited = plugin.getGuildManager().getInvites().get(p.getUniqueId());
        if (invited != null && invited.equalsIgnoreCase(name)) {
            Guild g = plugin.getGuildManager().getGuilds().get(name.toLowerCase());
            if (g != null) {
                g.getMembers().add(p.getUniqueId());
                plugin.getGuildManager().addPlayerToCache(p.getUniqueId(), g.getName());
                p.sendMessage("§aJoined!");
            }
        }
    }

    private void handleLeave(Player p, Guild g) {
        if (g == null) { p.sendMessage("§cYou are not in a guild."); return; }
        if (g.getLeader().equals(p.getUniqueId())) {
            p.sendMessage("§cLeaders cannot leave. Use /g transfer or /g disband.");
            return;
        }

        g.getMembers().remove(p.getUniqueId());
        g.getCoFounders().remove(p.getUniqueId());
        g.getOfficers().remove(p.getUniqueId());
        plugin.getGuildManager().removePlayerFromCache(p.getUniqueId());
        plugin.getDatabaseManager().removeMember(p.getUniqueId());

        p.sendMessage("§7You left the guild.");
        plugin.getGuildManager().broadcast(g, "§e" + p.getName() + " §7has left the guild.");
    }

    private void handleKick(Player p, Guild g, String targetName) {
        if (g == null || g.getRoleWeight(p.getUniqueId()) < 1) {
            p.sendMessage("§cOfficers and above only."); return;
        }

        Player target = Bukkit.getPlayer(targetName);
        UUID targetUUID = (target != null) ? target.getUniqueId() : Bukkit.getOfflinePlayer(targetName).getUniqueId();

        if (!g.getMembers().contains(targetUUID)) { p.sendMessage("§cPlayer not in guild."); return; }

        // Hierarchy Check: Can't kick someone with equal or higher rank
        if (g.getRoleWeight(p.getUniqueId()) <= g.getRoleWeight(targetUUID)) {
            p.sendMessage("§cYou cannot kick someone of equal or higher rank.");
            return;
        }

        g.getMembers().remove(targetUUID);
        g.getCoFounders().remove(targetUUID);
        g.getOfficers().remove(targetUUID);
        plugin.getGuildManager().removePlayerFromCache(targetUUID);
        plugin.getDatabaseManager().removeMember(targetUUID);

        plugin.getGuildManager().broadcast(g, "§c" + targetName + " was kicked by " + p.getName());
        if (target != null) target.sendMessage("§cYou were kicked from the guild.");
    }

    private void handlePromotion(Player p, Guild g, String targetName, boolean promote) {
        if (g == null || g.getRoleWeight(p.getUniqueId()) < 2) {
            p.sendMessage("§cCo-Founders+ only."); return;
        }

        OfflinePlayer target = Bukkit.getOfflinePlayer(targetName);
        UUID tUUID = target.getUniqueId();

        if (!g.getMembers().contains(tUUID)) { p.sendMessage("§cPlayer not in guild."); return; }
        if (tUUID.equals(p.getUniqueId())) { p.sendMessage("§cYou cannot promote/demote yourself."); return; }

        int currentWeight = g.getRoleWeight(tUUID);

        if (promote) {
            if (currentWeight >= 2) { p.sendMessage("§cCannot promote further."); return; }
            if (currentWeight == 0) g.getOfficers().add(tUUID);
            else if (currentWeight == 1) {
                if (!g.getLeader().equals(p.getUniqueId())) { p.sendMessage("§cOnly the Leader can promote to Co-Founder."); return; }
                g.getOfficers().remove(tUUID);
                g.getCoFounders().add(tUUID);
            }
            plugin.getGuildManager().broadcast(g, "§a" + targetName + " has been promoted to " + g.getRole(tUUID) + "!");
        } else {
            if (currentWeight == 0) { p.sendMessage("§cCannot demote further."); return; }
            if (currentWeight >= g.getRoleWeight(p.getUniqueId())) { p.sendMessage("§cRank too high to demote."); return; }

            g.getOfficers().remove(tUUID);
            g.getCoFounders().remove(tUUID);
            plugin.getGuildManager().broadcast(g, "§e" + targetName + " has been demoted to Member.");
        }
    }

    private void handleTransfer(Player p, Guild g, String targetName) {
        if (g == null || !g.getLeader().equals(p.getUniqueId())) {
            p.sendMessage("§cOnly the Leader can transfer ownership."); return;
        }

        Player target = Bukkit.getPlayer(targetName);
        if (target == null || !g.getMembers().contains(target.getUniqueId())) {
            p.sendMessage("§cTarget must be online and in the guild."); return;
        }

        g.setLeader(target.getUniqueId());
        g.getCoFounders().remove(target.getUniqueId());
        g.getOfficers().remove(target.getUniqueId());

        // Previous leader becomes a Co-Founder
        g.getCoFounders().add(p.getUniqueId());

        plugin.getGuildManager().broadcast(g, "§6§l" + target.getName() + " is the new Guild Leader!");
    }

    private void handleInfo(Player p, Guild g, String[] args) {
        if (g == null && args.length > 1) g = plugin.getGuildManager().getGuilds().get(args[1].toLowerCase());
        if (g == null) return;
        p.sendMessage("§8§m-----------------------");
        p.sendMessage("§b§l" + g.getName() + " §8[" + g.getTagColor() + g.getTag() + "§8]");
        p.sendMessage("§7Leader: §f" + Bukkit.getOfflinePlayer(g.getLeader()).getName());
        p.sendMessage("§7Points: §e" + g.getPoints());
        p.sendMessage("§7Coins: §6" + (int)g.getCoins());
        p.sendMessage("§7Multiplier: §bx" + g.calculateMultiplier());
        p.sendMessage("§8§m-----------------------");
    }

    private void sendHelp(Player p) {
        p.sendMessage("§b§lGUILD COMMANDS: §f/g create, disband, info, chat, color, invite, join");
    }
}