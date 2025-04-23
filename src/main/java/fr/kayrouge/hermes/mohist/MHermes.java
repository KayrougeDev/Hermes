package fr.kayrouge.hermes.mohist;

import com.google.common.io.ByteArrayDataOutput;
import com.google.common.io.ByteStreams;
import com.mohistmc.api.PlayerAPI;
import fr.kayrouge.hera.Hera;
import fr.kayrouge.hera.util.type.PacketType;
import fr.kayrouge.hermes.Hermes;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.plugin.PluginManager;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.logging.Logger;

public class MHermes implements Listener {

    private final Hermes plugin;
    private final Logger LOGGER;
    private final List<UUID> communicationList = new ArrayList<>();

    public MHermes(Hermes plugin) {
        this.plugin = plugin;
        this.LOGGER = Hermes.LOGGER;
    }

    public void onEnable() {
        LOGGER.info("Enabling MHermes");
        plugin.getServer().getMessenger().registerOutgoingPluginChannel(plugin, "hermes:hestia");
        plugin.getServer().getMessenger().registerIncomingPluginChannel(plugin, "hermes:hestia", new MPacketsHandler(this));
        registerEvents();
    }

    public void onDisable() {
        LOGGER.info("Disabling MHermes");
        plugin.getServer().getMessenger().unregisterOutgoingPluginChannel(plugin);
        plugin.getServer().getMessenger().unregisterIncomingPluginChannel(plugin);
    }

    public void registerEvents() {
        PluginManager pm = plugin.getServer().getPluginManager();
        pm.registerEvents(this, plugin);

        //ServerAPI.registerBukkitEvents(this, plugin);
    }

    public boolean hasMod(Player player, String modid) {
        return PlayerAPI.hasMod(player, modid);
    }

    public boolean communicationAvailable(Player player) {
        return communicationList.contains(player.getUniqueId());
    }


    public List<UUID> getCommunicationList() {
        return communicationList;
    }

    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        Player player = event.getPlayer();

        if(PlayerAPI.hasMod(player, "hestia")) {
            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                player.sendMessage("[Hermes] Hestia detected, connection...");
                ByteArrayDataOutput out = ByteStreams.newDataOutput();
                out.writeByte(PacketType.JOIN.getId());
                out.writeLong(Hera.VERSION);
                player.sendPluginMessage(Hermes.PLUGIN, "hermes:hestia", out.toByteArray());
            }, 30);

            Bukkit.getScheduler().runTaskLaterAsynchronously(plugin, () -> {
                if(!communicationAvailable(player)) {
                    player.sendMessage("[Hermes] No answer from Hestia received, try quit and rejoin server (without closing your game)");
                }
            }, 70);
        }
        else {
            player.sendMessage("For a better experience install Hestia");
        }
    }

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        this.communicationList.remove(event.getPlayer().getUniqueId());
    }
}
