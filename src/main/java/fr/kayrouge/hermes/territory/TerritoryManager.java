package fr.kayrouge.hermes.territory;

import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.team.Team;
import fr.kayrouge.hermes.team.TeamColorMapper;
import fr.kayrouge.hermes.util.BlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class TerritoryManager {

    // TODO one game per territory, players can join/spec game
    // TODO player can expand their territory
    private static final HashMap<String, TerritoryManager> territoryManagers = new HashMap<>();

    public static final File TERRITORIES_FILE = new File(Hermes.PLUGIN.getDataFolder(), "territories.yml");

    private final HashMap<String, String> territoryChunks = new HashMap<>(); // Chunk conquis ("chunkX,chunkZ" -> team)
    private final HashMap<String, HashMap<String, Set<Integer>>> territoryBlocks = new HashMap<>(); // Chunks contestés ("chunkX,chunkZ" -> Map<team, Set<blocXZ>>)

    private final World world;

    private final int bottomLeftX;
    private final int bottomLeftY;

    private final int topRightX;
    private final int topRightY;

    private final int xSize;
    private final int ySize;

    private TerritoryManager(World world, int bottomLeftX, int bottomLeftY, int topRightX, int topRightY) {
        this.world = world;
        this.bottomLeftX = bottomLeftX;
        this.bottomLeftY = bottomLeftY;
        this.topRightX = topRightX;
        this.topRightY = topRightY;
        this.xSize = Math.abs(this.topRightX - this.bottomLeftX);
        this.ySize = Math.abs(this.topRightY - this.bottomLeftY);
    }

    public static @Nullable TerritoryManager create(String name, World world, int bottomLeftX, int bottomLeftY, int topRightX, int topRightY) {
        TerritoryManager territoryManager = new TerritoryManager(world, bottomLeftX, bottomLeftY, topRightX, topRightY);
        if(territoryManager.isValid()) {
            territoryManagers.put(name, territoryManager);
            return territoryManager;
        }
        return null;
    }

    public static void save() {
        YamlConfiguration config = new YamlConfiguration();
        for (Map.Entry<String, TerritoryManager> entry : territoryManagers.entrySet()) {
            String key = entry.getKey();
            TerritoryManager territory = entry.getValue();

            ConfigurationSection section = config.createSection(key);
            section.set("world", territory.getWorld().getName());
            section.set("bottomLeft.x", territory.bottomLeftX);
            section.set("bottomLeft.y", territory.bottomLeftY);
            section.set("topRight.x", territory.topRightX);
            section.set("topRight.y", territory.topRightY);

            try {
                config.save(TERRITORIES_FILE);
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    public static void load() {
        territoryManagers.clear();
        YamlConfiguration config = YamlConfiguration.loadConfiguration(TERRITORIES_FILE);

        for (String key : config.getKeys(false)) {
            ConfigurationSection section = config.getConfigurationSection(key);
            if (section == null) continue;

            String worldName = section.getString("world");
            if(worldName == null) continue;
            World configWorld = Bukkit.getWorld(worldName);
            if(configWorld == null) continue;
            int bottomLeftX = section.getInt("bottomLeft.x");
            int bottomLeftY = section.getInt("bottomLeft.y");
            int topRightX = section.getInt("topRight.x");
            int topRightY = section.getInt("topRight.y");

            TerritoryManager.create(key, configWorld, bottomLeftX, bottomLeftY, topRightX, topRightY);
        }
    }

    public boolean isValid() {
        return xSize > 0 && ySize > 0;
    }

    public void captureBlock(int x, int z, Team team) {
        String chunkKey = (x >> 4) + "," + (z >> 4);
        int blockXZ = ((x & 15) << 4) | (z & 15); // Stocke X et Z dans un seul int

        if (territoryBlocks.containsKey(chunkKey)) {
            for (String teamName : new HashSet<>(territoryBlocks.get(chunkKey).keySet())) {
                if (territoryBlocks.get(chunkKey).get(teamName).contains(blockXZ)) {
                    territoryBlocks.get(chunkKey).get(teamName).remove(blockXZ);
                    if (territoryBlocks.get(chunkKey).get(teamName).isEmpty()) {
                        territoryBlocks.get(chunkKey).remove(teamName);
                    }
                    break;
                }
            }
        }

        territoryBlocks.putIfAbsent(chunkKey, new HashMap<>());
        territoryBlocks.get(chunkKey).putIfAbsent(team.getName(), new HashSet<>());
        territoryBlocks.get(chunkKey).get(team.getName()).add(blockXZ);

        checkChunkCapture(chunkKey);
    }


    private void checkChunkCapture(String chunkKey) {
        HashMap<String, Set<Integer>> teamsBlocks = territoryBlocks.get(chunkKey);
        if (teamsBlocks == null || teamsBlocks.isEmpty()) return;

        for (String teamName : teamsBlocks.keySet()) {
            if(!Team.getTeams().containsKey(teamName)) continue;
            Team team = Team.getTeam(teamName);
            if (teamsBlocks.get(team.getName()).size() == 256) {
                territoryChunks.put(chunkKey, team.getName()); // Marquer le chunk conquis
                territoryBlocks.remove(chunkKey); // Supprimer les blocs contestés (optimisation)
                break;
            }
        }
    }

    public void updateAllBlocks(Team team, List<Player> players) {
        for (String chunkKey : territoryBlocks.keySet()) {
            HashMap<String, Set<Integer>> teamsBlocks = territoryBlocks.get(chunkKey);
            if (teamsBlocks == null || !teamsBlocks.containsKey(team.getName())) continue;

            String[] chunkParts = chunkKey.split(",");
            int chunkX = Integer.parseInt(chunkParts[0]);
            int chunkZ = Integer.parseInt(chunkParts[1]);

            for (int blockXZ : teamsBlocks.get(team.getName())) {
                int x = (chunkX << 4) | (blockXZ >> 4);
                int z = (chunkZ << 4) | (blockXZ & 15);

                Block highestBlock = world.getHighestBlockAt(x, z);
                Location location = new Location(world, x, highestBlock.getY(), z);

                Material material;
                if (team.equals(Team.NEUTRAL)) {
                    material = highestBlock.getType();
                } else {
                    material = TeamColorMapper.getMaterialFromColor(team.getColor());
                }

                players.forEach(player -> BlockUtils.sendFakeBlock(player, location, material));
            }
        }
    }

    public void updateBlocksForAllTeam(List<Player> players) {
        Team.getTeams().forEach((s, team) -> {
            updateAllBlocks(team, players);
        });
    }

    public Team getBlockOwner(int x, int z) {
        String chunkKey = (x >> 4) + "," + (z >> 4);
        int blockXZ = ((x & 15) << 4) | (z & 15);

        if (territoryChunks.containsKey(chunkKey)) {
            return Team.getTeam(territoryChunks.get(chunkKey));
        }

        for (String teamName : territoryBlocks.getOrDefault(chunkKey, new HashMap<>()).keySet()) {
            if(!Team.getTeams().containsKey(teamName)) continue;
            Team team = Team.getTeam(teamName);
            if (territoryBlocks.get(chunkKey).get(team.getName()).contains(blockXZ)) {
                return team;
            }
        }

        return Team.NEUTRAL;
    }

    public void reset() {
        territoryBlocks.clear();
        territoryChunks.clear();
    }

    public int getXSize() {
        return xSize;
    }

    public int getYSize() {
        return ySize;
    }

    public int getSize() {
        return getXSize() * getYSize();
    }

    public World getWorld() {
        return world;
    }

    public Location getCenter() {
        return world.getHighestBlockAt((bottomLeftX + topRightX) / 2, (bottomLeftY+topRightY)/2).getLocation().add(0, 1, 0);
    }


    public boolean isBlockInTerritory(int x, int z) {
        int minX = Math.min(bottomLeftX, topRightX);
        int maxX = Math.max(bottomLeftX, topRightX);
        int minY = Math.min(bottomLeftY, topRightY);
        int maxY = Math.max(bottomLeftY, topRightY);
        return x >= minX && x <= maxX && z >= minY && z <= maxY;
    }

    public static HashMap<String, TerritoryManager> getTerritoryManagers() {
        return territoryManagers;
    }
}