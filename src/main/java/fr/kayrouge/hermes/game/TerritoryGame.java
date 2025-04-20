package fr.kayrouge.hermes.game;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import fr.kayrouge.dionysios.*;
import fr.kayrouge.hera.util.type.PacketType;
import fr.kayrouge.hera.util.type.game.TerritoryType;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.team.Team;
import fr.kayrouge.hermes.team.TeamColorMapper;
import fr.kayrouge.hermes.territory.TerritoryManager;
import fr.kayrouge.hermes.util.BlockUtils;
import fr.kayrouge.hermes.util.Style;
import org.bukkit.*;
import org.bukkit.boss.*;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class TerritoryGame extends Game {

    public final TerritoryManager territory;

    private final HashMap<Player, String> startPoint = new HashMap<>();
    private final Map<Player, ItemStack[]> playersInventory = new HashMap<>();
    private final ItemStack[] defaultInventory;
    private final HashMap<UUID, Double> troopsValue = new HashMap<>();

    public TerritoryGame(GameManager manager, GameSettings settings, TerritoryManager territory) {
        super(manager, settings);
        this.territory = territory;
        setStateAndCall(GState.WAITING);

        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.setDisplayName(" ");
            meta.getPersistentDataContainer().set(manager.PLAYER_GAME_KEY, PersistentDataType.STRING, "ignore");
            item.setItemMeta(meta);
        }
        defaultInventory = new ItemStack[36];
        Arrays.fill(defaultInventory, item);
    }

    @Override
    public void onGameWaiting() {
        super.onGameWaiting();
        this.territory.reset();
    }

    @Override
    public void playerJoin(Player player, AtomicBoolean isSpectator) {
        super.playerJoin(player, isSpectator);
        if(isSpectator.get()) {
            player.setGameMode(GameMode.SPECTATOR);
        }
        else {
            player.setGameMode(GameMode.ADVENTURE);
            player.setAllowFlight(true);
            Team team = Team.create(player.getName(), TeamColorMapper.getRandomColor(), player);
            player.sendMessage("You are in team " + team.getColor() + team.getName());
            playersInventory.put(player, player.getInventory().getContents());
            createInventory(player);
        }
        player.teleport(territory.getCenter());
    }

    @Override
    public void lose(Player player) {
        super.lose(player);
        player.setGameMode(GameMode.SPECTATOR);
    }

    @Override
    public void playerQuit(Player player) {
        super.playerQuit(player);

        player.setGameMode(GameMode.ADVENTURE);
        player.setAllowFlight(false);
        Team team = Team.getTeamForPlayer(player);
        if(team != Team.NEUTRAL) {
            Team.getTeams().remove(team.getName());
        }
        if(playersInventory.containsKey(player)) {
            player.getInventory().setContents(playersInventory.get(player));
        }
    }

    @Override
    public void quitOrLose(Player player) {
        super.quitOrLose(player);
        removeTroopsbar(player);
    }

    @Override
    public void onGameStarting() {
        ItemStack circleItem = new ItemStack(Material.BLAZE_ROD);
        ItemMeta circleMeta = circleItem.getItemMeta();
        if(circleMeta != null) {
            circleMeta.setDisplayName(Style.getAccentColor()+"Start Selector");
            circleMeta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            circleMeta.getPersistentDataContainer().set(manager.PLAYER_GAME_KEY, PersistentDataType.STRING, "selector");

            circleItem.setItemMeta(circleMeta);
        }

        getPlayersByRole(GRole.PLAYER).forEach(player -> {
            createInventory(player);
            player.getInventory().setItem(4, circleItem);
            player.getInventory().setHeldItemSlot(4);
        });
        sendMessageToAllPlayer("Choose an area to start !");


        AtomicInteger timer = new AtomicInteger(30);
        AtomicBoolean flag = new AtomicBoolean();
        Bukkit.getScheduler().runTaskTimer(Hermes.PLUGIN, bukkitTask -> {
            if(checkAndStop(bukkitTask)) return;
            if(startPoint.size() == getPlayerCount(GRole.PLAYER) && !flag.get()) {
                flag.set(true);
                if(timer.get() > 5) {
                    timer.set(5);
                }
            }

            int time = timer.getAndDecrement();
            getPlayers().forEach(player -> player.setLevel(time));

            if(time == 0) {
                bukkitTask.cancel();
                setStateAndCall(GState.PLAYING);
            }

        }, 0L, 20L);
    }

    @Override
    public void onGamePlaying() {
        super.onGamePlaying();


        getPlayersByRole(GRole.PLAYER).forEach(player -> {
            if(!startPoint.containsKey(player)) {
                lose(player);
                return;
            }
            Team team = Team.getTeamForPlayer(player);
            if(team == Team.NEUTRAL) {
                lose(player);
                return;
            }
            String[] locString = startPoint.get(player).split(";");
            int x = Integer.parseInt(locString[0]);
            int z = Integer.parseInt(locString[1]);
            this.territory.captureBlock(x, z, team);
            createInventory(player);
            createTroopsBar(player);
        });
        this.territory.updateBlocksForAllTeam(getPlayers());


        AtomicInteger timeSinceStart = new AtomicInteger();
        Bukkit.getScheduler().runTaskTimer(Hermes.PLUGIN, task -> {
            if(checkAndStop(task)) return;

            int time = timeSinceStart.getAndIncrement();
            getPlayers().forEach(player -> player.setLevel(time));

            // TODO remove temporary code
            if(time == 60) {
                task.cancel();
                setStateAndCall(GState.FINISHED);
            }

            // TODO game end logic
        }, 0L, 20L);
    }

    @Override
    public void onGameFinished() {
        super.onGameFinished();
    }

    private void createInventory(Player player) {
        player.getInventory().setContents(defaultInventory);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerInteract(PlayerInteractEvent event){
        Player player = event.getPlayer();
        if(!players.containsKey(player.getUniqueId())) return;
        ItemStack item = event.getItem();
        if(item == null) return;
        if(item.getItemMeta() == null) return;

        PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
        if (dataContainer.has(manager.PLAYER_GAME_KEY, PersistentDataType.STRING)) {
            String gameData = dataContainer.get(manager.PLAYER_GAME_KEY, PersistentDataType.STRING);
            if(gameData == null) return;
            event.setCancelled(true);
            if(gameData.equals("selector") && isState(GState.STARTING)) {
                int startPointX = player.getLocation().getBlockX();
                int startPointZ = player.getLocation().getBlockZ();
                String pointString = startPointX+";"+startPointZ;
                Team team = Team.getTeamForPlayer(player);
                if(team == Team.NEUTRAL) {
                    player.sendMessage("Not in a team !");
                    return;
                }
                if(territory.isBlockInTerritory(startPointX, startPointZ)) {
                    if(startPoint.containsKey(player) && !startPoint.containsValue(pointString)) {
                        // Player change point to one not taken
                        getPlayers().forEach(allPlayer -> {
                            String[] oldLocString = startPoint.get(player).split(";");
                            int oldX = Integer.parseInt(oldLocString[0]);
                            int oldZ = Integer.parseInt(oldLocString[1]);
                            Location oldLoc = new Location(player.getWorld(), oldX, 0, oldZ);
                            BlockUtils.sendFakeBlock(
                                    allPlayer,
                                    player.getWorld().getHighestBlockAt(oldLoc).getLocation(),
                                    player.getWorld().getHighestBlockAt(oldLoc).getType()
                            );
                        });
                    }

                    if(startPoint.containsValue(pointString)) {
                        player.sendMessage("Start point already taken !");
                    }
                    else {
                        if(player.getEquipment() != null && event.getHand() != null) {
                            ItemMeta meta = item.getItemMeta();
                            meta.addEnchant(Enchantment.QUICK_CHARGE, 0, true);
                            item.setItemMeta(meta);
                            player.getEquipment().setItem(event.getHand(), item);
                            player.updateInventory();
                        }
                        startPoint.put(player, pointString);
                        player.sendMessage("Start point selected !");
                        getPlayers().forEach(allPlayer -> BlockUtils.sendFakeBlock(
                                allPlayer,
                                player.getWorld().getHighestBlockAt(player.getLocation()).getLocation(),
                                TeamColorMapper.getMaterialFromColor(team.getColor())
                        ));
                    }
                }
                else {
                    player.sendMessage("Start point out of map limit !");
                }
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerDrop(PlayerDropItemEvent event) {
        Player player = event.getPlayer();
        if(!players.containsKey(player.getUniqueId())) return;
        ItemStack item = event.getItemDrop().getItemStack();
        if(item.getItemMeta() == null) return;

        PersistentDataContainer dataContainer = item.getItemMeta().getPersistentDataContainer();
        if (dataContainer.has(manager.PLAYER_GAME_KEY, PersistentDataType.STRING)) {
            String gameData = dataContainer.get(manager.PLAYER_GAME_KEY, PersistentDataType.STRING);
            if(gameData != null) {
                event.setCancelled(true);
            }
        }
    }

    @EventHandler
    public void playerClickInventory(InventoryClickEvent event) {
        if(!(event.getWhoClicked() instanceof Player player)) return;
        if(!players.containsKey(player.getUniqueId())) return;

        event.setCancelled(true);
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerSwitchHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if(!players.containsKey(player.getUniqueId())) return;

        if(isState(GState.PLAYING)) {
            event.setCancelled(true);
            player.openInventory(Bukkit.createInventory(player, 9, "MENU"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerHeldItemChange(PlayerItemHeldEvent event) {
        if(!players.containsKey(event.getPlayer().getUniqueId())) return;
        boolean isPlaying = isState(GState.PLAYING);
        if(isState(GState.STARTING) || isPlaying) {
            if(isPlaying) {
                if(event.getNewSlot() > event.getPreviousSlot()) {
                    incrementTroopsbar(event.getPlayer(), 0.05);
                }
                else {
                    incrementTroopsbar(event.getPlayer(), -0.05);
                }
            }
            event.setCancelled(true);
        }
    }


    private void createTroopsBar(Player player) {
        final double defaultValue = 0.2d;
        if(Hermes.isMohist() && Hermes.mohistHermes().communicationAvailable(player)) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketType.TERRITORY_GAME.getId());
            out.writeByte(TerritoryType.SET.getId());
            out.writeDouble(defaultValue);
            player.sendPluginMessage(Hermes.PLUGIN, "hermes:hestia", out.toByteArray());
            troopsValue.put(player.getUniqueId(), defaultValue);
        }
        else {
            final BossBar troopsBar = Bukkit.createBossBar(new NamespacedKey(Hermes.PLUGIN, player.getUniqueId().toString()), "Troups", BarColor.BLUE, BarStyle.SEGMENTED_20, BarFlag.PLAY_BOSS_MUSIC);
            troopsBar.setProgress(defaultValue);
            troopsBar.addPlayer(player);
            troopsBar.setVisible(true);
        }
    }

    private void removeTroopsbar(Player player) {
        if(Hermes.isMohist() && Hermes.mohistHermes().communicationAvailable(player)) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketType.TERRITORY_GAME.getId());
            out.writeByte(TerritoryType.VISIBLE.getId());
            out.writeBoolean(false);
            player.sendPluginMessage(Hermes.PLUGIN, "hermes:hestia", out.toByteArray());
            troopsValue.remove(player.getUniqueId());
        }
        else {
            Bukkit.removeBossBar(new NamespacedKey(Hermes.PLUGIN, player.getUniqueId().toString()));
        }
    }

    private void incrementTroopsbar(Player player, double value) {
        if(Hermes.isMohist() && Hermes.mohistHermes().communicationAvailable(player)) {
            ByteArrayDataOutput out = ByteStreams.newDataOutput();
            out.writeByte(PacketType.TERRITORY_GAME.getId());
            out.writeByte(TerritoryType.SET.getId());
            out.writeDouble(troopsValueCalculator(troopsValue.get(player.getUniqueId()), value));
            player.sendPluginMessage(Hermes.PLUGIN, "hermes:hestia", out.toByteArray());
        }
        else {
            BossBar bossBar = Bukkit.getBossBar(new NamespacedKey(Hermes.PLUGIN, player.getUniqueId().toString()));
            if(bossBar != null) {
                bossBar.setProgress(troopsValueCalculator(bossBar.getProgress(), value));
            }
        }
    }

    private double troopsValueCalculator(double oldValue, double increment) {
        if(oldValue+increment > 1) {
            return 1;
        }
        else if(oldValue+increment < 0) {
            return 0;
        }
        else {
            return oldValue+increment;
        }
    }
}
