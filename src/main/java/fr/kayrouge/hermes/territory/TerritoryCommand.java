package fr.kayrouge.hermes.territory;

import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.team.Team;
import fr.kayrouge.hermes.team.TeamColorMapper;
import fr.kayrouge.hermes.util.FakeBlockUtils;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TerritoryCommand implements CommandExecutor, TabCompleter, Listener {

    public static final List<String> actionArgs = Arrays.asList(
            "info", "capture", "help", "update", "updateAll", "updateAllTeams",
            "item");

    public static final NamespacedKey STICK_TEAM_DATA = new NamespacedKey(Hermes.PLUGIN, "team_data");


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1 || !actionArgs.contains(args[0])) {
            return false;
        }

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
                return true;
            }
            if(!args[2].startsWith("~") && !isInteger(args[2])) {
                commandSender.sendMessage("Invalid x coordinate !");
                return true;
            }
            if(!args[3].startsWith("~") && !isInteger(args[3])) {
                commandSender.sendMessage("Invalid z coordinate !");
                return true;
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

            Team newTeam = Team.getTeam(args[1]);

            Team blockTeam = Hermes.getTerritoryManager().getBlockOwner(blockX, blockZ);
            String s = "OLD TEAM: "+blockTeam.getColor()+blockTeam.getName();

            Hermes.getTerritoryManager().captureBlock(blockX, blockZ, newTeam);

            blockTeam = Hermes.getTerritoryManager().getBlockOwner(blockX, blockZ);
            s += "   NEW TEAM: "+blockTeam.getColor()+blockTeam.getName();

            commandSender.sendMessage(s);

            return true;
        }

        if(args[0].equalsIgnoreCase("update")) {
            if(!(commandSender instanceof Player)) return true;
            Player player = (Player)commandSender;

            TerritoryManager territory = Hermes.getTerritoryManager();

            int x = player.getLocation().getBlockX();
            int z = player.getLocation().getBlockZ();

            Team team = territory.getBlockOwner(x, z);

            //Hermes.getTerritoryManager().updateBlock(x, z, player.getWorld(), team);

            FakeBlockUtils.sendFakeBlock(player, player.getLocation().add(0, -1, 0), TeamColorMapper.getMaterialFromColor(team.getColor()));

            return true;
        }

        if(args[0].equalsIgnoreCase("updateAll")) {
            if(args.length < 2) {
                commandSender.sendMessage("Not enough args");
                return false;
            }
            if(!Team.getTeams().containsKey(args[1])) {
                commandSender.sendMessage("Unknown team '"+args[1]+"'.");
                return true;
            }

            Team team = Team.getTeam(args[1]);

            Hermes.getTerritoryManager().updateAllBlocks(team);
            return true;
        }

        if(args[0].equalsIgnoreCase("updateAllTeams")) {
            Hermes.getTerritoryManager().updateBlocksForAllTeam();
            return true;
        }

        if(args[0].equalsIgnoreCase("item")) {
            if(!(commandSender instanceof Player)) return true;
            Player player = (Player)commandSender;

            Team team = Team.NEUTRAL;
            if(args.length > 1) {
                team = Team.getTeam(args[1]);
            }

            ItemStack stack = new ItemStack(Material.STICK);
            ItemMeta meta = stack.getItemMeta();
            if(meta != null) {
                meta.setDisplayName(team.getColor()+"Territory Extender");
                meta.setLore(Collections.singletonList(team.getColor()+team.getName()));
                meta.getPersistentDataContainer().set(STICK_TEAM_DATA, PersistentDataType.STRING, team.getName());

                stack.setItemMeta(meta);
            }


            player.getInventory().addItem(stack);
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

    @EventHandler
    public void itemUsed(PlayerInteractEvent event) {
        ItemStack stack = event.getItem();
        Block block = event.getClickedBlock();
        if(Objects.isNull(block) || Objects.isNull(stack)) return;

        if(stack.hasItemMeta() && stack.getItemMeta() != null) {
            ItemMeta meta =  stack.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();
            if(dataContainer.has(STICK_TEAM_DATA, PersistentDataType.STRING)) {
                Hermes.getTerritoryManager().captureBlock(block.getX(), block.getZ(),
                        Team.getTeam(dataContainer.get(STICK_TEAM_DATA, PersistentDataType.STRING)));
                event.setCancelled(true);
            }
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
                if(args[0].equalsIgnoreCase("capture") || args[0].equalsIgnoreCase("item") || args[0].equalsIgnoreCase("updateAll")) {
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
