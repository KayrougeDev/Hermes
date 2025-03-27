package fr.kayrouge.hermes.util;

import org.bukkit.ChatColor;

public class Style {

    public static String getASCIILogo() {
        return  "   ▄█    █▄       ▄████████    ▄████████   ▄▄▄▄███▄▄▄▄      ▄████████    ▄████████\n"+
                "  ███    ███     ███    ███   ███    ███ ▄██▀▀▀███▀▀▀██▄   ███    ███   ███    ███\n"+
                "  ███    ███     ███    █▀    ███    ███ ███   ███   ███   ███    █▀    ███    █▀ \n"+
                " ▄███▄▄▄▄███▄▄  ▄███▄▄▄      ▄███▄▄▄▄██▀ ███   ███   ███  ▄███▄▄▄       ███       \n"+
                "▀▀███▀▀▀▀███▀  ▀▀███▀▀▀     ▀▀███▀▀▀▀▀   ███   ███   ███ ▀▀███▀▀▀     ▀███████████\n"+
                "  ███    ███     ███    █▄  ▀███████████ ███   ███   ███   ███    █▄           ███\n"+
                "  ███    ███     ███    ███   ███    ███ ███   ███   ███   ███    ███    ▄█    ███\n"+
                "  ███    █▀      ██████████   ███    ███  ▀█   ███   █▀    ██████████  ▄████████▀\n"+
                "                              ███    ███                                         \n";
    }

    public static String getASCIILine() {
        StringBuilder builder = new StringBuilder(getAccentColor().toString());
        for(int i = 0; i < getASCIILogo().split("\n")[0].length(); i++) {
            builder.append("-");
        }
        return getAccentColor().toString()+ builder;
    }

    public static ChatColor getColor() {
        return ChatColor.BLUE;
    }

    public static ChatColor getAccentColor() {
        return ChatColor.AQUA;
    }
}
