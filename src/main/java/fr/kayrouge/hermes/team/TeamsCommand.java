package fr.kayrouge.hermes.team;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TeamsCommand implements CommandExecutor, TabCompleter {

    public static final List<String> actionArgs = Arrays.asList("list", "create", "delete", "edit");

    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1 || !actionArgs.contains(args[0].toLowerCase())) return false;

        if(args[0].equalsIgnoreCase("list")) {
            StringBuilder stringBuilder = new StringBuilder("Teams: ");
            Team.getTeams().forEach((s, team) -> {
                stringBuilder.append(team.getColor()).append(s);
                stringBuilder.append(", ");
            });

            commandSender.sendMessage(stringBuilder.toString());
            return true;
        }

        if(args[0].equalsIgnoreCase("create")) {
            if(args.length < 2) return false;
            if(Team.getTeams().containsKey(args[1])) {
                commandSender.sendMessage("This team already exist !");
                return true;
            }

            Player player = null;
            if(commandSender instanceof Player) {
                player = (Player) commandSender;
            }

            Team team = Team.create(args[1], TeamColorMapper.getRandomColor(), player);
            commandSender.sendMessage("Created "+team.getColor()+team.getName());
        }

        return false;
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {

            case 1:
                return actionArgs;

            case 2:
                if(!actionArgs.contains(args[0].toLowerCase())) return null;
                if(args[0].equalsIgnoreCase("delete") || args[0].equalsIgnoreCase("edit")) {
                    return new ArrayList<>(Team.getTeams().keySet());
                }

            case 3:
                if(args[0].equalsIgnoreCase("edit")) {
                    return Collections.singletonList("123");
                }

            default: return Collections.emptyList();
        }
    }
}
