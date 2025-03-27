package fr.kayrouge.hermes.territory;

import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.team.Team;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class TerritoryCommand implements CommandExecutor, TabCompleter {

    public static final List<String> actionArgs = Arrays.asList("info", "capture", "help");


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1 || !actionArgs.contains(args[0].toLowerCase())) return false;

        if(args[0].equalsIgnoreCase("info")) {
            if(args.length < 3) {
                commandSender.sendMessage("Not enough args");
                return false;
            }
            if(!args[1].startsWith("~") && !isInteger(args[1])) {
                commandSender.sendMessage("Invalid x coordinate !");
                return false;
            }
            if(!args[2].startsWith("~") && !isInteger(args[2])) {
                commandSender.sendMessage("Invalid z coordinate !");
                return false;
            }
            int blockX;
            int blockZ;
            if(commandSender instanceof Player) {
                blockX = parseCoordinate(args[1], ((Player)commandSender).getLocation().getBlockX());
                blockZ = parseCoordinate(args[2], ((Player)commandSender).getLocation().getBlockZ());
            }
            else {
                blockX = parseCoordinate(args[1], 0);
                blockZ = parseCoordinate(args[2], 0);
            }

            Team team = Hermes.getTerritoryManager().getBlockOwner(blockX, blockZ);

            commandSender.sendMessage("TEAM: "+team.getColor()+team.getName());

            return true;


        }

        if(args[0].equalsIgnoreCase("capture")) {

            if(args.length < 4) {
                commandSender.sendMessage("Not enough args");
                return false;
            }
            if(!Team.getTeams().containsKey(args[1])) {
                commandSender.sendMessage("Unknown team '"+args[1]+"'.");
                return false;
            }
            if(!args[2].startsWith("~") && !isInteger(args[2])) {
                commandSender.sendMessage("Invalid x coordinate !");
                return false;
            }
            if(!args[3].startsWith("~") && !isInteger(args[3])) {
                commandSender.sendMessage("Invalid z coordinate !");
                return false;
            }
            int blockX;
            int blockZ;
            if(commandSender instanceof Player) {
                blockX = parseCoordinate(args[2], ((Player)commandSender).getLocation().getBlockX());
                blockZ = parseCoordinate(args[3], ((Player)commandSender).getLocation().getBlockZ());
            }
            else {
                blockX = parseCoordinate(args[2], 0);
                blockZ = parseCoordinate(args[3], 0);
            }

            Team team = Hermes.getTerritoryManager().getBlockOwner(blockX, blockZ);

            Hermes.getTerritoryManager().captureBlock(blockX, blockZ, team);
            commandSender.sendMessage("TEAM: "+team.getColor()+team.getName());

            return true;
        }

        return false;
    }

    private int parseCoordinate(String arg, int playerCoord) {
        if (arg.startsWith("~")) {
            if(arg.length() == 1) {
                return playerCoord;
            }
            else {
                try {
                    return playerCoord + Integer.parseInt(arg.substring(1, arg.length()-1));
                } catch (NumberFormatException e) {
                    return playerCoord;
                }
            }
        }
        try {
            return Integer.parseInt(arg);
        } catch (NumberFormatException e) {
            return playerCoord;
        }
    }

    @Override
    public @Nullable List<String> onTabComplete(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        switch (args.length) {

            case 1:
                return actionArgs;

            case 2:
                if(!actionArgs.contains(args[0].toLowerCase())) return null;
                if(args[0].equalsIgnoreCase("info")) {
                    return getLocationArgs(commandSender);
                }
                if(args[0].equalsIgnoreCase("capture")) {
                    return new ArrayList<>(Team.getTeams().keySet());
                }

            case 3:
                if(args[0].equalsIgnoreCase("capture")) {
                    return getLocationArgs(commandSender);
                }

            default: return Collections.emptyList();
        }
    }

    private static List<String> getLocationArgs(Object o) {
        List<String> list = new ArrayList<>();
        if(o instanceof Entity) {
            Location location = ((Entity) o).getLocation();
            list.add(location.getBlockX() + " " + location.getBlockZ());
        }
        list.add("~ ~");
        return list;
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
