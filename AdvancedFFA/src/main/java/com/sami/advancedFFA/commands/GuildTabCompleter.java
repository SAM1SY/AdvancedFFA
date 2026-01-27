package com.sami.advancedFFA.commands;

import com.sami.advancedFFA.Main;
import com.sami.advancedFFA.models.Guild;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.bukkit.util.StringUtil;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class GuildTabCompleter implements TabCompleter {

    private final Main plugin;
    private final List<String> subs = Arrays.asList(
            "create", "info", "invite", "join", "leave",
            "kick", "promote", "demote", "transfer", "disband", "chat", "color", "shop"
    );

    public GuildTabCompleter(Main plugin) {
        this.plugin = plugin;
    }

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        if (!(sender instanceof Player)) return Collections.emptyList();
        Player player = (Player) sender;
        List<String> completions = new ArrayList<>();

        if (args.length == 1) {
            StringUtil.copyPartialMatches(args[0], subs, completions);
        }

        else if (args.length == 2) {
            String sub = args[0].toLowerCase();
            Guild guild = plugin.getGuildManager().getGuild(player.getUniqueId());

            switch (sub) {
                case "invite":
                case "join":
                    List<String> players = Bukkit.getOnlinePlayers().stream()
                            .map(Player::getName)
                            .collect(Collectors.toList());
                    StringUtil.copyPartialMatches(args[1], players, completions);
                    break;

                case "kick":
                case "promote":
                case "demote":
                case "transfer":
                    if (guild != null) {
                        List<String> members = guild.getMembers().stream()
                                .map(uuid -> Bukkit.getOfflinePlayer(uuid).getName())
                                .filter(name -> name != null)
                                .collect(Collectors.toList());
                        StringUtil.copyPartialMatches(args[1], members, completions);
                    }
                    break;

                case "color":
                    List<String> colors = Arrays.asList("red", "green", "gold", "purple", "white");
                    StringUtil.copyPartialMatches(args[1], colors, completions);
                    break;
            }
        }

        Collections.sort(completions);
        return completions;
    }
}