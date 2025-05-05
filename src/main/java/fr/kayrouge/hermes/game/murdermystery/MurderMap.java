package fr.kayrouge.hermes.game.murdermystery;

import fr.kayrouge.hermes.Hermes;
import lombok.Getter;
import lombok.Setter;
import org.bukkit.Bukkit;
import org.bukkit.World;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Getter
public class MurderMap {

    public static final File mapFolder = new File(Hermes.PLUGIN.getDataFolder(), "MurderMysteryMap");
    public static final List<MurderMap> MAPS = new ArrayList<>();

    @Nullable
    public static MurderMap getMap(String name) {
        if(MAPS.isEmpty()) loadMaps();
        Optional<MurderMap> mapOptional = MAPS.stream().filter(map -> map.getName().equals(name)).findFirst();
        return mapOptional.orElse(null);
    }

    public static void loadMaps() {
        checkAndCreateFolder();
        MAPS.clear();

        if (mapFolder.exists() && mapFolder.isDirectory()) {
            File[] ymlFiles = mapFolder.listFiles((dir, fileName) -> fileName.toLowerCase().endsWith(".yml"));

            if (ymlFiles == null) {
                Hermes.LOGGER.info("No map find in MurderMysteryMap folder !");
                return;
            }

            for (File ymlFile : ymlFiles) {
                YamlConfiguration mapData = YamlConfiguration.loadConfiguration(ymlFile);
                String name = ymlFile.getName().substring(0, ymlFile.getName().length()-4);
                int maxPlayerCount = mapData.getInt("maxPlayerCount", -1);
                int maxMurderCount = mapData.getInt("maxMurderCount", -1);
                String worldName = mapData.getString("world");
                World world = null;
                if(worldName != null) world = Bukkit.getWorld(worldName);
                if(name.isEmpty() || maxMurderCount == -1 || maxPlayerCount == -1 || world == null) {
                    Hermes.LOGGER.warning("Can't load mmmap: "+ymlFile.getName());
                    return;
                }

                MurderMap map = new MurderMap(name, maxPlayerCount, maxMurderCount, world);

                map.setDisplayName(mapData.getString("displayName", map.getName()));

                ConfigurationSection section = mapData.getConfigurationSection("spawnAreas");
                if (section != null) {
                    for(String key : section.getKeys(false)) {
                        ConfigurationSection area = section.getConfigurationSection(key);
                        if(area == null) continue;
                        int radius = area.getInt("radius", -1);
                        int x = area.getInt("x", 0);
                        int y = area.getInt("y", world.getMinHeight());
                        int z = area.getInt("z", 0);

                        if(radius == -1 || y == world.getMinHeight()) {
                            Hermes.LOGGER.warning("Invalid spawn area '"+key+"' in "+ymlFile.getName());
                            continue;
                        }
                        map.getSpawnAreas().add(new SpawnArea(key, x, y, z, radius));
                    }
                }

                if(map.getSpawnAreas().isEmpty()) {
                    Hermes.LOGGER.warning("No spawn areas for map '"+name+"'");
                    return;
                }
                MAPS.add(map);
            }
        }
    }

    private final String name;
    private final World world;
    private final int maxPlayerCount;
    private final int maxMurderCount;
    private final List<SpawnArea> spawnAreas = new ArrayList<>();
    @Setter
    private String displayName;

    private MurderMap(String name, int maxPlayerCount, int maxMurderCount, World world) {
        this.name = name;
        this.maxMurderCount = maxMurderCount;
        this.maxPlayerCount = maxPlayerCount;
        this.displayName = name;
        this.world = world;
    }


    public static class SpawnArea {
        public final String name;
        public final int x;
        public final int y;
        public final int z;
        public final int radius;
        public SpawnArea(String name, int x, int y, int z, int radius) {
            this.name = name;
            this.x = x;
            this.y = y;
            this.z = z;
            this.radius = radius;
        }
    }

    private static void checkAndCreateFolder() {
        if(!mapFolder.exists()) {
            if(!mapFolder.mkdir()) {
                Hermes.LOGGER.warning("Unable to create murder mystery map folder !");
            }
        }
    }
}
