package fr.kayrouge.hermes.commands;

import fr.kayrouge.dionysios.Game;
import fr.kayrouge.dionysios.GameSettings;
import fr.kayrouge.hera.Choice;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.game.TerritoryGame;
import fr.kayrouge.hermes.mohist.MHermes;
import fr.kayrouge.hermes.mohist.PacketListeners;
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


        if(label.equalsIgnoreCase("question")) {
            if(!(commandSender instanceof Player)) return true;
            Player player = (Player)commandSender;

            if(Hermes.isMohist()) {
                MHermes mHermes = Hermes.mohistHermes();
                mHermes.getCommunicationList().forEach(uuid -> {
                    Player target = Bukkit.getPlayer(uuid);
                    if(target != null) {
                        Hermes.LOGGER.info(target.getDisplayName());
                    }
                });

                if(mHermes.communicationAvailable(player)) {
                    PacketListeners.createAndSendQuestion(player, "Marche stp", (choiceName, questionId, data) -> {
                        if(data == null) {
                            data = "NULL";
                        }
                        Hermes.LOGGER.info(choiceName +" "+questionId+" "+data);
                        PacketListeners.removeQuestion(player, questionId);
                    }, Choice.of("cancel"), Choice.of("texte moi", Choice.Type.TEXT_ENTRY));
                }
            }
            return true;
        }

        if(label.equalsIgnoreCase("game")) {
            if(!(commandSender instanceof Player)) return true;
            Player player = (Player)commandSender;

            String territoryName = "test";
            if(args.length > 0) {
                territoryName = args[0];
            }
            Game game;
            if(Hermes.getGameManager().getGames().isEmpty()) {
                GameSettings settings = new GameSettings()
                        .setMinPlayerCount(Bukkit.getServer().getOnlinePlayers().size())
                        .setMinPlayerToStopGame(1);
                game = Hermes.getGameManager().createGame(new TerritoryGame(Hermes.getGameManager(), settings, TerritoryManager.getTerritoryManagers().get(territoryName)));
            }
            else {
                game = Hermes.getGameManager().getGames().values().iterator().next();
            }

            game.playerJoin(player, new AtomicBoolean());

            return true;
        }

        return false;
    }
}
