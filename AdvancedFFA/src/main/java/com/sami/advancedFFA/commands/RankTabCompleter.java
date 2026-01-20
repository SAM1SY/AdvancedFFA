package com.sami.advancedFFA.commands;

import com.sami.advancedFFA.Rank;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class RankTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(@NotNull CommandSender s, @NotNull Command c, @NotNull String a, String[] args) {
        if (args.length == 1) return Arrays.asList("set", "add", "rem", "reset", "get").stream().filter(x -> x.startsWith(args[0])).collect(Collectors.toList());
        if (args.length == 2) return null;
        if (args.length == 3) {
            List<String> r = new ArrayList<>();
            for (Rank rank : Rank.values()) r.add(rank.name());
            return r.stream().filter(x -> x.startsWith(args[2].toUpperCase())).collect(Collectors.toList());
        }
        return new ArrayList<>();
    }
}