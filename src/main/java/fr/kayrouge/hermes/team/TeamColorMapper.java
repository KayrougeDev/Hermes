package fr.kayrouge.hermes.team;

import org.bukkit.ChatColor;
import org.bukkit.Material;

import java.util.*;

public class TeamColorMapper {

    private static final Map<ChatColor, Material> COLOR_TO_BLOCK = new HashMap<>();

    static {
        COLOR_TO_BLOCK.put(ChatColor.WHITE, Material.WHITE_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.BLACK, Material.BLACK_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.DARK_BLUE, Material.BLUE_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.DARK_GREEN, Material.GREEN_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.DARK_AQUA, Material.CYAN_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.DARK_RED, Material.RED_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.DARK_PURPLE, Material.PURPLE_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.GOLD, Material.ORANGE_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.GRAY, Material.LIGHT_GRAY_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.DARK_GRAY, Material.GRAY_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.BLUE, Material.LIGHT_BLUE_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.GREEN, Material.LIME_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.AQUA, Material.LIGHT_BLUE_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.RED, Material.RED_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.LIGHT_PURPLE, Material.MAGENTA_CONCRETE);
        COLOR_TO_BLOCK.put(ChatColor.YELLOW, Material.YELLOW_CONCRETE);
    }

    public static Material getMaterialFromColor(ChatColor color) {
        return COLOR_TO_BLOCK.getOrDefault(color, Material.WHITE_CONCRETE);
    }

    public static ChatColor getRandomColor() {
        List<ChatColor> keys = new ArrayList<>(COLOR_TO_BLOCK.keySet());
        keys.remove(ChatColor.WHITE);
        return keys.get(new Random().nextInt(keys.size()));
    }

}
