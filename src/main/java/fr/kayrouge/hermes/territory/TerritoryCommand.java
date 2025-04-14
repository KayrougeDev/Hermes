package fr.kayrouge.hermes.territory;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.kayrouge.hera.Choice;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.event.ChatEvents;
import fr.kayrouge.hermes.mohist.PacketListeners;
import fr.kayrouge.hermes.team.Team;
import fr.kayrouge.hermes.util.BlockUtils;
import fr.kayrouge.hermes.util.MessageUtil;
import fr.kayrouge.hermes.util.Style;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.event.ClickEvent;
import org.bukkit.ChatColor;
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
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class TerritoryCommand implements CommandExecutor, TabCompleter, Listener {

    public static final List<String> actionArgs = Arrays.asList(
            "info", "list", "help", "update", "item", "load", "save");

    public static final NamespacedKey TERRITORY_CREATOR_DATA = new NamespacedKey(Hermes.PLUGIN, "creator_data");


    @Override
    public boolean onCommand(@NotNull CommandSender commandSender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        if(args.length < 1 || !actionArgs.contains(args[0])) {
            return false;
        }

        if(args[0].equalsIgnoreCase("save")) {
            commandSender.sendMessage("Saving territories...");
            TerritoryManager.save();
            commandSender.sendMessage("Territories saved");
            return true;
        }

        if(args[0].equalsIgnoreCase("load")) {
            commandSender.sendMessage("Loading territories...");
            TerritoryManager.load();
            commandSender.sendMessage("Territories loaded");
            return true;
        }

        if(args[0].equalsIgnoreCase("list")) {

            StringBuilder builder = new StringBuilder("territories: ");

            TerritoryManager.getTerritoryManagers().forEach((s, territoryManager) -> {
                builder.append(s).append(", ");
            });

            commandSender.sendMessage(builder.toString());

            return true;
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

            // TODO
            commandSender.sendMessage(MessageUtil.COMMAND_DISABLED);
            return false;

            //Team team = Hermes.getTerritoryManager().getBlockOwner(blockX, blockZ);

            //commandSender.sendMessage("TEAM: "+team.getColor()+team.getName());


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

            // TODO
            commandSender.sendMessage(MessageUtil.COMMAND_DISABLED);
            return false;

//            Team newTeam = Team.getTeam(args[1]);
//
//            Team blockTeam = Hermes.getTerritoryManager().getBlockOwner(blockX, blockZ);
//            String s = "OLD TEAM: "+blockTeam.getColor()+blockTeam.getName();
//
//            Hermes.getTerritoryManager().captureBlock(blockX, blockZ, newTeam);
//
//            blockTeam = Hermes.getTerritoryManager().getBlockOwner(blockX, blockZ);
//            s += "   NEW TEAM: "+blockTeam.getColor()+blockTeam.getName();

//            commandSender.sendMessage(s);
        }

        if(args[0].equalsIgnoreCase("update")) {
            if(!(commandSender instanceof Player)) return true;
            Player player = (Player)commandSender;

            //TerritoryManager territory = Hermes.getTerritoryManager();

            int x = player.getLocation().getBlockX();
            int z = player.getLocation().getBlockZ();

            //Team team = territory.getBlockOwner(x, z);

            //Hermes.getTerritoryManager().updateBlock(x, z, player.getWorld(), team);

            //FakeBlockUtils.sendFakeBlock(player, player.getLocation().add(0, -1, 0), TeamColorMapper.getMaterialFromColor(team.getColor()));

            // TODO
            commandSender.sendMessage(MessageUtil.COMMAND_DISABLED);
            return false;
        }


        if(args[0].equalsIgnoreCase("item")) {
            if(!(commandSender instanceof Player)) return true;
            Player player = (Player)commandSender;

            ItemStack creatorItem = new ItemStack(Material.STICK);
            ItemMeta creatorMeta = creatorItem.getItemMeta();
            if(creatorMeta != null) {

                creatorMeta.setDisplayName(Style.getAccentColor()+"Territory Creator");
                creatorMeta.setLore(Arrays.asList(Style.getAccentColor()+"Bottom Left: 0;0",
                        Style.getAccentColor()+"Top Right: 0;0"));
                creatorMeta.getPersistentDataContainer().set(TERRITORY_CREATOR_DATA, PersistentDataType.INTEGER_ARRAY, new int[5]);

                creatorItem.setItemMeta(creatorMeta);
            }

            ItemStack circleItem = new ItemStack(Material.BLAZE_ROD);
            ItemMeta circleMeta = circleItem.getItemMeta();
            if(circleMeta != null) {
                circleMeta.setDisplayName(Style.getAccentColor()+"Circle Creator");
                circleMeta.getPersistentDataContainer().set(Hermes.ACTION_ITEM_DATA, PersistentDataType.STRING, "circle");

                circleItem.setItemMeta(circleMeta);
            }


            player.getInventory().addItem(creatorItem);
            player.getInventory().addItem(circleItem);
            return true;
        }

        return false;
    }


    @EventHandler
    public void itemUsed(PlayerInteractEvent event) {
        ItemStack stack = event.getItem();
        Block block = event.getClickedBlock();
        EquipmentSlot hand = event.getHand();
        Player player = event.getPlayer();
        if(hand == null || player.getEquipment() == null) return;
        if(Objects.isNull(block) || Objects.isNull(stack)) return;

        if(stack.hasItemMeta() && stack.getItemMeta() != null) {
            ItemMeta meta =  stack.getItemMeta();
            PersistentDataContainer dataContainer = meta.getPersistentDataContainer();

            if(dataContainer.has(TERRITORY_CREATOR_DATA, PersistentDataType.INTEGER_ARRAY)) {
                int[] pos = dataContainer.get(TERRITORY_CREATOR_DATA, PersistentDataType.INTEGER_ARRAY);
                if(pos == null || pos.length < 5) return;
                if(pos[4] == 2) {
                    Component component = getCreatorMessageComponent(meta);
                    if(Hermes.isMohist() && Hermes.mohistHermes().communicationAvailable(player)) {
                        PacketListeners.createAndSendQuestion(player, "Do you want to reset this "+ meta.getDisplayName()+ ChatColor.WHITE +" or create a new territory ?", (choiceName, questionId, data) ->  {
                            if(choiceName.equalsIgnoreCase("cancel")) {
                                player.sendMessage("Creation cancelled !");
                            } else if(choiceName.equalsIgnoreCase("reset")) {

                                dataContainer.set(TERRITORY_CREATOR_DATA, PersistentDataType.INTEGER_ARRAY, new int[5]);

                                stack.setItemMeta(meta);
                                player.getEquipment().setItem(hand, updateCreatorLore(stack));
                            }
                            else if(choiceName.equalsIgnoreCase("create")) {
                                if(!(data instanceof String)) {
                                    player.sendMessage("Name not valid, retry later or update Hestia");
                                    return;
                                }
                                String territoryName = (String) data;

                                TerritoryManager territoryManager = TerritoryManager.create(territoryName, player.getWorld(),
                                        pos[0], pos[1], pos[2], pos[3]);

                                if(territoryManager == null) {
                                    player.sendMessage("Invalid size !");
                                }
                                else {
                                    player.sendMessage("Created: "+territoryName);
                                }
                            }
                        }, Choice.of("cancel"), Choice.of("reset"), Choice.of("create", Choice.Type.TEXT_ENTRY));
                        return;
                    }
                    ChatEvents.askChatQuestion(component, player, answer -> {
                        if(answer.equalsIgnoreCase("reset")) {

                            dataContainer.set(TERRITORY_CREATOR_DATA, PersistentDataType.INTEGER_ARRAY, new int[5]);

                            stack.setItemMeta(meta);
                            player.getEquipment().setItem(hand, updateCreatorLore(stack));

                            return true;
                        }
                        else if(answer.startsWith("create")) {
                            String[] args = answer.split(" ");
                            if(args.length < 2) {
                                player.sendMessage("Please insert a name");
                                return true;
                            }
                            StringBuilder territoryName = new StringBuilder();
                            for (int i = 1; i < args.length; i++) {
                                territoryName.append(args[i]);
                            }

                            TerritoryManager territoryManager = TerritoryManager.create(territoryName.toString(), player.getWorld(),
                                    pos[0], pos[1], pos[2], pos[3]);

                            if(territoryManager == null) {
                                player.sendMessage("Invalid size !");
                            }
                            else {
                                player.sendMessage("Created: "+territoryName);
                            }

                            return true;
                        }
                        return false;
                    });
                    return;
                }
                else if(pos[4] == 0)  {
                    pos[0] = block.getX();
                    pos[1] = block.getZ();
                    pos[4] = 1;
                }
                else if(pos[4] == 1) {
                    pos[2] = block.getX();
                    pos[3] = block.getZ();
                    pos[4] = 2;

                }
                dataContainer.set(TERRITORY_CREATOR_DATA, PersistentDataType.INTEGER_ARRAY, pos);
                stack.setItemMeta(meta);
                player.getEquipment().setItem(hand, updateCreatorLore(stack));

            } else if(dataContainer.has(Hermes.ACTION_ITEM_DATA, PersistentDataType.STRING)) {
                if(dataContainer.get(Hermes.ACTION_ITEM_DATA, PersistentDataType.STRING).equalsIgnoreCase("circle")) {
                    BlockUtils.createCircle(player.getLocation().add(0, -3, 0), 8, Material.SAND, player);

                }
            }

// TODO
//  if(dataContainer.has(STICK_TEAM_DATA, PersistentDataType.STRING)) {
//                Hermes.getTerritoryManager().captureBlock(block.getX(), block.getZ(),
//                        Team.getTeam(dataContainer.get(STICK_TEAM_DATA, PersistentDataType.STRING)));
//                event.setCancelled(true);
//            }
        }
    }

    private static @NotNull Component getCreatorMessageComponent(ItemMeta meta) {
        Component component = Component.text("Do you want to reset this "+ meta.getDisplayName());
        component = component.appendNewline();
        component = component.append(Component.text("Or create the new territory ?"));
        component = component.appendNewline();
        component = component.append(Component.text("Create").clickEvent(ClickEvent.suggestCommand("create <name-without-space>")));
        component = component.append(Component.space());
        component = component.append(Component.text("Reset").clickEvent(ClickEvent.runCommand("reset")));
        return component;
    }

    private ItemStack updateCreatorLore(ItemStack stack) {
        ItemMeta meta = stack.getItemMeta();
        if(meta == null) return stack;
        if(meta.getPersistentDataContainer().has(TERRITORY_CREATOR_DATA, PersistentDataType.INTEGER_ARRAY)) {
            int[] pos = meta.getPersistentDataContainer().get(TERRITORY_CREATOR_DATA, PersistentDataType.INTEGER_ARRAY);
            if(pos == null || pos.length < 5) return stack;
            String bottomLeftString = Style.getAccentColor()+"Bottom Left: "+pos[0]+";"+pos[1];
            String topRightString = Style.getAccentColor()+"Top Right: "+pos[2]+";"+pos[3];

            meta.setLore(Arrays.asList(bottomLeftString, topRightString, String.valueOf(pos[4])));
        }
        stack.setItemMeta(meta);
        return stack;
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
