package fr.kayrouge.hermes.game;

import fr.kayrouge.hermes.territory.TerritoryManager;
import org.bukkit.entity.Player;

public class TerritoryGame extends Game {


    private final TerritoryManager territory;

    public TerritoryGame(String name, TerritoryManager territoryManager) {
        super(name);
        this.territory = territoryManager;
    }

    @Override
    public boolean join(Player player, boolean spectator, Runnable onJoin) {
        return super.join(player, spectator, onJoin);
    }

    @Override
    public int getMaxPlayer() {
        if(territory.getSize() < 100) {
            return 3;
        } else if (territory.getSize() < 750) {
            return 6;
        }
        else {
            return 10;
        }
    }

    public TerritoryManager getTerritory() {
        return territory;
    }
}
