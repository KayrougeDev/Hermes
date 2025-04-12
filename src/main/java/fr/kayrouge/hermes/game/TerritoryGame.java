package fr.kayrouge.hermes.game;

import fr.kayrouge.dionysios.*;
import fr.kayrouge.hermes.Hermes;
import fr.kayrouge.hermes.team.Team;
import fr.kayrouge.hermes.team.TeamColorMapper;
import fr.kayrouge.hermes.territory.TerritoryManager;
import fr.kayrouge.hermes.util.Style;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.player.PlayerDropItemEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.event.player.PlayerSwapHandItemsEvent;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

public class TerritoryGame extends Game {

    public final TerritoryManager territory;

    private final Map<Player, ItemStack[]> playersInventory = new HashMap<>();
    private final ItemStack[] defaultInventory;

    public TerritoryGame(GameManager manager, GameSettings settings, TerritoryManager territory) {
        super(manager, settings);
        this.territory = territory;
        setStateAndCall(GState.WAITING);

        ItemStack item = new ItemStack(Material.RED_STAINED_GLASS_PANE);
        ItemMeta meta = item.getItemMeta();
        if(meta != null) {
            meta.setDisplayName("");
            meta.getPersistentDataContainer().set(manager.PLAYER_GAME_KEY, PersistentDataType.STRING, "ignore");
            item.setItemMeta(meta);
        }
        defaultInventory = new ItemStack[36];
        Arrays.fill(defaultInventory, item);
    }

    @Override
    public void onGameWaiting() {
        this.territory.reset();
    }

    @Override
    public void playerJoin(Player player, boolean isSpectator) {
        super.playerJoin(player, isSpectator);
        Team.create(player.getName(), TeamColorMapper.getRandomColor(), player);
        playersInventory.put(player, player.getInventory().getContents());
        player.teleport(territory.getCenter());
        createInventory(player);
        Bukkit.getOnlinePlayers().forEach(player1 -> {
            player.hidePlayer(Hermes.PLUGIN, player1);
            player1.hidePlayer(Hermes.PLUGIN, player);
        });
    }

    @Override
    public void playerQuit(Player player) {
        super.playerQuit(player);
        Team team = Team.getTeamForPlayer(player);
        if(team != Team.NEUTRAL) {
            Team.getTeams().remove(team.getName());
        }
        player.getInventory().setContents(playersInventory.get(player));
        Bukkit.getOnlinePlayers().forEach(player1 -> {
            if(!player1.getPersistentDataContainer().has(manager.PLAYER_GAME_KEY, PersistentDataType.INTEGER)) {
                player.showPlayer(Hermes.PLUGIN, player1);
                player1.showPlayer(Hermes.PLUGIN, player);
            }
        });
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

        getPlayers(GRole.PLAYER).forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null) {
                createInventory(player);
                player.getInventory().setItem(4, circleItem);
                player.getInventory().setHeldItemSlot(4);
            }
        });
        sendMessageToAllPlayer("Choose an area to start !");


        AtomicInteger timer = new AtomicInteger(30);
        Bukkit.getScheduler().runTaskTimer(Hermes.PLUGIN, bukkitTask -> {
            checkAndStop(bukkitTask);

            int time = timer.getAndDecrement();
            players.forEach((uuid, role) -> {
                Player player = Bukkit.getPlayer(uuid);
                if(player != null) {
                    player.setLevel(time);
                }
            });

            if(time == 0) {
                bukkitTask.cancel();
                setStateAndCall(GState.PLAYING);
            }

        }, 0L, 20L);
    }

    @Override
    public void onGamePlaying() {
        getPlayers(GRole.PLAYER).forEach(uuid -> {
            Player player = Bukkit.getPlayer(uuid);
            if(player != null) {
                createInventory(player);
            }
        });

        AtomicInteger timeSinceStart = new AtomicInteger();
        Bukkit.getScheduler().runTaskTimer(Hermes.PLUGIN, task -> {
            checkAndStop(task);

            int time = timeSinceStart.getAndIncrement();
            players.forEach((uuid, role) -> {
                Player player = Bukkit.getPlayer(uuid);
                if(player != null) {
                    player.setLevel(time);
                }
            });

            // TODO remove temporary code
            if(time == 60) {
                task.cancel();
                setStateAndCall(GState.FINISHED);
            }

            // TODO game end logic
        }, 0L, 20L);
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
            if(gameData.equals("ignore")) {
                event.setCancelled(true);
            }
            else if(gameData.equals("selector") && isState(GState.STARTING)) {
                if(territory.isBlockInTerritory(player.getLocation().getBlockX(), player.getLocation().getBlockZ())) {
                    // TODO fix isBlockInTerritory
                    item.getItemMeta().addEnchant(Enchantment.QUICK_CHARGE, 0, true);
                    player.sendMessage("Area selected !");
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

    @EventHandler(priority = EventPriority.HIGH)
    public void playerSwitchHand(PlayerSwapHandItemsEvent event) {
        Player player = event.getPlayer();
        if(!players.containsKey(player.getUniqueId())) return;

        if(isState(GState.PLAYING)) {
            event.setCancelled(true);
            player.openInventory(Bukkit.createInventory(player, 9, "TEST"));
        }
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void playerHeldItemChange(PlayerItemHeldEvent event) {
        if(!players.containsKey(event.getPlayer().getUniqueId())) return;
        if(isState(GState.STARTING)) {
            event.setCancelled(true);
        }
    }

}
