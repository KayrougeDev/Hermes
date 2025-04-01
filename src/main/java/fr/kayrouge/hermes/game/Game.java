package fr.kayrouge.hermes.game;

import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public abstract class Game {

    private final String name;

    private final List<UUID> PLAYERS = new ArrayList<>();
    private final List<UUID> SPECTATORS = new ArrayList<>();

    public Game(String name) {
        this.name = name;
    }

    public boolean join(Player player, boolean spectator, Runnable onJoin) {
        if(spectator) {
            if(PLAYERS.contains(player.getUniqueId())) {
                quit(player);
            }
            SPECTATORS.add(player.getUniqueId());
            onJoin.run();
            return true;
        }
        else {
            if(!isFull()) {
                PLAYERS.add(player.getUniqueId());
                onJoin.run();
                return true;
            }
        }
        return false;
    }

    public boolean quit(Player player) {
        UUID uuid = player.getUniqueId();
        if(SPECTATORS.contains(uuid)) {
            SPECTATORS.remove(uuid);
            return true;
        }

        if(PLAYERS.contains(uuid)) {
            PLAYERS.remove(uuid);
            return true;
        }

        return false;
    }

    public abstract int getMaxPlayer();

    public List<UUID> getPlayers() {
        return PLAYERS;
    }

    public boolean isFull() {
        return PLAYERS.size() == getMaxPlayer();
    }

    public int getPlayerCount() {
        return PLAYERS.size();
    }

}
