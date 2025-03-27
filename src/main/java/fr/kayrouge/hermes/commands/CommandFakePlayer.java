package fr.kayrouge.hermes.commands;

import fr.kayrouge.hermes.util.MessageUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;

import java.util.Random;

public class CommandFakePlayer implements CommandExecutor {

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(!(commandSender instanceof Player)) return true;
        Player player = (Player) commandSender;

        String name;
        if(args.length == 0) {
            name = "Player"+ new Random().nextInt(1000);
        }
        else {
            name = args[0];
        }

        commandSender.sendMessage(MessageUtil.COMMAND_DISABLED);


        return false;
    }
}
