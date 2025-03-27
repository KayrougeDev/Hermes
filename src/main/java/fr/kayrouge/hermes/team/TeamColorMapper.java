package fr.kayrouge.hermes.team;

import org.bukkit.Color;
import org.bukkit.Material;

import java.util.*;

public class TeamColorMapper {

    private static final Map<Color, Material> COLOR_TO_BLOCK = new HashMap<>();

    static {
        COLOR_TO_BLOCK.put(Color.WHITE, Material.WHITE_CONCRETE);
        COLOR_TO_BLOCK.put(Color.SILVER, Material.LIGHT_GRAY_CONCRETE);
        COLOR_TO_BLOCK.put(Color.GRAY, Material.GRAY_CONCRETE);
        COLOR_TO_BLOCK.put(Color.BLACK, Material.BLACK_CONCRETE);
        COLOR_TO_BLOCK.put(Color.RED, Material.RED_CONCRETE);
        COLOR_TO_BLOCK.put(Color.GREEN, Material.LIME_CONCRETE);
        COLOR_TO_BLOCK.put(Color.BLUE, Material.BLUE_CONCRETE);
        COLOR_TO_BLOCK.put(Color.YELLOW, Material.YELLOW_CONCRETE);
        COLOR_TO_BLOCK.put(Color.AQUA, Material.LIGHT_BLUE_CONCRETE);
        COLOR_TO_BLOCK.put(Color.ORANGE, Material.ORANGE_CONCRETE);
        COLOR_TO_BLOCK.put(Color.PURPLE, Material.PURPLE_CONCRETE);
        COLOR_TO_BLOCK.put(Color.FUCHSIA, Material.MAGENTA_CONCRETE);
        COLOR_TO_BLOCK.put(Color.OLIVE, Material.BROWN_CONCRETE);
        COLOR_TO_BLOCK.put(Color.NAVY, Material.BLUE_CONCRETE);
        COLOR_TO_BLOCK.put(Color.TEAL, Material.CYAN_CONCRETE);
        COLOR_TO_BLOCK.put(Color.LIME, Material.LIME_CONCRETE);
    }

    public static Material getMaterialFromColor(Color color) {
        return COLOR_TO_BLOCK.getOrDefault(color, Material.WHITE_CONCRETE);
    }

    public static Color getRandomColor() {
        List<Color> keys = new ArrayList<>(COLOR_TO_BLOCK.keySet());
        Random random = new Random();
        return keys.get(random.nextInt(keys.size()));
    }

}
