package fr.kayrouge.hermes.commands;

import fr.kayrouge.dionysios.Game;
import fr.kayrouge.dionysios.GameManager;
import fr.kayrouge.dionysios.GameSettings;
import fr.kayrouge.hera.Choice;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.event.ChatEvents;
import fr.kayrouge.hermes.game.MurderMysteryGame;
import fr.kayrouge.hermes.game.TerritoryGame;
import fr.kayrouge.hermes.game.murdermystery.MurderMap;
import fr.kayrouge.hermes.mohist.MPacketsHandler;
import fr.kayrouge.hermes.territory.TerritoryManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.atomic.AtomicBoolean;

public class CommandTest implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        commandSender.sendMessage("TEST COMMAND ISSUED '"+label+"'");

        if(label.equalsIgnoreCase("question")) {
            if(!(commandSender instanceof Player player)) return true;

            ChatEvents.askQuestion(player, "Marche stp", (choiceName, questionId, customHandler, data) -> {
                if(data == null) {
                    data = "NULL";
                }
                Hermes.LOGGER.info(choiceName +" "+questionId+" "+data);
                MPacketsHandler.removeQuestion(player, questionId);
                return true;
            }, Choice.of("cancel"), Choice.of("texte moi", Choice.Type.TEXT_ENTRY));
            return true;
        }

        GameManager gm = Hermes.getGameManager();

        if(label.equalsIgnoreCase("murdermystery")) {
            if(!(commandSender instanceof Player player)) return true;

            Game game;
            if(Hermes.getGameManager().getGames().values().stream().noneMatch(game1 -> game1 instanceof MurderMysteryGame)) {
                GameSettings settings = new GameSettings().setMinPlayerCount(2);
                MurderMap map = MurderMap.getMap("test");
                if(map == null) {
                    player.sendMessage("Map don't exist");
                    return true;
                }


                game = gm.createGame(new MurderMysteryGame(gm, settings, map));
            }
            else {
                game = gm.getGames().values().stream().filter(game1 -> game1 instanceof MurderMysteryGame).iterator().next();
            }

            game.playerJoin(player, new AtomicBoolean());
            return true;
        }

        if(label.equalsIgnoreCase("game")) {
            if(!(commandSender instanceof Player player)) return true;

            String territoryName = "test";
            if(args.length > 0) {
                territoryName = args[0];
            }
            Game game;
            if(Hermes.getGameManager().getGames().values().stream().noneMatch(game1 -> game1 instanceof TerritoryGame)) {
                GameSettings settings = new GameSettings()
                        .setMinPlayerCount(Bukkit.getServer().getOnlinePlayers().size())
                        .setMinPlayerToStopGame(1);
                game = gm.createGame(new TerritoryGame(gm, settings, TerritoryManager.getTerritoryManagers().get(territoryName)));
            }
            else {
                game = gm.getGames().values().stream().filter(game1 -> game1 instanceof TerritoryGame).iterator().next();
            }
            game.playerJoin(player, new AtomicBoolean());

            return true;
        }

        return false;
    }
}
