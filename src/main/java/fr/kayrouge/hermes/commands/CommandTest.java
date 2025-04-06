package fr.kayrouge.hermes.commands;

import fr.kayrouge.hera.Choice;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.mohist.MHermes;
import fr.kayrouge.hermes.mohist.MessageListener;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

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
                    MessageListener.createAndSendQuestion(player, "Marche stp", s -> {
                        Hermes.LOGGER.info(s);
                    }, Choice.of("cancel"), Choice.of("texte moi", Choice.Type.TEXT_ENTRY));
                }
            }
        }

        return false;
    }
}
