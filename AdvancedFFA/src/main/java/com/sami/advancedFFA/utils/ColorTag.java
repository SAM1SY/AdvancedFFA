package com.sami.advancedFFA.utils;

import net.md_5.bungee.api.ChatColor;
import java.awt.Color;

public class ColorTag {

    public static String getAnimatedTag(String tag, String hex1, String hex2, int frame) {
        if (tag == null || tag.isEmpty()) return "";

        StringBuilder builder = new StringBuilder();
        Color c1 = Color.decode(hex1);
        Color c2 = Color.decode(hex2);

        for (int i = 0; i < tag.length(); i++) {
            float wave = (float) Math.sin((frame + (i * 2)) * 0.4);
            float ratio = (wave + 1) / 2; // Converts -1/1 to 0/1 ratio

            int r = (int) (c1.getRed() * (1 - ratio) + c2.getRed() * ratio);
            int g = (int) (c1.getGreen() * (1 - ratio) + c2.getGreen() * ratio);
            int b = (int) (c1.getBlue() * (1 - ratio) + c2.getBlue() * ratio);

            builder.append(ChatColor.of(new Color(r, g, b)).toString())
                    .append("§l")
                    .append(tag.charAt(i));
        }

        return builder.toString();
    }

    public static String getGradientTag(String tag, String hex1, String hex2) {
        return getAnimatedTag(tag, hex1, hex2, 0);
    }
}