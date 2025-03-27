package fr.kayrouge.hermes.territory;

import fr.kayrouge.hermes.team.Team;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;

public class TerritoryManager {
    private final HashMap<String, Team> territoryChunks = new HashMap<>(); // Chunk conquis ("chunkX,chunkZ" -> équipe)
    private final HashMap<String, HashMap<Team, Set<Integer>>> territoryBlocks = new HashMap<>(); // Chunks contestés ("chunkX,chunkZ" -> Map<équipe, Set<blocXZ>>)

    // Capture un bloc (sans Y)
    public void captureBlock(int x, int z, Team team) {
        String chunkKey = (x >> 4) + "," + (z >> 4);
        int blockXZ = ((x & 15) << 4) | (z & 15); // Stocke X et Z dans un seul int

        // Vérifier si le chunk est déjà conquis
        //if (territoryChunks.containsKey(chunkKey)) return; // Chunk déjà capturé

        // Ajouter le bloc dans la bonne équipe
        territoryBlocks.putIfAbsent(chunkKey, new HashMap<>());
        territoryBlocks.get(chunkKey).putIfAbsent(team, new HashSet<>());
        territoryBlocks.get(chunkKey).get(team).add(blockXZ);

        // Vérifier si tout le chunk est capturé par une seule équipe
        checkChunkCapture(chunkKey);
    }

    // Vérifie si un chunk est totalement conquis
    private void checkChunkCapture(String chunkKey) {
        HashMap<Team, Set<Integer>> teamsBlocks = territoryBlocks.get(chunkKey);
        if (teamsBlocks == null || teamsBlocks.isEmpty()) return;

        // Vérifier si une équipe a capturé tous les blocs du chunk (16x16 = 256 blocs)
        for (Team team : teamsBlocks.keySet()) {
            if (teamsBlocks.get(team).size() == 256) {
                territoryChunks.put(chunkKey, team); // Marquer le chunk conquis
                territoryBlocks.remove(chunkKey); // Supprimer les blocs contestés (optimisation)
                break;
            }
        }
    }

    public Team getBlockOwner(int x, int z) {
        String chunkKey = (x >> 4) + "," + (z >> 4);
        int blockXZ = ((x & 15) << 4) | (z & 15);

        if (territoryChunks.containsKey(chunkKey)) {
            return territoryChunks.get(chunkKey);
        }

        for (Team team : territoryBlocks.getOrDefault(chunkKey, new HashMap<>()).keySet()) {
            if (territoryBlocks.get(chunkKey).get(team).contains(blockXZ)) {
                return team;
            }
        }

        return Team.NEUTRAL;
    }
}