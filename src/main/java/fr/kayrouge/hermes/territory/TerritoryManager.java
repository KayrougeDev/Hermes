package fr.kayrouge.hermes.territory;

import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.team.Team;
import fr.kayrouge.hermes.team.TeamColorMapper;
import fr.kayrouge.hermes.util.FakeBlockUtils;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TerritoryManager {
    private final HashMap<String, String> territoryChunks = new HashMap<>(); // Chunk conquis ("chunkX,chunkZ" -> team)
    private final HashMap<String, HashMap<String, Set<Integer>>> territoryBlocks = new HashMap<>(); // Chunks contestés ("chunkX,chunkZ" -> Map<team, Set<blocXZ>>)

    private final World world;

    public TerritoryManager(World world) {
        this.world = world;
    }

    // Capture un bloc (sans Y)
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

        // Ajouter le bloc dans la bonne équipe
        territoryBlocks.putIfAbsent(chunkKey, new HashMap<>());
        territoryBlocks.get(chunkKey).putIfAbsent(team.getName(), new HashSet<>());
        territoryBlocks.get(chunkKey).get(team.getName()).add(blockXZ);

        // Vérifier si tout le chunk est capturé par une seule équipe
        checkChunkCapture(chunkKey);
    }

    // Vérifie si un chunk est totalement conquis
    private void checkChunkCapture(String chunkKey) {
        HashMap<String, Set<Integer>> teamsBlocks = territoryBlocks.get(chunkKey);
        if (teamsBlocks == null || teamsBlocks.isEmpty()) return;

        // Vérifier si une équipe a capturé tous les blocs du chunk (16x16 = 256 blocs)
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

    public void updateAllBlocks(Team team) {
        for (String chunkKey : territoryBlocks.keySet()) {
            HashMap<String, Set<Integer>> teamsBlocks = territoryBlocks.get(chunkKey);
            if (teamsBlocks == null || !teamsBlocks.containsKey(team.getName())) continue;

            for (int blockXZ : teamsBlocks.get(team.getName())) {
                int x = ((Integer.bitCount(blockXZ >> 4)) << 4) | (blockXZ >> 4);
                int z = blockXZ & 15;

                Block highestBlock = world.getHighestBlockAt(x, z);
                Location location = new Location(world, x, highestBlock.getY(), z);
                Hermes.LOGGER.info(location.toString());
                Material material;
                if(team.equals(Team.NEUTRAL)) {
                    material = highestBlock.getType();
                }
                else {
                    material = TeamColorMapper.getMaterialFromColor(team.getColor());
                }
                Bukkit.getOnlinePlayers().forEach(player ->
                        FakeBlockUtils.sendFakeBlock(player, location, material));
            }
        }
    }

    public void updateBlocksForAllTeam() {
        Team.getTeams().forEach((s, team) -> updateAllBlocks(team));
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

    public World getWorld() {
        return world;
    }
}