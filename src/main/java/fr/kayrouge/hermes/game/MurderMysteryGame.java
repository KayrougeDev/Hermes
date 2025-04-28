package fr.kayrouge.hermes.game;

import fr.kayrouge.dionysios.*;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.game.murdermystery.MurderMap;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;

public class MurderMysteryGame extends Game {

    private final MurderMap map;
    private final Player[] murders;
    private Player sheriff = null;

    public MurderMysteryGame(GameManager manager, GameSettings settings, MurderMap map) {
        super(manager, settings.setMaxPlayerCount(map.getMaxPlayerCount()));
        this.murders = new Player[map.getMaxMurderCount()];
        this.map = map;
        setStateAndCall(GState.WAITING);
    }

    @Override
    public void onGameStarting() {
        if(Arrays.stream(chooseMurder()).filter(Objects::nonNull).count() != map.getMaxMurderCount() || chooseSheriff() == null) {
            sendMessageToAllPlayer("Error while choosing murders and sheriff, game will stop in 5 seconds, please join another");
            Bukkit.getScheduler().runTaskLaterAsynchronously(Hermes.PLUGIN, task -> setState(GState.TERMINATED), 5*20L);
            return;
        }

        AtomicInteger timer = new AtomicInteger(10);
        Bukkit.getScheduler().runTaskTimer(Hermes.PLUGIN, task -> {
            int time = timer.get();
            sendMessageToAllPlayer("Role revealed in "+time);

            if(timer.decrementAndGet() == 0) {
                task.cancel();
                setState(GState.PLAYING);
            }

        }, 0L, 20L);
    }

    @Override
    public void onGamePlaying() {
        getPlayersByRole(GRole.PLAYER).forEach(player -> {
            String role = "innocent";
            if(Arrays.stream(murders).anyMatch(player1 -> player1 == player)) {
                role = "murder";
            }
            else if(this.sheriff == player) {
                role = "sheriff";
            }
            player.sendActionBar("You are "+role);
        });
    }

    private Player chooseSheriff() {
        this.sheriff = getRandomPlayer();
        if(Arrays.stream(murders).noneMatch(player -> player == this.sheriff)) {
            return this.sheriff;
        }
        else {
            return chooseSheriff();
        }
    }

    private Player[] chooseMurder() {
        for(int i = 0; i < murders.length; i++) {
            Player player = getRandomPlayer();
            murders[i] = player;
        }
        return this.murders;
    }


    public Player getRandomPlayer() {
        int i = 0;
        while (i < 3) {
            List<Player> onlinePlayers = getPlayersByRole(GRole.PLAYER);
            Player player = onlinePlayers.isEmpty() ? null : onlinePlayers.get(new Random().nextInt(onlinePlayers.size()));

            if(player != null) {
                return player;
            }
            i++;
        }
        return null;
    }
}
